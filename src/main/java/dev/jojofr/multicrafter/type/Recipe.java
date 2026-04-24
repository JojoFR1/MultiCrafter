package dev.jojofr.multicrafter.type;

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
    
    public float overheatScale = 1f;
    public float maxEfficiency = 1f;
    public float warmupRate = 0.15f;
    
    public Recipe(String name) { this(name, new IOEntry(), new IOEntry()); }
    public Recipe(String name, IOEntry input) { this(name, input, new IOEntry()); }
    public Recipe(String name, IOEntry input, IOEntry output) {
        this(name, input, output, 80);
    }
    public Recipe(String name, IOEntry input, IOEntry output, float craftTime) {
        super(name);
        this.input = input;
        this.output = output;
        this.craftTime = craftTime;
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
    
    @Override
    public ContentType getContentType() {
        return ContentType.loadout_UNUSED;
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
}
