package dev.jojofr.multicrafter.type;

import arc.util.Log;
import arc.util.Nullable;
import arc.util.serialization.Json;
import arc.util.serialization.JsonValue;
import mindustry.content.Fx;
import mindustry.content.TechTree;
import mindustry.entities.Effect;
import mindustry.game.Objectives;
import mindustry.io.SaveVersion;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.Attribute;

@SuppressWarnings("DeprecatedIsStillUsed")
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
    
    @Nullable public Attribute attribute = null;
    public float baseEfficiency = Float.NaN;
    public float boostScale = Float.NaN;
    public float maxBoost = Float.NaN;
    public float minEfficiency = Float.NaN;
    
    public boolean randomOutput = false;
    
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
            if (jsonData.isString()) this.parent = jsonData.asString();
            else if (jsonData.isObject()) {
                this.parent = jsonData.getString("parent", null);
                
                if (jsonData.has("requirements"))
                    this.requirements = json.readValue(ItemStack[].class, jsonData.get("requirements"));
                
                if (jsonData.has("objectives"))
                    this.objectives = json.readValue(Objectives.Objective[].class, jsonData.get("objectives"));
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    public Recipe build(Block owner) {
        Recipe recipe = new Recipe(prefixName(this.name, owner), this.input, this.output, this.craftTime);
        if (recipe.minfo == null) recipe.minfo = owner.minfo;
        
        recipe.weight = this.weight;
        
        if (recipe.localizedName == null || recipe.localizedName.isEmpty())
            recipe.localizedName = this.localizedName;
        recipe.craftEffect = this.craftEffect;
        recipe.updateEffect = this.updateEffect;
        recipe.updateEffectChance = this.updateEffectChance;
        recipe.updateEffectSpread = this.updateEffectSpread;
        recipe.warmupSpeed = this.warmupSpeed;
        recipe.warmupRate = this.warmupRate;
        recipe.overheatScale = this.overheatScale;
        recipe.maxEfficiency = this.maxEfficiency;
        
        recipe.attribute = this.attribute;
        recipe.baseEfficiency = this.baseEfficiency;
        recipe.boostScale = this.boostScale;
        recipe.maxBoost = this.maxBoost;
        recipe.minEfficiency = this.minEfficiency;
        
        recipe.randomOutput = this.randomOutput;
        if (randomOutput && (output.hasLiquids() || output.hasPower() ||output.hasHeat() || output.hasPayloads()))
            throw new IllegalArgumentException("Recipe '" + recipe.name + "' is set to random output, but has non-item outputs. Random output only works with items.");
        
        if (this.unlocked) recipe.isUnlocked();
        recipe.alwaysUnlocked = this.alwaysUnlocked;
        
        if (this.research != null && this.research.parent != null) {
            String researchName = this.research.parent;
            
            TechTree.TechNode lastNode = TechTree.all.find(node -> node.content == recipe);
            if (lastNode != null) lastNode.remove();
            
            TechTree.TechNode parent = TechTree.all.find(techNode ->
                techNode.content.name.equals(researchName)
                    || (recipe.minfo != null && recipe.minfo.mod != null && techNode.content.name.equals(recipe.minfo.mod.name +"-"+ researchName))
                    || techNode.content.name.equals(SaveVersion.mapFallback(researchName))
            );
            
            if (parent == null) {
                Log.warn("Content '@' isn't in the tech tree, but '@' requires it.", researchName, recipe.name);
                return recipe;
            }
            
            ItemStack[] requirements = this.research.requirements != null ? this.research.requirements
                : this.researchRequirements != null ? this.researchRequirements
                : recipe.researchRequirements();
            TechTree.TechNode node = new TechTree.TechNode(null, recipe, requirements);
            if (this.research.objectives != null && this.research.objectives.length > 0)
                node.objectives.addAll(this.research.objectives);
            
            if (!parent.children.contains(node)) parent.children.add(node);
            
            node.parent = parent;
            node.planet = parent.planet;
        }
        
        recipe.drawer = this.drawer;
        
        return recipe;
    }
    
    private static String prefixName(String name, Block owner) {
        if (owner == null || owner.minfo == null || owner.minfo.mod == null) return name;
        
        String modName = owner.minfo.mod.name;
        String prefix = modName + "-";
        
        return name.startsWith(prefix) ? name : prefix + name;
    }
}