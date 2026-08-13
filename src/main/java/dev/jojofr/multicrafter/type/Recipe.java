package dev.jojofr.multicrafter.type;

import arc.Core;
import arc.func.Cons;
import arc.math.Interp;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import dev.jojofr.multicrafter.MultiCrafterBlock;
import dev.jojofr.multicrafter.world.AttributeMultiCrafterBlock;
import mindustry.content.Fx;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Effect;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;

/**
 * Defines a recipe used by a {@link MultiCrafterBlock}.
 * <p>
 * A recipe specifies its inputs and outputs, crafting time, effects, and other properties used when processing the recipe.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
public class Recipe extends UnlockableContent {
    public final IOEntry input, output;
    /** Relative weight of this recipe when auto-selecting a recipe. Higher means it will be prioritized more. */
    public float weight = 1f;
    
    /** Time required to complete the recipe, in ticks. */
    public float craftTime = 80f;
    /** Effect played when the recipe is completed. */
    public Effect craftEffect = Fx.none;
    /** Effect played when the recipe is being processed. */
    public Effect updateEffect = Fx.none;
    /** Chance of the update effect being played each update. */
    public float updateEffectChance = 0.04f;
    /** Spread of the update effect. */
    public float updateEffectSpread = 4f;
    /** Speed at which the recipe warms up. */
    public float warmupSpeed = 0.019f;
    
    /** [Heat] Speed at which the heat warms up, or cools down. */
    public float warmupRate = 0.15f;
    /** [Heat Producer] Multiplier applied to heat produced above the required amount. */
    public float overheatScale = 1f;
    /** [Heat Producer] Maximum efficiency that can be reached through heating. */
    public float maxEfficiency = 4f;
    
    /** [Attribute] Attribute used for this recipe, or {@code null} to use the block's default. */
    public Attribute attribute = null;
    /** [Attribute] Base efficiency override, or {@link Float#NaN} to use the block's default. */
    public float baseEfficiency = Float.NaN;
    /** [Attribute] Boost scale override, or {@link Float#NaN} to use the block's default. */
    public float boostScale = Float.NaN;
    /** [Attribute] Maximum boost override, or {@link Float#NaN} to use the block's default. */
    public float maxBoost = Float.NaN;
    /** [Attribute] Minimum efficiency override, or {@link Float#NaN} to use the block's default. */
    public float minEfficiency = Float.NaN;
    
    /**
     * Whether item outputs are selected randomly based on their amounts.
     * <p>
     * Only works when the recipe has item outputs and no other outputs (liquids, power, heat, payloads).
     */
    public boolean randomOutput = false;
    
    /** Drawer used to render this recipe's block visuals. */
    public DrawBlock drawer = new DrawDefault();
    
    
    private static final Cons<IOEntry> EMPTY_CONS = e -> {};
    
    /** Creates an empty {@link Recipe}. */
    public Recipe(String name) { this(name, EMPTY_CONS, EMPTY_CONS, 80f); }
    
    /** @deprecated Use {@link #Recipe(String, Cons)} instead. */
    @Deprecated(since = "1.5.0") public Recipe(String name, IOEntry input) { this(name, input, new IOEntry(), 80f); }
    /** Creates a {@link Recipe} with the specified input and no output. */
    public Recipe(String name, Cons<IOEntry> input) { this(name, input, EMPTY_CONS, 80f); }
    
    /** @deprecated Use {@link #Recipe(String, Cons, Cons)} instead. */
    @Deprecated(since = "1.5.0") public Recipe(String name, IOEntry input, IOEntry output) { this(name, input, output, 80f); }
    /** Creates a {@link Recipe} with the specified input and output. */
    public Recipe(String name, Cons<IOEntry> input, Cons<IOEntry> output) { this(name, input, output, 80f); }
    
    /** @deprecated Use {@link #Recipe(String, Cons, Cons, float)} instead. */
    @Deprecated(since = "1.5.0") public Recipe(String name, IOEntry input, IOEntry output, float craftTime) { this(name, in -> in.copy(input), out -> out.copy(output), craftTime); }
    /** Creates a {@link Recipe} with the specified input, output, and crafting time. */
    public Recipe(String name, Cons<IOEntry> input, Cons<IOEntry> output, float craftTime) {
        super(name);
        
        this.localizedName = Core.bundle.get(getContentTypeName() + "." + this.name + ".name", this.name);
        this.description = Core.bundle.getOrNull(getContentTypeName() + "." + this.name + ".description");
        this.details = Core.bundle.getOrNull(getContentTypeName() + "." + this.name + ".details");
        this.credit = Core.bundle.getOrNull(getContentTypeName() + "." + this.name + ".credit");
        
        this.input = configure(input).removeDuplicate(name);
        this.output = configure(output).removeDuplicate(name);
        this.craftTime = craftTime;
    }
    
    private static IOEntry configure(Cons<IOEntry> configurator) {
        IOEntry entry = new IOEntry();
        if (configurator != null) configurator.get(entry);
        return entry;
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
    
    /**
     * Builds the UI table used to display this recipe.
     *
     * @param block         block that owns this recipe, used for attribute display. Can be null if not needed.
     * @param showAttribute whether to display attribute information
     * @param perSecond     whether to display the resources amounts per second instead of per craft
     * @return              the built table
     */
    public Table buildTable(MultiCrafterBlock block, boolean showAttribute, boolean perSecond) {
        Table table =  new Table();
        table.setBackground(Tex.whiteui);
        table.setColor(Pal.darkerGray);
        
        Table recipeTable = new Table();
        if (!unlockedNow()) {
            recipeTable.setColor(Pal.darkestGray);
            recipeTable.image(Icon.lock).size(100f, 50f).pad(12f).fill();
            
            return recipeTable;
        }
        
        Cell<Table> inputTable = recipeTable.add(this.input.buildTable(perSecond, craftTime)).minWidth(80f).pad(12f).fill();
        inputTable.left();
        
        Table time = new Table();
        Bar timeBar = new Bar(String.format("%.1f", this.craftTime / 60f) + "s",
            Pal.accent, () -> Interp.smooth.apply((Time.time % this.craftTime) / this.craftTime));
        time.add(timeBar).height(50f).width(250f);
        recipeTable.add(time).pad(12f);
        
        Cell<Table> outputCell = recipeTable.add(this.output.buildTable(perSecond, craftTime, this.randomOutput)).minWidth(80f).pad(12f).fill();
        outputCell.right();
        
        table.add(recipeTable).growX();
        
        if (showAttribute && attribute != null && block instanceof AttributeMultiCrafterBlock attributeBlock) {
            Table attributeTable = new Table();
            
            float baseEfficiency = !Float.isNaN(this.baseEfficiency) ? this.baseEfficiency : attributeBlock.baseEfficiency;
            attributeTable.add("[lightgray] " + (baseEfficiency <= 0.0001f ? Stat.tiles : Stat.affinities).localized() + ": []");
            
            float boostScale = !Float.isNaN(this.boostScale) ? this.boostScale : attributeBlock.boostScale;
            StatValue statValue = StatValues.blocks(attribute, block.floating, boostScale * block.size * block.size, !attributeBlock.displayEfficiency);
            statValue.display(attributeTable);
            
            table.row();
            table.add(attributeTable).pad(4f).growX();
        }
        
        return table;
    }
    
    public Recipe withLocalizedName(String localizedName) {
        this.localizedName = localizedName;
        return this;
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
    
    public Recipe withUpdateEffect(Effect updateEffect) { return withUpdateEffect(updateEffect, 0.04f, 4f); }
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
    
    /** Only useful for {@link AttributeMultiCrafterBlock} blocks. */
    public Recipe withAttribute(Attribute attribute) {
        this.attribute = attribute;
        return this;
    }
    /** Only useful for {@link AttributeMultiCrafterBlock} blocks. */
    public Recipe withBaseEfficiency(float baseEfficiency) {
        this.baseEfficiency = baseEfficiency;
        return this;
    }
    /** Only useful for {@link AttributeMultiCrafterBlock} blocks. */
    public Recipe withBoostScale(float boostScale) {
        this.boostScale = boostScale;
        return this;
    }
    /** Only useful for {@link AttributeMultiCrafterBlock} blocks. */
    public Recipe withMaxBoost(float maxBoost) {
        this.maxBoost = maxBoost;
        return this;
    }
    /** Only useful for {@link AttributeMultiCrafterBlock} blocks. */
    public Recipe withMinEfficiency(float minEfficiency) {
        this.minEfficiency = minEfficiency;
        return this;
    }
    
    public Recipe isRandomOutput() { return isRandomOutput(true); }
    public Recipe isNotRandomOutput() { return isRandomOutput(false); }
    /**
     * Sets whether this recipe produces a random item output.
     *
     * @param randomOutput whether to enable random output
     * @return             this recipe, for chaining
     * @throws IllegalArgumentException if the recipe has non-item outputs
     */
    public Recipe isRandomOutput(boolean randomOutput) {
        if (output.hasLiquids() || output.hasPower() || output.hasHeat() || output.hasPayloads())
            throw new IllegalArgumentException("Recipe '" + this.name + "' is set to random output, but has non-item outputs. Random output only works with items.");
        
        this.randomOutput = randomOutput;
        return this;
    }
    
    public Recipe isUnlocked() { return isUnlocked(true); }
    public Recipe isLocked() { return isUnlocked(false); }
    public Recipe isUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
        return this;
    }
    
    public Recipe isAlwaysUnlocked() { return isAlwaysUnlocked(true); }
    public Recipe isNotAlwaysUnlocked() { return isAlwaysUnlocked(false); }
    public Recipe isAlwaysUnlocked(boolean alwaysUnlocked) {
        this.alwaysUnlocked = alwaysUnlocked;
        return this;
    }
    
    public Recipe withDrawer(DrawBlock drawer) {
        this.drawer = drawer;
        return this;
    }
    
    public boolean hasItems() { return input != null && input.hasItems() || output != null && output.hasItems(); }
    
    public boolean hasLiquids() { return input != null && input.hasLiquids() || output != null && output.hasLiquids(); }
    
    public boolean hasPower() { return input != null && input.hasPower() || output != null && output.hasPower(); }
    
    public boolean hasHeat() { return input != null && input.hasHeat() || output != null && output.hasHeat(); }
    
    public boolean hasPayloads() { return input != null && input.hasPayloads() || output != null && output.hasPayloads(); }
    
    @Override
    public ContentType getContentType() {
        return ContentType.typeid_UNUSED;
    }
    
    protected String getContentTypeName() {
        return "recipe";
    }
}
