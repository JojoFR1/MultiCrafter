package dev.jojofr.multicrafter.type.menu;

import arc.scene.Element;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import dev.jojofr.multicrafter.MultiCrafterBlock;
import dev.jojofr.multicrafter.type.Recipe;
import dev.jojofr.multicrafter.world.AttributeMultiCrafterBlock;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;

public enum MenuTypes implements MenuType {
    DEFAULT("default"),
    DEFAULT_LARGE("defaultLarge"),
    FULLSCREEN("fullScreen");
    
    public final String name;
    
    MenuTypes(String name) { this.name = name; }
    
    @Override
    public void build(Table table, MultiCrafterBlock.MultiCrafterBuild build) {
        switch (this) {
            case DEFAULT -> buildDefault(table, build);
            case DEFAULT_LARGE -> buildDefault(table, build, true);
            // case FULLSCREEN -> new Table();
        };
    }
    
    public static MenuTypes get(String blockName, String name) {
        for (MenuTypes type : values()) if (type.name.equals(name)) return type;
        
        Log.warn("["+blockName+"] MenuType with name '" + name + "' not found, using default.");
        return DEFAULT;
    }
    
    
    private void buildDefault(Table table, MultiCrafterBlock.MultiCrafterBuild build) { buildDefault(table, build, false); }
    private void buildDefault(Table table, MultiCrafterBlock.MultiCrafterBuild build, boolean large) {
        MultiCrafterBlock block = (MultiCrafterBlock) build.block;
        int index = 0;
        
        Table buttonTable = new Table();
        ButtonGroup<Button> buttonGroup = new ButtonGroup<>();
        buttonGroup.setMinCheckCount(0);
        buttonGroup.setMaxCheckCount(1);
        
        for (Recipe recipe : block.recipes) {
            Button button = new Button(Styles.clearTogglet);
            buttonGroup.add(button);
            Table buttonContent = new Table();
            
            Table recipeTable = new Table();
            if (!recipe.unlockedNow()) {
                recipeTable.image(Icon.lock).pad(4f).fill().grow();
                recipeTable.addListener(Tooltip.Tooltips.getInstance().create("@locked", Vars.mobile));
                
                buttonContent.add(recipeTable).pad(4f).growX();
            } else {
                recipeTable.add(recipe.input.buildTable(false, false, recipe.craftTime)).growX().right().pad(4f);
                recipeTable.image(Icon.right).padRight(4f).padLeft(4f);
                recipeTable.add(recipe.output.buildTable(false, false, recipe.craftTime, recipe.randomOutput)).growX().left().pad(4f);
                
                buttonContent.add(recipeTable).pad(4f).growX();
                
                if (block.hasAttribute() && recipe.attribute != null && block instanceof AttributeMultiCrafterBlock attributeBlock) {
                    Table attributeTable = new Table();
                    
                    float baseEfficiency = !Float.isNaN(recipe.baseEfficiency) ? recipe.baseEfficiency : attributeBlock.baseEfficiency;
                    attributeTable.add("[lightgray] " + (baseEfficiency <= 0.0001f ? Stat.tiles : Stat.affinities).localized() + ": []");
                    
                    float boostScale = !Float.isNaN(recipe.boostScale) ? recipe.boostScale : attributeBlock.boostScale;
                    StatValue statValue = StatValues.blocks(recipe.attribute, block.floating, boostScale * block.size * block.size, !attributeBlock.displayEfficiency);
                    statValue.display(attributeTable);
                    
                    buttonContent.row();
                    buttonContent.add(attributeTable).pad(4f).growX();
                }
                
                final int finalIndex = index;
                button.changed(() -> build.configure(finalIndex));
                button.setChecked(build.currentRecipeIndex == finalIndex);
            }
            button.setDisabled(!recipe.unlockedNow());
            button.add(buttonContent).pad(4f);
            
            buttonTable.add(button).pad(1.5f).grow();
            if (!large && index % 2 == 1) buttonTable.row();
            index++;
        }
        
        ScrollPane container = new ScrollPane(buttonTable, Styles.smallPane);
        container.setScrollingDisabled(true, false);
        container.setFadeScrollBars(false);
        
        table.add(container).growX().maxHeight(300f);
        
        table.layout();
        
        Element selected = buttonTable.getChildren().get(build.currentRecipeIndex);
        container.scrollTo(selected.x, selected.y, selected.getWidth(), selected.getHeight(), false, true);
        container.updateVisualScroll();
    }
}
