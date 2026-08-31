package dev.jojofr.multicrafter.world;

import arc.Core;
import dev.jojofr.multicrafter.MultiCrafterBlock;
import dev.jojofr.multicrafter.type.Recipe;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Stat;

public class AttributeMultiCrafterBlock extends MultiCrafterBlock {
    public Attribute attribute = Attribute.heat;
    public float baseEfficiency = 1f;
    public float boostScale = 1f;
    public float maxBoost = 1f;
    public float minEfficiency = -1f;
    
    public float displayEfficiencyScale = 1f;
    public boolean displayEfficiency = true;
    public boolean scaleLiquidConsumption = false;
    
    public AttributeMultiCrafterBlock(String name) { super(name); }
    
    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        
        if (!displayEfficiency) return;
        
        drawPlaceText(Core.bundle.format("bar.efficiency",
            (int) ((baseEfficiency + Math.min(maxBoost, boostScale * sumAttribute(attribute, x, y))) * 100f)), x, y, valid);
    }
    
    @Override
    public void setBars() {
        super.setBars();
        
        if(!displayEfficiency) return;
        
        addBar("efficiency", (AttributeMultiCrafterBuild entity) ->
            new Bar(
                () -> Core.bundle.format("bar.efficiency", (int) (entity.efficiencyMultiplier() * 100 * displayEfficiencyScale)),
                () -> Pal.lightOrange,
                entity::efficiencyMultiplier));
    }
    
    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return baseEfficiency + tile.getLinkedTilesAs(this, tempTiles).sumf(other -> other.floor().attributes.get(attribute)) >= minEfficiency;
    }
    
    @Override
    public void setStats() {
        super.setStats();
        
        stats.add(baseEfficiency <= 0.0001f ? Stat.tiles : Stat.affinities, attribute, floating, boostScale * size * size, !displayEfficiency);
    }
    
    public class AttributeMultiCrafterBuild extends MultiCrafterBuild {
        private Attribute currentAttribute = attribute;
        private float currentBaseEfficiency = baseEfficiency;
        private float currentBoostScale = boostScale;
        private float currentMaxBoost = maxBoost;
        private float currentMinEfficiency = minEfficiency;

        public float attrsum;
        
        @Override
        public float getProgressIncrease(float base) {
            return super.getProgressIncrease(base) * efficiencyMultiplier();
        }
        
        public float efficiencyMultiplier() {
            return currentBaseEfficiency + Math.min(currentMaxBoost, currentBoostScale * attrsum) + currentAttribute.env();
        }
        
        @Override
        public float efficiencyScale() {
            return scaleLiquidConsumption ? efficiencyMultiplier() : super.efficiencyScale();
        }

        @Override
        public boolean productionValid() {
            return super.productionValid() && efficiencyMultiplier() > 0f;
        }

        @Override
        public void pickedUp() {
            attrsum = 0f;
            warmup = 0f;
        }
        
        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            
            attrsum = sumAttribute(currentAttribute, tile.x, tile.y);
        }
        
        @Override
        protected void setCurrentRecipe(Recipe recipe, boolean showEffect) {
            super.setCurrentRecipe(recipe, showEffect);
            
            currentAttribute = recipe.attribute != null ? recipe.attribute : attribute;
            currentBaseEfficiency = !Float.isNaN(recipe.baseEfficiency) ? recipe.baseEfficiency : baseEfficiency;
            currentBoostScale = !Float.isNaN(recipe.boostScale) ? recipe.boostScale : boostScale;
            currentMaxBoost = !Float.isNaN(recipe.maxBoost) ? recipe.maxBoost : maxBoost;
            currentMinEfficiency = !Float.isNaN(recipe.minEfficiency) ? recipe.minEfficiency : minEfficiency;
            
            attrsum = sumAttribute(currentAttribute, tile.x, tile.y);
        }
    }
    
    @Override
    protected boolean hasAttribute() { return true; }
}
