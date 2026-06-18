package dev.jojofr.multicrafter.type;

import arc.Core;
import arc.math.Interp;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Time;
import dev.jojofr.multicrafter.MultiCrafterBlock;
import mindustry.content.Fx;
import mindustry.content.TechTree;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Effect;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.io.SaveVersion;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;

public class Recipe extends UnlockableContent {
    public final IOEntry input, output;
    public float weight = 1f;
    
    public float craftTime = 80f;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public float updateEffectChance = 0.04f;
    public float updateEffectSpread = 4f;
    public float warmupSpeed = 0.019f;
    
    /** [Heat Consumer] */
    public float warmupRate = 0.15f;
    /** [Heat Producer] After heat meets this requirement, excess heat will be scaled by this number. */
    public float overheatScale = 1f;
    /** [Heat Producer] Maximum possible efficiency after overheating. */
    public float maxEfficiency = 4f;
    
    // Attribute support
    public Attribute attribute = null;
    public float baseEfficiency = 1f;
    public float boostScale = 1f;
    public float maxBoost = 1f;
    public float minEfficiency = -1f;
    public float displayEfficiencyScale = 1f;
    public boolean displayEfficiency = true;
    public boolean scaleLiquidConsumption = false;
    
    public DrawBlock drawer = new DrawDefault();
    
    public Recipe(String name) { this(name, new IOEntry(), new IOEntry()); }
    public Recipe(String name, IOEntry input) { this(name, input, new IOEntry()); }
    public Recipe(String name, IOEntry input, IOEntry output) { this(name, input, output, 80f); }
    public Recipe(String name, IOEntry input, IOEntry output, float craftTime) {
        super(name);
        
        this.localizedName = Core.bundle.get(getContentTypeName() + "." + this.name + ".name", this.name);
        this.description = Core.bundle.getOrNull(getContentTypeName() + "." + this.name + ".description");
        this.details = Core.bundle.getOrNull(getContentTypeName() + "." + this.name + ".details");
        this.credit = Core.bundle.getOrNull(getContentTypeName() + "." + this.name + ".credit");
        
        this.input = input.removeDuplicate(name);
        this.output = output.removeDuplicate(name);
        this.craftTime = craftTime;
    }
    
    public Recipe(JsonRecipe jsonRecipe, Block owner) {
        this(prefixName(jsonRecipe.name, owner), jsonRecipe.input, jsonRecipe.output, jsonRecipe.craftTime);
        if (this.minfo == null) this.minfo = owner.minfo;
        
        this.weight = jsonRecipe.weight;
        
        if (this.localizedName == null || this.localizedName.isEmpty())
            this.localizedName = jsonRecipe.localizedName;
        this.craftEffect = jsonRecipe.craftEffect;
        this.updateEffect = jsonRecipe.updateEffect;
        this.updateEffectChance = jsonRecipe.updateEffectChance;
        this.updateEffectSpread = jsonRecipe.updateEffectSpread;
        this.warmupSpeed = jsonRecipe.warmupSpeed;
        this.warmupRate = jsonRecipe.warmupRate;
        this.overheatScale = jsonRecipe.overheatScale;
        this.maxEfficiency = jsonRecipe.maxEfficiency;
        
        this.attribute = jsonRecipe.attribute;
        this.baseEfficiency = jsonRecipe.baseEfficiency;
        this.boostScale = jsonRecipe.boostScale;
        this.maxBoost = jsonRecipe.maxBoost;
        this.minEfficiency = jsonRecipe.minEfficiency;
        this.displayEfficiencyScale = jsonRecipe.displayEfficiencyScale;
        this.displayEfficiency = jsonRecipe.displayEfficiency;
        this.scaleLiquidConsumption = jsonRecipe.scaleLiquidConsumption;
        
        this.unlocked = jsonRecipe.unlocked;
        this.alwaysUnlocked = jsonRecipe.alwaysUnlocked;
        
        if (jsonRecipe.research != null && jsonRecipe.research.parent != null) {
            String researchName = jsonRecipe.research.parent;
            
            TechTree.TechNode lastNode = TechTree.all.find(node -> node.content == this);
            if (lastNode != null) lastNode.remove();
            
            TechTree.TechNode parent = TechTree.all.find(techNode ->
                techNode.content.name.equals(researchName)
                || techNode.content.name.equals(this.minfo.mod.name +"-"+ researchName)
                || techNode.content.name.equals(SaveVersion.mapFallback(researchName))
            );
            
            if (parent == null) {
                Log.warn("Content '@' isn't in the tech tree, but '@' requires it.", researchName, this.name);
                return;
            }
            
            ItemStack[] requirements = jsonRecipe.research.requirements != null ? jsonRecipe.research.requirements
                                     : jsonRecipe.researchRequirements != null ? jsonRecipe.researchRequirements
                                     : researchRequirements();
            TechTree.TechNode node = new TechTree.TechNode(null, this, requirements);
            if (jsonRecipe.research.objectives != null && jsonRecipe.research.objectives.length > 0)
                node.objectives.addAll(jsonRecipe.research.objectives);
            
            if (!parent.children.contains(node)) parent.children.add(node);
            
            node.parent = parent;
            node.planet = parent.planet;
        }
        
        this.drawer = jsonRecipe.drawer;
    }
    
    @Override
    public void postInit() {
        if(databaseCategory == null || databaseCategory.isEmpty()) databaseCategory = getContentTypeName();
        if(databaseTag == null || databaseTag.isEmpty()) databaseTag = "default";
        
        databaseTabs.addAll(shownPlanets);
    }
    
    @Override
    public void loadIcon() {
        fullIcon =
            Core.atlas.find(fullOverride == null ? "" : fullOverride,
                Core.atlas.find(getContentTypeName() + "-" + name + "-full",
                    Core.atlas.find(name + "-full",
                        Core.atlas.find(name,
                            Core.atlas.find(getContentTypeName() + "-" + name,
                                Core.atlas.find(name + "1"))))));
        
        uiIcon = Core.atlas.find(getContentTypeName() + "-" + name + "-ui", fullIcon);
    }
    
    @Override
    public void setStats() {
        stats.add(Stat.output, table -> {
            table.row();
            boolean perSecond = Core.settings.getBool("multicrafter.show-per-second");
            table.check("Show per second? ", perSecond, b -> {
                Core.settings.put("multicrafter.show-per-second", b);
                stats.remove(Stat.output);
                setStats();
            });
            table.row();
            
            table.add(buildTable(null, false, perSecond)).pad(4f).grow();
            table.defaults().grow();
        });
    }
    
    public Table buildTable(Block block, boolean showAttribute, boolean perSecond) {
        Table recipeTable = new Table();
        recipeTable.setBackground(Tex.whiteui);
        recipeTable.setColor(Pal.darkerGray);
        
        if (!this.unlocked()) {
            recipeTable.setColor(Pal.darkestGray);
            recipeTable.image(Icon.lock).size(100f, 50f).pad(12f).fill();
            
            return recipeTable;
        }
        
        Cell<Table> inputTable = recipeTable.add(this.input.buildTable(perSecond, craftTime)).minWidth(80f).maxWidth(220f).pad(12f).fill();
        inputTable.left();
        
        // TODO not perfect
        Table time = new Table();
        final float[] dur = {0f};
        time.update(() -> {
            dur[0] += Time.delta;
            if (dur[0] >= this.craftTime) dur[0] = 0f;
        });
        
        Bar timeBar = new Bar(String.format("%.1f", this.craftTime / 60f) + "s",
            Pal.accent, () -> Interp.smooth.apply((Time.time % this.craftTime) / this.craftTime));
        time.add(timeBar).height(50f).width(250f);
        recipeTable.add(time).pad(12f);
        
        Cell<Table> outputCell = recipeTable.add(this.output.buildTable(perSecond, craftTime)).minWidth(80f).maxWidth(220f).pad(12f).fill();
        outputCell.right();
        
        if (showAttribute && this.hasAttribute()) {
            recipeTable.row();
            
            Table attributeTable = new Table();
            attributeTable.add("[lightgray] " + (baseEfficiency <= 0.0001f ? Stat.tiles : Stat.affinities).localized() + ":[] ").left();
            
            StatValue value = StatValues.blocks(attribute, block.floating, boostScale * block.size * block.size, !displayEfficiency);
            value.display(attributeTable);
            
            recipeTable.add(attributeTable).colspan(3).left().pad(12f);
        }
        
        return recipeTable;
    }
    
    public Recipe withWeight(float weight) {
        this.weight = weight;
        return this;
    }
    
    public Recipe withCraftTime(float craftTime) {
        this.craftTime = craftTime;
        return this;
    }
    
    public Recipe withCraftEffect(Effect craftEffect) {
        this.craftEffect = craftEffect;
        return this;
    }
    
    public Recipe withUpdateEffect(Effect updateEffect) {
        return withUpdateEffect(updateEffect, 0.04f, 4f);
    }
    public Recipe withUpdateEffect(Effect updateEffect, float chance) { return withUpdateEffect(updateEffect, chance, 4f); }
    public Recipe withUpdateEffect(Effect updateEffect, float chance, float spread) {
        this.updateEffect = updateEffect;
        this.updateEffectChance = chance;
        this.updateEffectSpread = spread;
        return this;
    }
    
    public Recipe withWarmupSpeed(float speed) {
        this.warmupSpeed = speed;
        return this;
    }
    
    public Recipe withOverheatScale(float scale) {
        this.overheatScale = scale;
        return this;
    }
    
    public Recipe withMaxEfficiency(float maxEfficiency) {
        this.maxEfficiency = maxEfficiency;
        return this;
    }
    
    public Recipe withWarmupRate(float warmupRate) {
        this.warmupRate = warmupRate;
        return this;
    }
    
    public Recipe withDrawer(DrawBlock drawer) {
        this.drawer = drawer;
        return this;
    }
    
    
    public Recipe isUnlocked() {
        this.unlocked = true;
        return this;
    }
    
    public Recipe isLocked() {
        this.unlocked = false;
        return this;
    }
    
    public Recipe isAlwaysUnlocked() {
        this.alwaysUnlocked = true;
        return this;
    }
    
    public Recipe isNotAlwaysUnlocked() {
        this.alwaysUnlocked = false;
        return this;
    }
    
    public Recipe withAttribute(Attribute attribute) { return withAttribute(attribute, 1f, 1f, 1f, -1f, 1f, true, false); }
    public Recipe withAttribute(Attribute attribute, float baseEfficiency) { return withAttribute(attribute, baseEfficiency, 1f, 1f, -1f, 1f, true, false); }
    public Recipe withAttribute(Attribute attribute, float baseEfficiency, float boostScale) { return withAttribute(attribute, baseEfficiency, boostScale, 1f, -1f, 1f, true, false); }
    public Recipe withAttribute(Attribute attribute, float baseEfficiency, float boostScale, float maxBoost) { return withAttribute(attribute, baseEfficiency, boostScale, maxBoost, -1f, 1f, true, false); }
    public Recipe withAttribute(Attribute attribute, float baseEfficiency, float boostScale, float maxBoost, float minEfficiency) { return withAttribute(attribute, baseEfficiency, boostScale, maxBoost, minEfficiency, 1f, true, false); }
    public Recipe withAttribute(Attribute attribute, float baseEfficiency, float boostScale, float maxBoost, float minEfficiency, float displayEfficiencyScale) { return withAttribute(attribute, baseEfficiency, boostScale, maxBoost, minEfficiency, displayEfficiencyScale, true, false); }
    public Recipe withAttribute(Attribute attribute, float baseEfficiency, float boostScale, float maxBoost, float minEfficiency, float displayEfficiencyScale, boolean displayEfficiency) { return withAttribute(attribute, baseEfficiency, boostScale, maxBoost, minEfficiency, displayEfficiencyScale, displayEfficiency, false); }
    public Recipe withAttribute(Attribute attribute, float baseEfficiency, float boostScale, float maxBoost,
                                float minEfficiency, float displayEfficiencyScale, boolean displayEfficiency, boolean scaleLiquidConsumption)
    {
        this.attribute = attribute;
        this.baseEfficiency = baseEfficiency;
        this.boostScale = boostScale;
        this.maxBoost = maxBoost;
        this.minEfficiency = minEfficiency;
        this.displayEfficiencyScale = displayEfficiencyScale;
        this.displayEfficiency = displayEfficiency;
        this.scaleLiquidConsumption = scaleLiquidConsumption;
        return this;
    }
    
    public Recipe withDrawer(DrawBlock drawer) {
        this.drawer = drawer;
        return this;
    }
    
    public boolean hasItems() {
        return input != null && input.hasItems() || output != null && output.hasItems();
    }
    
    public boolean hasLiquids() {
        return input != null && input.hasLiquids() || output != null && output.hasLiquids();
    }
    
    public boolean hasPower() {
        return input != null && input.hasPower() || output != null && output.hasPower();
    }
    
    public boolean hasHeat() {
        return input != null && input.hasHeat() || output != null && output.hasHeat();
    }
    
    public boolean hasPayloads() { return input != null && input.hasPayloads() || output != null && output.hasPayloads(); }
    
    public boolean hasAttribute() { return attribute != null; }
    
    public boolean hasInput(MultiCrafterBlock.MultiCrafterBuild building) {
        if (input.hasItems()) {
            for (ItemStack stack : input.items) if (building.items.get(stack.item) < stack.amount) return false;
        }
        
        if (input.hasLiquids()) {
            for (LiquidStack stack : input.liquids) if (building.liquids.get(stack.liquid) < stack.amount) return false;
        }
        
        if (input.hasHeat() && building.heat < input.heat) return false;
        
        return true;
    }
    
    @Override
    public ContentType getContentType() {
        return ContentType.typeid_UNUSED;
    }
    
    protected String getContentTypeName() {
        return "recipe";
    }
    
    private static String prefixName(String name, Block owner) {
        if (owner == null || owner.minfo == null || owner.minfo.mod == null) return name;
        
        String modName = owner.minfo.mod.name;
        String prefix = modName + "-";
        
        return name.startsWith(prefix) ? name : prefix + name;
    }
}
