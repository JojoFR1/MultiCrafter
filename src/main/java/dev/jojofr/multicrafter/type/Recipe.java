package dev.jojofr.multicrafter.type;

import arc.Core;
import mindustry.content.Fx;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Effect;

public class Recipe extends UnlockableContent {
    public final IOEntry input, output;
    
    public float craftTime;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public float updateEffectChance = 0.04f;
    public float updateEffectSpread = 4f;
    public float warmupSpeed = 0.019f;
    
    /** [Heat Consumer] */
    public float warmupRate = 0.15f;
    /** [Heat Producer] After heat meets this requirement, excess heat will be scaled by this number. */
    public float overheatScale = 1f;
    /** [Heat Producer] Maximum possible efficiency after overheat. */
    public float maxEfficiency = 4f;
    
    public Recipe(String name) { this(name, new IOEntry(), new IOEntry()); }
    public Recipe(String name, IOEntry input) { this(name, input, new IOEntry()); }
    public Recipe(String name, IOEntry input, IOEntry output) {
        this(name, input, output, 80);
    }
    public Recipe(String name, IOEntry input, IOEntry output, float craftTime) {
        super(name);
        
        this.localizedName = Core.bundle.get(getContentTypeName() + "." + this.name + ".name", this.name);
        this.description = Core.bundle.getOrNull(getContentTypeName() + "." + this.name + ".description");
        this.details = Core.bundle.getOrNull(getContentTypeName() + "." + this.name + ".details");
        this.credit = Core.bundle.getOrNull(getContentTypeName() + "." + this.name + ".credit");
        
        this.input = input;
        this.output = output;
        this.craftTime = craftTime;
    }
    
    @Override
    public void postInit() {
        if(databaseCategory == null || databaseCategory.isEmpty()) databaseCategory = getContentTypeName();
        if(databaseTag == null || databaseTag.isEmpty()) databaseTag = "default";
        
        databaseTabs.addAll(shownPlanets);
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
    public Recipe withUpdateEffect(Effect updateEffect, float chance) {
        return withUpdateEffect(updateEffect, chance, 4f);
    }
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
    
    public Recipe withWarmupRate(float spread) {
        this.warmupRate = spread;
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
    
    public boolean hasPayloads() {
        return input != null && input.hasPayloads() || output != null && output.hasPayloads();
    }
    
    @Override
    public ContentType getContentType() {
        return ContentType.typeid_UNUSED;
    }
    
    public String getContentTypeName() {
        return "recipe";
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
}
