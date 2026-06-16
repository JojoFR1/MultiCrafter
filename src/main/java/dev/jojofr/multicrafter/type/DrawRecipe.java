package dev.jojofr.multicrafter.type;

import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import dev.jojofr.multicrafter.MultiCrafterBlock;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;

public class DrawRecipe extends DrawBlock {
    
    @Override
    public void draw(Building build) {
        if (!(build instanceof MultiCrafterBlock.MultiCrafterBuild multiCrafterBuild)) return;
        if (multiCrafterBuild.currentRecipe == null) return;
        if (multiCrafterBuild.currentRecipe.drawer == null) return;
        
        multiCrafterBuild.currentRecipe.drawer.draw(build);
    }
    
    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        if (!(block instanceof MultiCrafterBlock multiCrafterBlock)) return;
        if (multiCrafterBlock.recipes.isEmpty()) return;
        if (multiCrafterBlock.recipes.first().drawer == null) return;
        
        multiCrafterBlock.recipes.first().drawer.drawPlan(block, plan, list);
    }
    
    @Override
    public void load(Block block) {
        if (!(block instanceof MultiCrafterBlock multiCrafterBlock)) return;
        if (multiCrafterBlock.recipes.isEmpty()) return;
        
        for (Recipe recipe : multiCrafterBlock.recipes) {
            if (recipe.drawer == null) continue;
            recipe.drawer.load(block);
        }
    }
    
    @Override
    public TextureRegion[] icons(Block block) {
        if (!(block instanceof MultiCrafterBlock multiCrafterBlock)) return new TextureRegion[]{};
        if (multiCrafterBlock.recipes.isEmpty()) return new TextureRegion[]{};
        if (multiCrafterBlock.recipes.first().drawer == null) return new TextureRegion[]{};
        
        return multiCrafterBlock.recipes.first().drawer.icons(block);
    }
}
