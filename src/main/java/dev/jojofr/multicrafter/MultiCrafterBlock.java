package dev.jojofr.multicrafter;

import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.Seq;
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
import mindustry.world.draw.DrawHeatOutput;
import mindustry.world.draw.DrawMulti;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;

/*
 *  - Item, liquid and power input seems to work fine
 *  - Item and liquid output seems to work fine
 * TODO: there's a lot
 *  - Bars (the UI + update)
 *  - Configuration (aka. select menu) for recipes
 *  - Multiple liquids bars
 *  - Support for power output
 *  - Support for power input AND output
 *  - Support for heat input AND output
 *  - Fix known bug that produce "infinite" heat when two multicrafter are chained
 *      I could either reuse the fix I made for the OG library that or maybe separate input/output (all in one variable)
 *  - Support for payload input
 *  - Support for payload output
 *  - Support JSON (JS support will be dropped)
 *  - Examples/Documentation
 */
public class MultiCrafterBlock extends Block {
    public Seq<Recipe> recipes = new Seq<>();
    
    public int[] liquidOutputDirections = {-1};
    public boolean dumpExtraLiquid = true;
    public boolean ignoreLiquidFullness = false;
    
    // TODO temporary
    public DrawBlock drawer = new DrawMulti(new DrawDefault(), new DrawHeatOutput());
    
    public MultiCrafterBlock(String name) {
        super(name);
        
        update = true;
        solid = true;
        sync = true;
        configurable = true;
        
        // TODO temporary
        rotateDraw = false;
        rotate = true;
        drawArrow = true;
        
        ambientSound = Sounds.loopMachine;
        ambientSoundVolume = 0.03f;
        
        flags = EnumSet.of(BlockFlag.factory);
        // drawArrow = false;
        
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
        
        for (Recipe recipe : recipes) {
            if (recipe.hasItems()) hasItems = true;
            if (recipe.hasLiquids()) hasLiquids = true;
            if (recipe.hasPower()) hasPower = true;
            if (recipe.output.hasPower()) outputsPower = true;
            if (recipe.input.hasPower()) consumesPower = true;
        }
        
        setupConsumers();
        
        super.init();
    }
    
    // TODO change it based of recipe?
    protected void setupConsumers() {
        boolean hasItems = false;
        boolean hasLiquids = false;
        boolean hasPower = false;
        boolean outputsPower = false;
        
        for (Recipe recipe : recipes) {
            if (recipe.input.hasItems()) hasItems = true;
            if (recipe.input.hasLiquids()) hasLiquids = true;
            if (recipe.input.hasPower()) hasPower = true;
            if (recipe.output.hasPower()) outputsPower = true;
        }
        
        if (hasItems) {
            consume(new ConsumeItemDynamic(
                (MultiCrafterBuild build) -> build.currentRecipe.input.items
            ));
        }
        
        if (hasLiquids) {
            consume(new ConsumeLiquidsDynamic(
                (MultiCrafterBuild build) -> build.currentRecipe.input.liquids
            ));
        }
        
        if (hasPower) {
            consume(new ConsumePowerDynamic(build ->
                ((MultiCrafterBuild) build).currentRecipe.input.power
            ));
        } else if (outputsPower) {
            consume(new ConsumePowerDynamic(build -> 0f));
        }
    }
    
    // TODO Payload support
    public class MultiCrafterBuild extends Building implements HeatBlock, HeatConsumer {
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
            
            if (currentRecipe.input.hasHeat()) heat = calculateHeat(sideHeat);
            if (currentRecipe.output.hasHeat()) heat = Mathf.approachDelta(heat, currentRecipe.output.heat * efficiency, currentRecipe.warmupRate * delta());
            
            if (efficiency > 0) {
                progress += getProgressIncrease(currentRecipe.craftTime);
                warmup = Mathf.approachDelta(warmup, warmupTarget(), currentRecipe.warmupSpeed);
                
                if (currentRecipe.output.hasLiquids()) {
                    float increase = getProgressIncrease(1f);
                    for (LiquidStack liquid : currentRecipe.output.liquids) {
                        handleLiquid(this, liquid.liquid, Math.min(liquid.amount * increase, liquidCapacity - liquids.get(liquid.liquid)));
                    }
                }
                
                if (wasVisible && Mathf.chance(currentRecipe.updateEffectChance)) {
                    currentRecipe.updateEffect.at(x + Mathf.range(size * currentRecipe.updateEffectSpread), y + Mathf.range(size * currentRecipe.updateEffectSpread));
                }
                
            } else warmup = Mathf.approachDelta(warmup, 0f, currentRecipe.warmupSpeed);
            
            totalProgress += warmup * Time.delta;
            if (progress >= 1f) {
                craft();
            }
            
            dumpOutputs();
        }
        
        public void craft() {
            consume();
            
            for (ItemStack output : currentRecipe.output.items) {
                for (int i = 0; i < output.amount; i++) {
                    offload(output.item);
                }
            }
            
            if (wasVisible) {
                currentRecipe.craftEffect.at(x, y);
            }
            
            progress %= 1f;
        }
        
        public void dumpOutputs() {
            if (currentRecipe == null) return;
            
            if (currentRecipe.output.hasItems() && timer(timerDump, dumpTime / timeScale)) {
                for (ItemStack output : currentRecipe.output.items) {
                    dump(output.item);
                }
            }
            
            if (currentRecipe.output.hasLiquids()) {
                for (int i = 0; i < currentRecipe.output.liquids.length; i++) {
                    int direction = liquidOutputDirections.length > i ? liquidOutputDirections[i] : -1;
                    dumpLiquid(currentRecipe.output.liquids[i].liquid, 2f, direction);
                }
            }
        }
        
        public float getProgressIncrease(float baseTime) {
            if (currentRecipe == null) return 0f;
            if (ignoreLiquidFullness) return super.getProgressIncrease(baseTime);
            
            float max = 1f;
            float scaling = 1f;
            if (currentRecipe.output.hasLiquids()) {
                max = 0f;
                for (LiquidStack liquid : currentRecipe.output.liquids) {
                    float value = (liquidCapacity - liquids.get(liquid.liquid) / (liquid.amount * edelta()));
                    scaling = Math.min(scaling, value);
                    max = Math.max(max, value);
                }
            }
            
            return super.getProgressIncrease(baseTime) * (dumpExtraLiquid ? Math.min(max, 1f) : scaling);
        }
        
        @Override
        public boolean shouldConsume() {
            if (currentRecipe == null) return false;
            
            for (ItemStack item : currentRecipe.output.items) {
                if (items.get(item.item) + item.amount > itemCapacity) {
                    return false;
                }
            }
            
            if (currentRecipe.input.hasHeat() && currentRecipe.input.heat > 0f && heat <= 0f) {
                return false;
            }
            
            return enabled;
        }
        
        @Override
        public float calculateHeat(float[] sideHeat) {
            return super.calculateHeat(sideHeat);
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
        public int getMaximumAccepted(Item item) {
            return itemCapacity;
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
        public float progress() {
            return Mathf.clamp(progress);
        }
        
        @Override
        public float totalProgress() { return totalProgress; }
        
        @Override
        public float warmup() { return warmup; }
        
        @Override
        public float heat() { return heat; }
        
        @Override
        public float heatFrac() { return currentRecipe != null ?  heat / Math.max(currentRecipe.input.heat, currentRecipe.output.heat) : 0f; }
        
        @Override
        public float[] sideHeat() { return sideHeat; }
        
        @Override
        public float heatRequirement() { return currentRecipe != null ? currentRecipe.input.heat : 0f; }
        
        @Override
        public float efficiencyScale() {
            if (currentRecipe == null) return 0f;
            
            float over = Math.max(heat - currentRecipe.input.heat, 0f);
            return Math.min(Mathf.clamp(heat / currentRecipe.input.heat) + over / currentRecipe.input.heat * currentRecipe.overheatScale, currentRecipe.maxEfficiency);
        }
        
        public float warmupTarget() {
            if (currentRecipe == null) return 0f;
            
            return Mathf.clamp(heat / currentRecipe.input.heat);
        }
        
        @Override
        public float getPowerProduction() {
            if (currentRecipe == null || !currentRecipe.output.hasPower()) return 0f;
            return currentRecipe.output.power * efficiency;
        }
        
        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(warmup);
            write.f(heat);
            
            write.i(currentRecipeIndex);
        }
        
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
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
       
        // TODO temporary
        addBar("heat", (MultiCrafterBuild entity) -> new Bar("bar.heat", Pal.lightOrange, () -> entity.heat / entity.currentRecipe.output.heat));
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
