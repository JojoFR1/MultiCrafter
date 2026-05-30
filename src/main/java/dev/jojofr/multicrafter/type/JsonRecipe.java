package dev.jojofr.multicrafter.type;

import mindustry.content.Fx;
import mindustry.entities.Effect;

public class JsonRecipe {
    public String name = "emptyrecipe";
    public IOEntry input = new IOEntry(), output = new IOEntry();
    
    public float craftTime = 80f;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public float updateEffectChance = 0.04f;
    public float updateEffectSpread = 4f;
    public float warmupSpeed = 0.019f;
    
    public float warmupRate = 0.15f;
    public float overheatScale = 1f;
    public float maxEfficiency = 4f;
    
    public boolean unlocked = false;
    
    public Recipe toRecipe() {
        Recipe recipe = new Recipe(name, input, output, craftTime)
            .withCraftEffect(craftEffect)
            .withUpdateEffect(updateEffect, updateEffectChance, updateEffectSpread)
            .withWarmupSpeed(warmupSpeed)
            .withWarmupRate(warmupRate)
            .withOverheatScale(overheatScale)
            .withMaxEfficiency(maxEfficiency);
        
        return unlocked ? recipe.isUnlocked() : recipe.isLocked();
    }
}
