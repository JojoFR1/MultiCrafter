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

@SuppressWarnings("DeprecatedIsStillUsed")
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
    
    public Attribute attribute = null;
    public float baseEfficiency = Float.NaN;
    public float boostScale = Float.NaN;
    public float maxBoost = Float.NaN;
    public float minEfficiency = Float.NaN;
    
    public boolean randomOutput = false;
    
    public DrawBlock drawer = new DrawDefault();
    
    
    private static final Cons<IOEntry> EMPTY_CONS = e -> {};
    
    public Recipe(String name) { this(name, EMPTY_CONS, EMPTY_CONS, 80f); }
    
    @Deprecated(since = "1.5.0")
    public Recipe(String name, IOEntry input) { this(name, input, new IOEntry(), 80f); }
    public Recipe(String name, Cons<IOEntry> input) { this(name, input, EMPTY_CONS, 80f); }
    
    @Deprecated(since = "1.5.0")
    public Recipe(String name, IOEntry input, IOEntry output) { this(name, input, output, 80f); }
    public Recipe(String name, Cons<IOEntry> input, Cons<IOEntry> output) { this(name, input, output, 80f); }
    
    @Deprecated(since = "1.5.0")
    public Recipe(String name, IOEntry input, IOEntry output, float craftTime) { this(name, in -> in.copy(input), out -> out.copy(output), craftTime); }
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
        
        Cell<Table> outputCell = (randomOutput ? recipeTable.add(this.output.buildTableRandom(perSecond, craftTime))
                                                : recipeTable.add(this.output.buildTable(perSecond, craftTime))).minWidth(80f).pad(12f).fill();
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
    
    public Recipe isRandomOutput() {
        if (output.hasLiquids() || output.hasPower() || output.hasHeat() || output.hasPayloads())
            throw new IllegalArgumentException("Recipe '" + this.name + "' is set to random output, but has non-item outputs. Random output only works with items.");
        
        this.randomOutput = true;
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
    
    @Override
    public ContentType getContentType() {
        return ContentType.typeid_UNUSED;
    }
    
    protected String getContentTypeName() {
        return "recipe";
    }
}
