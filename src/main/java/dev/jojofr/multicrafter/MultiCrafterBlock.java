package dev.jojofr.multicrafter;

import arc.Core;
import arc.func.Func;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.Button;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import dev.jojofr.multicrafter.type.JsonRecipe;
import dev.jojofr.multicrafter.type.Recipe;
import dev.jojofr.multicrafter.world.AttributeMultiCrafterBlock;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.core.UI;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.*;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadBlock;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.consumers.ConsumeLiquidsDynamic;
import mindustry.world.consumers.ConsumePayloadDynamic;
import mindustry.world.consumers.ConsumePowerDynamic;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;

// TODO improve the selection menu
public class MultiCrafterBlock extends Block {
    public transient Seq<Recipe> recipes = new Seq<>();
    /** Only intended for internal use and JSON parsing */
    public Seq<JsonRecipe> jsonRecipes = new Seq<>();
    public boolean autoSelectRecipe = false;
    
    public int[] liquidOutputDirections = {-1};
    public boolean dumpExtraLiquid = true;
    public boolean ignoreLiquidFullness = false;
    
    public float payloadSpeed = 0.7f;
    public float payloadRotateSpeed = 5f;
    
    public String regionSuffix = "";
    public TextureRegion topRegion, outRegion, inRegion;
    
    public boolean hasRandomOutputRecipes = false;
    
    public DrawBlock drawer = new DrawDefault();
    
    private final OrderedMap<String, Bar> liquidBarMap = new OrderedMap<>();
    
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
        
        config(Integer.class, (build, value) -> ((MultiCrafterBuild) build).setCurrentRecipe(value));
    }
    
    @Override
    public void load() {
        super.load();
        drawer.load(this);
        
        topRegion = findFactoryRegion("-top");
        outRegion =  findFactoryRegion("-out");
        inRegion =  findFactoryRegion("-in");
    }
    
    @Override
    public void init() {
        for (JsonRecipe jsonRecipe : jsonRecipes) recipes.add(jsonRecipe.build(this));
        
        if (recipes.isEmpty()) {
            throw new IllegalStateException("The block "+ name +" does not have recipes! It must have at least one recipe.");
        }
        
        configurable = !autoSelectRecipe;
        
        for (Recipe recipe : recipes) {
            if (recipe.hasItems()) hasItems = true;
            if (recipe.hasLiquids()) hasLiquids = true;
            if (recipe.hasPower()) hasPower = true;
            
            if (recipe.input.hasPower()) consumesPower = true;
            if (recipe.input.hasPayloads()) acceptsPayload = true;
            
            if (recipe.output.hasLiquids()) outputsLiquid = true;
            if (recipe.output.hasPower()) outputsPower = true;
            if (recipe.output.hasPayloads()) outputsPayload = true;
            
            if (recipe.randomOutput) hasRandomOutputRecipes = true;
            
            drawArrow = rotate = rotate || (recipe.output.hasHeat() || recipe.output.hasPayloads());
            rotateDraw = !rotate;
        }
        
        setupConsumers();
        
        super.init();
    }
    
    protected void setupConsumers() {
        boolean consumeItems = false;
        boolean consumeLiquids = false;
        boolean consumePower = false;
        boolean consumePayloads = false;
        
        for (Recipe recipe : recipes) {
            if (recipe.input.hasItems()) consumeItems = true;
            if (recipe.input.hasLiquids()) consumeLiquids = true;
            if (recipe.input.hasPower()) consumePower = true;
            if (recipe.input.hasPayloads()) consumePayloads = true;
            
            if (consumeItems && consumeLiquids && consumePower && consumePayloads) break;
        }
        
        if (consumeItems) {
            consume(new ConsumeItemDynamic(
                (MultiCrafterBuild build) -> build.currentRecipe == null ? ItemStack.empty : build.currentRecipe.input.items
            ));
        }
        
        if (consumeLiquids) {
            consume(new ConsumeLiquidsDynamic(
                (MultiCrafterBuild build) -> build.currentRecipe == null ? LiquidStack.empty : build.currentRecipe.input.liquids
            ));
        }
        
        if (consumePower) {
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
        
        if (consumePayloads) {
            consume(new ConsumePayloadDynamic((MultiCrafterBuild build) -> build.currentRecipe == null ? new Seq<>() : build.currentRecipe.input.payloads));
        }
    }
    
    public static void pushOutput(Payload payload, float progress){
        float thresh = 0.55f;
        if(progress >= thresh){
            boolean legStep = payload instanceof UnitPayload u && u.unit.type.allowLegStep;
            float size = payload.size(), radius = size/2f, x = payload.x(), y = payload.y(), scl = Mathf.clamp(((progress - thresh) / (1f - thresh)) * 1.1f);
            
            Groups.unit.intersect(x - size/2f, y - size/2f, size, size, u -> {
                float dst = u.dst(payload);
                float rs = radius + u.hitSize/2f;
                if(u.isGrounded() && u.type.allowLegStep == legStep && dst < rs){
                    u.vel.add(Tmp.v1.set(u.x - x, u.y - y).setLength(Math.min(rs - dst, 1f)).scl(scl));
                }
            });
        }
    }
    
    @Override public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) { drawer.drawPlan(this, plan, list); }
    @Override public void getRegionsToOutline(Seq<TextureRegion> out) { drawer.getRegionsToOutline(this, out); }
    @Override protected TextureRegion[] icons() { return drawer.finalIcons(this); }
    
    // From Mindustry PayloadBlock
    protected TextureRegion findFactoryRegion(String suf){
        TextureRegion region = Core.atlas.find(name + suf);
        
        if(!region.found() && minfo.mod != null) region = Core.atlas.find(minfo.mod.name + "-factory" + suf + "-" + size + regionSuffix);
        if(!region.found()) region = Core.atlas.find("factory" + suf + "-" + size + regionSuffix);
        
        return region;
    }
    
    public class MultiCrafterBuild extends Building implements HeatBlock, HeatConsumer {
        public Recipe currentRecipe;
        public int currentRecipeIndex;
        
        public float progress;
        public float totalProgress;
        public float warmup;
        
        public float heat;
        public float outputHeat;
        public float[] sideHeat = new float[4];
        
        public @Nullable Payload payload;
        public PayloadSeq payloadInput = new PayloadSeq();
        public PayloadSeq payloadOutput = new PayloadSeq();
        public boolean payloadOutgoing;
        
        public Vec2 payVector = new Vec2();
        public float payRotation;
        public boolean carried;
        
        public Effect changeRecipeEffect = Fx.placeBlock;
        
        public int seed;
        
        @Override
        public void created() {
            this.currentRecipeIndex = 0;
            this.currentRecipe = recipes.get(0);
            
            if (hasRandomOutputRecipes) this.seed = Mathf.randomSeed(tile.pos(), 0, Integer.MAX_VALUE - 1);
        }
        
        @Override
        public void updateTile() {
            if (autoSelectRecipe && (efficiency <= 0f || progress <= 0f)) {
                Recipe autoRecipe = currentAutoRecipe();
                if (autoRecipe != null && autoRecipe != currentRecipe) setCurrentRecipe(autoRecipe, true);
            }
            
            if (payload != null) {
                if (payloadOutgoing) moveOutPayload();
                else if (moveInPayload()) {
                    payloadInput.add(payload.content(), 1);
                    payload = null;
                }
            } else if (currentRecipe != null && currentRecipe.output.hasPayloads() && payloadOutput.any()) {
                spawnOutputPayload();
            }
            
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
            Item randomItem = null;
            if (currentRecipe.randomOutput) {
                int sum = 0;
                for (ItemStack item : currentRecipe.output.items) sum += item.amount;
                
                int i = Mathf.randomSeed(seed++, 0, sum - 1);
                int count = 0;
                
                for (ItemStack stack : currentRecipe.output.items) {
                    if (i >= count && i < count + stack.amount) {
                        randomItem = stack.item;
                        break;
                    }
                    count += stack.amount;
                }
            }
            
            consume();
            
            if (randomItem != null && items.get(randomItem) < itemCapacity) {
                offload(randomItem);
                return;
            }
            
            
            for (ItemStack output : currentRecipe.output.items) {
                for (int i = 0; i < output.amount; i++) {
                    offload(output.item);
                }
            }
            
            if (currentRecipe.output.hasPayloads())
                for (PayloadStack stack : currentRecipe.output.payloads) {
                    payloadOutput.add(stack.item, stack.amount);
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
                    float value = (liquidCapacity - liquids.get(liquid.liquid)) / (liquid.amount * edelta());
                    scaling = Math.min(scaling, value);
                    max = Math.max(max, value);
                }
            }
            
            return super.getProgressIncrease(baseTime) * (dumpExtraLiquid ? Math.min(max, 1f) : scaling);
        }
        
        @Override
        public float efficiencyScale() {
            if (currentRecipe == null) return 0f;
            if (!currentRecipe.input.hasHeat()) return super.efficiencyScale();
            
            float over = Math.max(heat - currentRecipe.input.heat, 0f);
            return Math.min(Mathf.clamp(heat / currentRecipe.input.heat) + over / currentRecipe.input.heat * currentRecipe.overheatScale, currentRecipe.maxEfficiency);
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
        public void pickedUp() {
            carried = true;
        }
        
        @Override
        public void drawTeamTop() {
            carried = false;
        }
        
        @Override
        public Payload takePayload() {
            Payload t = payload;
            payload = null;
            return t;
        }
        
        @Override
        public void onRemoved() {
            super.onRemoved();
            if (payload != null && !carried) payload.dump();
        }
        
        @Override
        public void onDestroyed() {
            if (payload != null) payload.destroyed();
            super.onDestroyed();
        }
        
        public Recipe currentAutoRecipe() {
            Recipe bestRecipe = null;
            float bestWeight = Float.NEGATIVE_INFINITY;
            
            outer:
            for (Recipe recipe : recipes) {
                if (!recipe.unlockedNow()) continue;
                
                if (recipe.input.hasItems()) for (ItemStack item : recipe.input.items)
                    if (items.get(item.item) < item.amount) continue outer;
                
                if (recipe.input.hasLiquids()) for (LiquidStack liquid : recipe.input.liquids)
                    if (liquids.get(liquid.liquid) < liquid.amount) continue outer;
                
                if (recipe.input.hasPower() && power.status < 0.99f) continue;
                if (recipe.input.hasHeat() && heat < recipe.input.heat) continue;
                
                if (recipe.weight > bestWeight) {
                    bestRecipe = recipe;
                    bestWeight = recipe.weight;
                }
            }
            
            return bestRecipe;
        }
        
        @Override
        public float calculateHeat(float[] sideHeat) {
            return super.calculateHeat(sideHeat);
        }
        
        @Override
        public void handlePayload(Building source, Payload payload) {
            this.payload = payload;
            this.payloadOutgoing = false;
            this.payVector.set(source).sub(this).clamp(-size * Vars.tilesize / 2f, -size * Vars.tilesize / 2f, size * Vars.tilesize / 2f, size * Vars.tilesize / 2f);
            this.payRotation = payload.rotation();
            
            updatePayload();
        }
        
        // TODO handle? give user choice?
        public boolean acceptUnitPayload(Unit unit) {
            return false;
        }
        
        @Override
        public boolean canControlSelect(Unit unit) {
            return !unit.spawnedByCore && unit.type.allowedInPayloads && this.payload == null && acceptUnitPayload(unit) && unit.tileOn() != null && unit.tileOn().build == this;
        }
        
        @Override
        public void onControlSelect(Unit player) {
            float x = player.x;
            float y = player.y;
            handleUnitPayload(player, p -> payload = p);
            this.payVector.set(x, y).sub(this).clamp(-size * Vars.tilesize / 2f, -size * Vars.tilesize / 2f, size * Vars.tilesize / 2f, size * Vars.tilesize / 2f);
            this.payRotation = player.rotation();
        }
        
        @Override
        public boolean acceptItem(Building source, Item item) {
            if (autoSelectRecipe) {
                boolean valid = false;
                for (Recipe recipe : recipes) {
                    if (recipe.unlockedNow() && recipe.input.hasItems() && recipe.input.acceptItem(item)) {
                        valid = true;
                        break;
                    }
                }
                
                return valid && items.get(item) < itemCapacity;
            }
            
            return currentRecipe != null && currentRecipe.input.hasItems() && currentRecipe.input.acceptItem(item) && items.get(item) < itemCapacity;
        }
        
        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if (autoSelectRecipe) {
                boolean valid = false;
                for (Recipe recipe : recipes) {
                    if (recipe.unlockedNow() && recipe.input.hasLiquids() && recipe.input.acceptLiquid(liquid)) {
                        valid = true;
                        break;
                    }
                }
                
                return valid && liquids.get(liquid) < liquidCapacity;
            }
            
            return currentRecipe != null && currentRecipe.input.hasLiquids() && currentRecipe.input.acceptLiquid(liquid) && liquids.get(liquid) < liquidCapacity;
        }
        
        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            if (autoSelectRecipe) {
                boolean valid = false;
                int payloadCapacity = 0;
                for (Recipe recipe : recipes) {
                    if (recipe.unlockedNow() && recipe.input.hasPayloads() && recipe.input.acceptPayload(payload)) {
                        valid = true;
                        payloadCapacity = recipe.input.getPayloadRequirements(payload);
                        break;
                    }
                }
                
                return valid && payloadInput.get(payload.content()) < payloadCapacity;
            }
            
            return currentRecipe != null && currentRecipe.input.hasPayloads() && currentRecipe.input.acceptPayload(payload) && payloadInput.get(payload.content()) < currentRecipe.input.getPayloadRequirements(payload);
        }
        
        @Override
        public int getMaximumAccepted(Item item) {
            return itemCapacity;
        }
        
        @Override
        public void draw() {
            drawer.draw(this);
            
            // Draw payload input conveyors
            if (currentRecipe != null && currentRecipe.hasPayloads()) {
                for (int i = 0; i < 4; i++) {
                    if (blends(i) && i != rotation) {
                        if (currentRecipe.input.hasPayloads()) Draw.rect(inRegion, x, y, (i * 90) - 180);
                        if (currentRecipe.output.hasPayloads()) Draw.rect(outRegion, x, y, (i * 90) - 180);
                    }
                }
                Draw.z(Layer.blockOver);
                
                payRotation = rotdeg();
                drawPayload();
                
                Draw.z(Layer.blockOver + 0.1f);
                
                Draw.rect(topRegion, x, y);
            }
        }
        
        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }
        
        protected void setCurrentRecipe(int index) { setCurrentRecipe(index, true); }
        protected void setCurrentRecipe(int index, boolean showEffect) {
            if (index == currentRecipeIndex) return;
            
            currentRecipeIndex = index;
            setCurrentRecipe(recipes.get(index), showEffect);
        }
        protected void setCurrentRecipe(Recipe recipe, boolean showEffect) {
            currentRecipe = recipe;
            
            progress = 0f;
            if (showEffect) changeRecipeEffect.at(x, y, block.size, block);
            
            Vars.ui.hudfrag.blockfrag.rebuild();
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
        
        public float warmupTarget() {
            if (currentRecipe == null) return 0f;
            if (!currentRecipe.input.hasHeat()) return 1f;
            
            return Mathf.clamp(heat / currentRecipe.input.heat);
        }
        
        @Override
        public PayloadSeq getPayloads() {
            return payloadInput;
        }
        
        public boolean blends(int direction){
            return PayloadBlock.blends(this, direction);
        }
        
        public void updatePayload(){
            if(payload != null){
                payload.set(x + payVector.x, y + payVector.y, payRotation);
            }
        }
        
        /** @return true if the payload is in position. */
        public boolean moveInPayload() {
            return moveInPayload(true);
        }
        /** @return true if the payload is in position. */
        public boolean moveInPayload(boolean rotate) {
            if(payload == null) return false;
            
            updatePayload();
            
            if(rotate) payRotation = Angles.moveToward(payRotation, block.rotate ? rotdeg() : 90f, payloadRotateSpeed * delta());
            payVector.approach(Vec2.ZERO, payloadSpeed * delta());
            
            return hasArrived();
        }
        
        public void moveOutPayload() {
            if(payload == null) return;
            
            updatePayload();
            
            Vec2 dest = Tmp.v1.trns(rotdeg(), size * Vars.tilesize/2f);
            
            payRotation = Angles.moveToward(payRotation, rotdeg(), payloadRotateSpeed * delta());
            payVector.approach(dest, payloadSpeed * delta());
            
            Building front = front();
            boolean canDump = front == null || !front.tile.solid();
            boolean canMove = front != null && (front.block.outputsPayload || front.block.acceptsPayload);
            
            if(canDump && !canMove) pushOutput(payload, 1f - (payVector.dst(dest) / (size * Vars.tilesize / 2f)));
            
            if(payVector.within(dest, 0.001f)) {
                payVector.clamp(-size * Vars.tilesize / 2f, -size * Vars.tilesize / 2f, size * Vars.tilesize / 2f, size * Vars.tilesize / 2f);
                
                if(canMove) if(movePayload(payload)) payload = null;
                else if(canDump) dumpPayload();
            }
        }
        
        public void spawnOutputPayload() {
            for (PayloadStack stack : currentRecipe.output.payloads) {
                if (payloadOutput.get(stack.item) > 0) {
                    Payload created = stack.item instanceof Block b ? new BuildPayload(b, team) :
                                        stack.item instanceof UnitType u ? new UnitPayload(u.create(team)) : null;
                    if (created == null) continue;
                    
                    created.set(x, y, rotdeg());
                    this.payload = created;
                    this.payloadOutgoing = true;
                    this.payVector.set(0f, 0f);
                    this.payRotation = rotdeg();
                    payloadOutput.remove(stack.item, 1);
                    break;
                }
            }
        }
        
        public void dumpPayload(){
            //translate payload forward slightly
            float tx = Angles.trnsx(payload.rotation(), 0.1f), ty = Angles.trnsy(payload.rotation(), 0.1f);
            payload.set(payload.x() + tx, payload.y() + ty, payload.rotation());
            
            if(payload.dump()){
                payload = null;
            }else{
                payload.set(payload.x() - tx, payload.y() - ty, payload.rotation());
            }
        }
        
        public boolean hasArrived(){
            return payVector.isZero(0.01f);
        }
        
        public void drawPayload(){
            if(payload != null){
                updatePayload();
                
                Draw.z(Layer.blockOver);
                payload.draw();
            }
        }
        
        @Override
        public float getPowerProduction() {
            if (currentRecipe == null || !currentRecipe.output.hasPower()) return 0f;
            return currentRecipe.output.power * efficiency;
        }
        
        @Override
        public void buildConfiguration(Table table) {
            if (autoSelectRecipe) return;
            int index = 0;
            
            Table buttonTable = new Table();
            for (Recipe recipe : recipes) {
                Button button = new Button(Styles.togglet);
                Table buttonContent = new Table();
                
                Table recipeTable = new Table();
                if (!recipe.unlockedNow()) {
                    recipeTable.image(Icon.lock).pad(4f).fill().grow();
                    recipeTable.addListener(Tooltip.Tooltips.getInstance().create("@locked", Vars.mobile));
                    
                    buttonContent.add(recipeTable).pad(4f).growX();
                } else {
                    recipeTable.add(recipe.input.buildTable(false, false, currentRecipe.craftTime)).pad(4f);
                    recipeTable.image(Icon.right);
                    if (recipe.randomOutput) recipeTable.add(recipe.output.buildTableRandom(false, true, currentRecipe.craftTime)).pad(4f);
                    else recipeTable.add(recipe.output.buildTable(false, false, currentRecipe.craftTime)).pad(4f);
                    
                    buttonContent.add(recipeTable).pad(4f).growX();
                    
                    if (hasAttribute() && recipe.attribute != null && block instanceof AttributeMultiCrafterBlock attributeBlock) {
                        Table attributeTable = new Table();
                        
                        float baseEfficiency = !Float.isNaN(recipe.baseEfficiency) ? recipe.baseEfficiency : attributeBlock.baseEfficiency;
                        attributeTable.add("[lightgray] " + (baseEfficiency <= 0.0001f ? Stat.tiles : Stat.affinities).localized() + ": []");
                        
                        float boostScale = !Float.isNaN(recipe.boostScale) ? recipe.boostScale : attributeBlock.boostScale;
                        StatValue statValue = StatValues.blocks(recipe.attribute, block.floating, boostScale * size * size, !attributeBlock.displayEfficiency);
                        statValue.display(attributeTable);
                        
                        buttonContent.row();
                        buttonContent.add(attributeTable).pad(4f).growX();
                    }
                    
                    final int finalIndex = index;
                    button.changed(() -> configure(finalIndex));
                    button.update(() -> button.setChecked(currentRecipeIndex == finalIndex));
                }
                button.setDisabled(!recipe.unlockedNow());
                button.add(buttonContent).pad(4f);

                buttonTable.add(button).pad(4f).margin(10f).grow();
                buttonTable.row();
                index++;
            }
            
            table.add(buttonTable);
        }
        
        @Override
        public void displayBars(Table table) {
            if (currentRecipe == null) return;
            
            var liquidBarPos = barMap.get("liquid");
            boolean liquidAdded = false;
            for (Func<Building, Bar> bar : this.block.listBars()) {
                if (currentRecipe.hasLiquids() && !liquidAdded && bar == liquidBarPos) {
                    for (LiquidStack liquid : currentRecipe.input.liquids) {
                        Bar liquidBar = liquidBarMap.get("liquid-" + liquid.liquid.name, () -> new Bar(
                                () -> liquid.liquid.localizedName,
                                liquid.liquid::barColor,
                                () -> this.liquids.get(liquid.liquid) / liquid.amount
                            ));
                        
                        table.add(liquidBar).growX();
                        table.row();
                    }
                    
                    for (LiquidStack liquid : currentRecipe.output.liquids) {
                        Bar liquidBar = liquidBarMap.get("liquid-" + liquid.liquid.name, () -> new Bar(
                            () -> liquid.liquid.localizedName,
                            liquid.liquid::barColor,
                            () -> this.liquids.get(liquid.liquid) / liquid.amount
                        ));
                        
                        table.add(liquidBar).growX();
                        table.row();
                    }
                    liquidAdded = true;
                    continue;
                }
                
                Bar result = (Bar) bar.get(this);
                if (result != null) {
                    table.add(result).growX();
                    table.row();
                }
            }
        }
        
        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress) return progress;
            return super.sense(sensor);
        }
        
        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(warmup);
            write.f(heat);
            write.f(outputHeat);
            
            Payload.write(payload, write);
            payloadInput.write(write);
            payloadOutput.write(write);
            write.bool(payloadOutgoing);
            write.f(payVector.x);
            write.f(payVector.y);
            write.f(payRotation);
            
            write.i(seed);
            
            write.i(currentRecipeIndex);
        }
        
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            progress = read.f();
            warmup = read.f();
            heat = read.f();
            outputHeat = read.f();
            
            if (revision >= 1) {
                payload = Payload.read(read);
                payloadInput.read(read);
                payloadOutput.read(read);
                payloadOutgoing = read.bool();
                payVector.set(read.f(), read.f());
                payRotation = read.f();
                
                seed = read.i();
            }
            
            int index = Mathf.clamp(read.i(), 0, recipes.size - 1);
            setCurrentRecipe(index, false);
        }
        
        @Override
        public byte version() { return 1; }
    }
    
    @Override
    public void setBars() {
        super.setBars();
        
        removeBar("power");
        removeBar("liquid");
        
        addBar("liquid", b -> null);
        
        addBar("power", (MultiCrafterBuild b) -> {
            if (b.currentRecipe == null || !b.currentRecipe.input.hasPower() || consPower == null) {
                return null;
            }
            
            return new Bar(
                consPower.buffered ? Core.bundle.format("bar.poweramount", Float.isNaN(b.power.status * consPower.capacity) ? "<ERROR>" : UI.formatAmount((int) (b.power.status * consPower.capacity))) :
                    "bar.power",
                Pal.powerBar,
                () -> b.efficiency
            );
        });
        addBar("power-output", (MultiCrafterBuild b) -> {
            if (b.currentRecipe == null || !b.currentRecipe.output.hasPower()) {
                return null;
            }
            
            return new Bar(
                Core.bundle.format("bar.poweroutput", Strings.fixed(b.getPowerProduction() * 60f * b.timeScale(), 1)),
                Pal.powerBar,
                () -> b.efficiency
            );
        });
        addBar("heat", (MultiCrafterBuild b) -> {
            if (b.currentRecipe == null || !b.currentRecipe.input.hasHeat()) {
                return null;
            }
            
            return new Bar(
                Core.bundle.format("bar.heatpercent", (int) (b.heat + 0.01f), (int) (b.efficiencyScale() * 100 + 0.01f)),
                Pal.lightOrange,
                b::heatFrac
            );
        });
        addBar("heat-output", (MultiCrafterBuild b) -> {
            if (b.currentRecipe == null || !b.currentRecipe.output.hasHeat()) {
                return null;
            }
            
            return new Bar(
                "bar.heat",
                Pal.lightOrange,
                b::heatOutputFrac
            );
        });
        
        addBar("progress", b -> new Bar(
            "bar.loadprogress",
            Pal.accent,
            b::progress
        ));
    }
    
    @Override
    public void setStats() {
        super.setStats();
        
        stats.add(Stat.output, table -> {
            // Add a toggle to show in per second or total amount
            table.row();
            boolean perSecond = Core.settings.getBool("multicrafter.show-per-second");
            table.check(Core.bundle.format("ui.show-per-second"), perSecond, b -> {
                Core.settings.put("multicrafter.show-per-second", b);
                Vars.ui.content.show(this);
            });
            table.row();
            
            for (Recipe recipe : recipes) {
                table.add(recipe.buildTable(this, hasAttribute(), perSecond)).pad(4f).grow();
                table.row();
            }
            
            table.row();
            table.defaults().grow();
        });
    }
    
    protected boolean hasAttribute() { return false; }
}
