package dev.jojofr.multicrafter;

import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Nullable;
import dev.jojofr.multicrafter.type.Recipe;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;

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
    }
}
