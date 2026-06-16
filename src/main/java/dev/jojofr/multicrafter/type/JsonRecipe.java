package dev.jojofr.multicrafter.type;

import arc.util.Nullable;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.type.ItemStack;

public class JsonRecipe {
    public String name = "empty-recipe";
    public String localizedName = "Empty Recipe";
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
    
    @Nullable public String research = null;
    @Nullable public ItemStack[] researchRequirements = null;
}