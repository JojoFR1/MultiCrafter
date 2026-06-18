package dev.jojofr.multicrafter.type;

import arc.util.Nullable;
import arc.util.serialization.Json;
import arc.util.serialization.JsonValue;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.Objectives;
import mindustry.type.ItemStack;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.Attribute;

public class JsonRecipe {
    public String name = "empty-recipe";
    public String localizedName = "Empty Recipe";
    
    public IOEntry input = new IOEntry(), output = new IOEntry();
    public float weight = 1f;
    
    public float craftTime = 80f;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public float updateEffectChance = 0.04f;
    public float updateEffectSpread = 4f;
    public float warmupSpeed = 0.019f;
    
    public float warmupRate = 0.15f;
    public float overheatScale = 1f;
    public float maxEfficiency = 4f;
    
    // Attribute support
    @Nullable public Attribute attribute = null;
    public float baseEfficiency = 1f;
    public float boostScale = 1f;
    public float maxBoost = 1f;
    public float minEfficiency = -1f;
    public float displayEfficiencyScale = 1f;
    public boolean displayEfficiency = true;
    public boolean scaleLiquidConsumption = false;
    
    public boolean unlocked = false;
    public boolean alwaysUnlocked = false;
    @Nullable public ResearchData research = null;
    @Nullable @Deprecated public ItemStack[] researchRequirements = null;
    
    public DrawBlock drawer = new DrawDefault();
    
    public static class ResearchData implements Json.JsonSerializable {
        public String parent;
        @Nullable public ItemStack[] requirements;
        @Nullable public Objectives.Objective[] objectives;
        
        @Override
        public void write(Json json) {}
        
        @Override
        public void read(Json json, JsonValue jsonData) {
            if (jsonData.isString()) {
                this.parent = jsonData.asString();
            } else if (jsonData.isObject()) {
                this.parent = jsonData.getString("parent", null);
                
                if (jsonData.has("requirements"))
                    this.requirements = json.readValue(ItemStack[].class, jsonData.get("requirements"));
                
                if (jsonData.has("objectives"))
                    this.objectives = json.readValue(Objectives.Objective[].class, jsonData.get("objectives"));
            }
        }
    }
}