package dev.jojofr.multicrafter;

import arc.math.Interp;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import dev.jojofr.multicrafter.type.Recipe;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;

public class MultiCrafterBlock extends Block {
    public int[] liquidOutputDirections = {-1};
    
    public boolean dumpExtraLiquid = true;
    public boolean ignoreLiquidFullness = false;
    
    Seq<Recipe> recipes = new Seq<>();
    // Recipe dependant
    public @Nullable ItemStack outputItem;
    public @Nullable ItemStack[] outputItems;
    
    public @Nullable LiquidStack outputLiquid;
    public @Nullable LiquidStack[] outputLiquids;
    
    public DrawBlock drawer = new DrawDefault();
    
    public MultiCrafterBlock(String name) {
        super(name);
        
        update = true;
        solid = true;
        sync = true;
        configurable = true;
        
        ambientSound = Sounds.loopMachine;
        ambientSoundVolume = 0.03f;
        
        flags = EnumSet.of(BlockFlag.factory);
        drawArrow = false;
        requirements(Category.crafting, ItemStack.empty);
    }
    
    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }
    
    @Override
    public void init() {
        super.init();
    }
    
    public class MultiCrafterBuild extends Building {
        public float progress;
        public float totalProgress;
        public float warmup;
        
        @Override
        public void draw() { drawer.draw(this); }
        
        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }
        
        @Override
        public float warmup() { return warmup; }
        
        @Override
        public float totalProgress() { return totalProgress; }
        
        
        @Override
        public void write(Writes write) {
            super.write(write);
        }
        
        @Override
        public void read(Reads read) {
            super.read(read);
        }
    }
    
    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.output, table -> {
            table.row();
            
            for (Recipe recipe : recipes) {
                Table recipeTable = new Table();
                recipeTable.setBackground(Tex.whiteui);
                recipeTable.setColor(Pal.darkerGray);
                
                Cell<Table> inputTable = recipeTable.add(recipe.input.buildTable());
                inputTable.left();
                
                Table time = new Table();
                Bar timeBar = new Bar(String.format("%.1f", recipe.craftTime / 60f) + "s", Pal.accent, () -> Interp.smooth.apply((Time.time % recipe.craftTime) / recipe.craftTime));
                time.add(timeBar).height(50).width(250);
                recipeTable.add(time).pad(12);
                
                Cell<Table> outputCell = recipeTable.add(recipe.output.buildTable());
                outputCell.right();
                
                table.add(recipeTable);
                table.row();
            }
            
            table.row();
            table.defaults().grow();
        });
    }
}
