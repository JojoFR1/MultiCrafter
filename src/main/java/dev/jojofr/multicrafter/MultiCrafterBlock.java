package dev.jojofr.multicrafter;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.Button;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Strings;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import dev.jojofr.multicrafter.type.JsonRecipe;
import dev.jojofr.multicrafter.type.Recipe;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
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

/*
 *  - Item, liquid, power and heat input seems to work fine
 *  - Item, liquid, power and heat output seems to work fine
 *  - Configuration (aka. select menu) for recipes
 * TODO: there's a lot
 *  - Bars (the UI + update)
 *  - Multiple liquids bars
 *  - Support for payloads
 *  - Examples/Documentation
 */
/*
 TODO observations from a heat/power input/output test in one single block
  - the selection menu is too big with multiple recipes, maybe look into making it a scrollable container? (also need to test for big input/output too)
  - another small ui issue, the arrow for "input -> output" is not centered since the amount of input/output items can vary (and specify craft time)
 */
public class MultiCrafterBlock extends Block {
    public transient Seq<Recipe> recipes = new Seq<>();
    /** Only intended for internal use and JSON parsing */
    public Seq<JsonRecipe> jsonRecipes = new Seq<>();
    
    public int[] liquidOutputDirections = {-1};
    public boolean dumpExtraLiquid = true;
    public boolean ignoreLiquidFullness = false;
    
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
        for (JsonRecipe jsonRecipe : jsonRecipes) recipes.add(new Recipe(jsonRecipe, this));
        
        if (recipes.isEmpty()) {
            throw new IllegalStateException("The block "+ name +" does not have recipes! It must have at least one recipe.");
        }
        
        for (Recipe recipe : recipes) {
            if (recipe.hasItems()) hasItems = true;
            if (recipe.hasLiquids()) hasLiquids = true;
            if (recipe.hasPower()) hasPower = true;
            if (recipe.output.hasPower()) outputsPower = true;
            if (recipe.input.hasPower()) consumesPower = true;
            
            drawArrow = rotate = rotate || (recipe.output.hasHeat() || recipe.output.hasPayloads());
            rotateDraw = !rotate;
        }
        
        setupConsumers();
        
        super.init();
    }
    
    // TODO change it based of recipe?
    protected void setupConsumers() {
        boolean hasItems = false;
        boolean hasLiquids = false;
        boolean hasPower = false;
        
        for (Recipe recipe : recipes) {
            if (recipe.input.hasItems()) hasItems = true;
            if (recipe.input.hasLiquids()) hasLiquids = true;
            if (recipe.input.hasPower()) hasPower = true;
        }
        
        if (hasItems) {
            consume(new ConsumeItemDynamic(
                (MultiCrafterBuild build) -> build.currentRecipe == null ? ItemStack.empty : build.currentRecipe.input.items
            ));
        }
        
        if (hasLiquids) {
            consume(new ConsumeLiquidsDynamic(
                (MultiCrafterBuild build) -> build.currentRecipe == null ? LiquidStack.empty : build.currentRecipe.input.liquids
            ));
        }
        
        if (hasPower) {
            consume(new ConsumePowerDynamic(build ->
                ((MultiCrafterBuild) build).currentRecipe == null ? 0f : ((MultiCrafterBuild) build).currentRecipe.input.power) {
                @Override
                public float efficiency(Building build) {
                    MultiCrafterBuild multiCrafterBuild = (MultiCrafterBuild) build;
                    if (multiCrafterBuild.currentRecipe == null || multiCrafterBuild.currentRecipe.input.power <= 0f) {
                        return 1f;
                    }
                    
                    return super.efficiency(build);
                }
            });
        }
    }
    
    @Override public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) { drawer.drawPlan(this, plan, list); }
    @Override public void getRegionsToOutline(Seq<TextureRegion> out) { drawer.getRegionsToOutline(this, out); }
    @Override protected TextureRegion[] icons() { return drawer.finalIcons(this); }
    
    // TODO Payload support
    public class MultiCrafterBuild extends Building implements HeatBlock, HeatConsumer {
        public float progress;
        public float totalProgress;
        public float warmup;
        
        public float heat;
        public float outputHeat;
        public float[] sideHeat = new float[4];
        
        public Recipe currentRecipe;
        public int currentRecipeIndex;
        public Effect changeRecipeEffect = Fx.placeBlock;
        
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
            else heat = Mathf.approachDelta(heat, 0f, currentRecipe.warmupRate * delta());
            
            if (currentRecipe.output.hasHeat()) outputHeat = Mathf.approachDelta(outputHeat, currentRecipe.output.heat * efficiency, currentRecipe.warmupRate * delta());
            else outputHeat = Mathf.approachDelta(outputHeat, 0f, currentRecipe.warmupRate * delta());
            
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
            if (index == currentRecipeIndex) return;
            
            currentRecipeIndex = index;
            currentRecipe = recipes.get(index);
            progress = 0;
            changeRecipeEffect.at(x, y, block.size, block);
            
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
        public float heat() { return outputHeat; }
        
        @Override
        public float heatFrac() { return currentRecipe != null ? heat / Math.max(currentRecipe.input.heat, 0.01f) : 0f; }
        public float heatOutputFrac() { return currentRecipe != null ? outputHeat / Math.max(currentRecipe.output.heat, 0.01f) : 0f; }
        
        @Override
        public float[] sideHeat() { return sideHeat; }
        
        @Override
        public float heatRequirement() { return currentRecipe != null ? currentRecipe.input.heat : 0f; }
        
        @Override
        public float efficiencyScale() {
            if (currentRecipe == null) return 0f;
            if (!currentRecipe.input.hasHeat()) return super.efficiencyScale();
            
            float over = Math.max(heat - currentRecipe.input.heat, 0f);
            return Math.min(Mathf.clamp(heat / currentRecipe.input.heat) + over / currentRecipe.input.heat * currentRecipe.overheatScale, currentRecipe.maxEfficiency);
        }
        
        public float warmupTarget() {
            if (currentRecipe == null) return 0f;
            if (!currentRecipe.input.hasHeat()) return 1f;
            
            return Mathf.clamp(heat / currentRecipe.input.heat);
        }
        
        @Override
        public float getPowerProduction() {
            if (currentRecipe == null || !currentRecipe.output.hasPower()) return 0f;
            return currentRecipe.output.power * efficiency;
        }
        
        @Override
        public void buildConfiguration(Table table) {
            int index = 0;
            
            Table buttonTable = new Table();
            for (Recipe recipe : recipes) {
                Button button = new Button(Styles.togglet);

                Table recipeTable = new Table();
                if (!recipe.unlocked()) {
                    recipeTable.image(Icon.lock).pad(4f).fill().grow();
                    recipeTable.addListener(Tooltip.Tooltips.getInstance().create("@locked", Vars.mobile));
                } else {
                    recipeTable.add(recipe.input.buildTable(false)).pad(4f);
                    recipeTable.image(Icon.right);
                    recipeTable.add(recipe.output.buildTable(false)).pad(4f);

                    final int finalIndex = index;
                    button.changed(() -> configure(finalIndex));
                    button.update(() -> button.setChecked(currentRecipeIndex == finalIndex));
                }
                button.setDisabled(!recipe.unlocked());
                button.add(recipeTable).pad(4f);

                buttonTable.add(button).pad(4f).margin(10f).grow();
                buttonTable.row();
                index++;
            }
            
            table.add(buttonTable);
        }
        
        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(warmup);
            write.f(heat);
            write.f(outputHeat);
            
            write.i(currentRecipeIndex);
        }
        
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            progress = read.f();
            warmup = read.f();
            heat = read.f();
            outputHeat = read.f();
            
            currentRecipeIndex = Mathf.clamp(read.i(), 0, recipes.size - 1);
            currentRecipe = recipes.get(currentRecipeIndex);
        }
    }
    
    @Override
    public void setBars() {
        super.setBars();
        
        if (hasPower)
            addBar("power", (MultiCrafterBuild b) -> new Bar(
                b.currentRecipe.output.hasPower() ? Core.bundle.format("bar.poweroutput", Strings.fixed(b.getPowerProduction() * 60f * b.timeScale(), 1)) : "bar.power",
                Pal.powerBar,
                () -> b.efficiency
            ));
        
        if (recipes.contains(recipe -> recipe.input.hasHeat()))
            addBar("heat", (MultiCrafterBuild b) -> new Bar(
                Core.bundle.format("bar.heatpercent", (int) (b.heat + 0.01f), (int) (b.efficiencyScale() * 100 + 0.01f)),
                Pal.lightOrange,
                b::heatFrac
            ));
        if (recipes.contains(recipe -> recipe.output.hasHeat()))
            addBar("heat-output", (MultiCrafterBuild b) -> new Bar(
                "bar.heat",
                Pal.lightOrange,
                b::heatOutputFrac
            ));
        
        addBar("progress", (MultiCrafterBuild b) -> new Bar(
            "bar.loadprogress",
            Pal.accent,
            b::progress
        ));
    }
    
    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.output, table -> {
            table.row();
            
            for (Recipe recipe : recipes) {
                table.add(recipe.buildTable()).pad(4f).grow();
                table.row();
            }
            
            table.row();
            table.defaults().grow();
        });
    }
}
