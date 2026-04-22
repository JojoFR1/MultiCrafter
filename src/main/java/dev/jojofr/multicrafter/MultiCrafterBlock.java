package dev.jojofr.multicrafter;

import arc.math.Interp;
import arc.math.Mathf;
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
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.consumers.ConsumeLiquidsDynamic;
import mindustry.world.consumers.ConsumePowerDynamic;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;

public class MultiCrafterBlock extends Block {
    public int[] liquidOutputDirections = {-1};
    
    public boolean dumpExtraLiquid = true;
    public boolean ignoreLiquidFullness = false;
    
    public Seq<Recipe> recipes = new Seq<>();
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
        
        config(Integer.class, MultiCrafterBuild::setCurrentRecipe);
    }
    
    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }
    
    @Override
    public void init() {
        if (recipes.isEmpty()) {
            throw new IllegalStateException("MultiCrafterBlock " + name + " must have at least one recipe");
        }
        setupConsumers();
        
        super.init();
    }
    
    protected void setupConsumers() {
        consume(new ConsumeItemDynamic(
            (MultiCrafterBuild build) -> build.currentRecipe.input.items
        ));
        
        consume(new ConsumeLiquidsDynamic(
            (MultiCrafterBuild build) -> build.currentRecipe.input.liquids
        ));
        
        consume(new ConsumePowerDynamic(build ->
            ((MultiCrafterBuild) build).currentRecipe.input.power
        ));
    }
    
    // TODO Payload support
    public class MultiCrafterBuild extends Building implements HeatBlock, HeatConsumer {
        public float craftingTime;
        public float progress;
        public float totalProgress;
        public float warmup;
        
        public float heat;
        public float[] sideHeat = new float[4];
        
        public Recipe currentRecipe;
        public int currentRecipeIndex;
        
        @Override
        public void created() {
            super.created();
            
            this.currentRecipeIndex = 0;
            this.currentRecipe = recipes.get(0);
        }
        
        @Override
        public void updateTile() {
            if (currentRecipe == null) return;
            
            if (currentRecipe.craftTime > 0) craftingTime += Time.delta;
            
            warmup = Mathf.approachDelta(warmup, 0f, currentRecipe.warmupSpeed);
            totalProgress += warmup * Time.delta;
            
            if (craftingTime >= currentRecipe.craftTime) {
                consume();
                
                for (ItemStack output : currentRecipe.output.items) {
                    for (int i = 0; i < output.amount; i++) {
                        offload(output.item);
                    }
                }
                
                craftingTime = 0;
            }
            
            if (timer(timerDump, dumpTime / timeScale)) {
                for (ItemStack output : currentRecipe.output.items) {
                    dump(output.item);
                }
            }
            
            for (int i = 0; i < currentRecipe.output.liquids.length; i++) {
                int direction = liquidOutputDirections[i % liquidOutputDirections.length];
                if (direction == -1) continue;
                
                LiquidStack output = currentRecipe.output.liquids[i];
                dumpLiquid(output.liquid, output.amount, direction);
            }
        }
        
        @Override
        public boolean shouldConsume() {
            return super.shouldConsume();
        }
        
        @Override
        public boolean acceptItem(Building source, Item item) {
            return currentRecipe != null && currentRecipe.input.hasItems() && currentRecipe.input.acceptItem(item) && items.get(item) < itemCapacity;
        }
        
        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return currentRecipe != null && currentRecipe.input.hasLiquids() && currentRecipe.input.acceptLiquid(liquid) && liquids.get(liquid) < liquidCapacity;
        }
        
        // TODO capacity
        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            return currentRecipe != null && currentRecipe.input.hasPayloads() && currentRecipe.input.acceptPayload(payload);
        }
        
        @Override
        public void draw() { drawer.draw(this); }
        
        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }
        
        protected void setCurrentRecipe(int index) {
            this.currentRecipeIndex = index;
            this.currentRecipe = recipes.get(index);
            
            // TODO does not work
            // this.block.removeConsumers(c -> true);
            // setupConsumers();
            // reinitializeConsumers();
        }
        
        @Override
        public float warmup() { return warmup; }
        
        @Override
        public float totalProgress() { return totalProgress; }
        
        @Override
        public float heat() { return heat; }
        
        @Override
        public float heatFrac() { return currentRecipe != null ?  heat / Math.max(currentRecipe.input.heat, currentRecipe.output.heat) : 0f; }
        
        @Override
        public float[] sideHeat() { return sideHeat; }
        
        @Override
        public float heatRequirement() { return currentRecipe != null ? currentRecipe.input.heat : 0f; }
        
        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(craftingTime);
            write.f(progress);
            write.f(warmup);
            write.f(heat);
            
            write.i(currentRecipeIndex);
        }
        
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            craftingTime = read.f();
            progress = read.f();
            warmup = read.f();
            heat = read.f();
            
            currentRecipeIndex = Mathf.clamp(read.i(), 0, recipes.size - 1);
            currentRecipe = recipes.get(currentRecipeIndex);
        }
    }
    
    // TODO
    @Override
    public void setBars() {
        super.setBars();
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
                
                // TODO not perfect
                Table time = new Table();
                Bar timeBar = new Bar(String.format("%.1f", recipe.craftTime / 60f) + "s", Pal.accent, () -> Interp.smooth.apply((Time.time % recipe.craftTime) / recipe.craftTime));
                time.add(timeBar).height(50).width(250);
                recipeTable.add(time).pad(12);
                
                Cell<Table> outputCell = recipeTable.add(recipe.output.buildTable());
                outputCell.right();
                
                table.add(recipeTable).pad(10).grow();
                table.row();
            }
            
            table.row();
            table.defaults().grow();
        });
    }
}
