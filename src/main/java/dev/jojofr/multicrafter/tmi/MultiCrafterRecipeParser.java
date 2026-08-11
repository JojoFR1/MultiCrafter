package dev.jojofr.multicrafter.tmi;

import arc.struct.Seq;
import dev.jojofr.multicrafter.MultiCrafterBlock;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import org.jetbrains.annotations.NotNull;
import tmi.TooManyItems;
import tmi.recipe.Recipe;
import tmi.recipe.RecipeParser;
import tmi.recipe.RecipeType;
import tmi.recipe.types.HeatMark;
import tmi.recipe.types.PowerMark;
import tmi.recipe.types.RecipeItemType;

public class MultiCrafterRecipeParser extends RecipeParser<MultiCrafterBlock> {
    
    @Override
    public boolean isTarget(@NotNull Block block) { return block instanceof MultiCrafterBlock; }
    
    @Override @NotNull
    public Seq<Recipe> parse(@NotNull MultiCrafterBlock multiCrafterBlock) {
        Seq<Recipe> recipes = new Seq<>();
        
        for (var recipe : multiCrafterBlock.recipes) {
            Recipe tmiRecipe;
            
            if (!recipe.unlockedNow()) {
                tmiRecipe = new Recipe(RecipeType.getFactory(), TooManyItems.itemsManager.getItem(multiCrafterBlock), 0f);
                
                tmiRecipe.setSubInfo(table -> {
                    table.row();
                    table.add("[scarlet]Locked").pad(4f).row();
                });
                
                recipes.add(tmiRecipe);
                continue;
            } else tmiRecipe = new Recipe(RecipeType.getFactory(), TooManyItems.itemsManager.getItem(multiCrafterBlock), recipe.craftTime);
            
            // Input
            for (ItemStack item : recipe.input.getItems()) tmiRecipe.addMaterialInteger(TooManyItems.itemsManager.getItem(item.item), item.amount);
            for (LiquidStack liquid : recipe.input.getLiquids()) tmiRecipe.addMaterialFloat(TooManyItems.itemsManager.getItem(liquid.liquid), liquid.amount);
            
            if (recipe.input.hasPower()) tmiRecipe.addMaterialPersec(PowerMark.INSTANCE, recipe.input.power).setType(RecipeItemType.POWER);
            if (recipe.input.hasHeat()) tmiRecipe.addMaterialPersec(HeatMark.INSTANCE, recipe.input.heat).setType(RecipeItemType.POWER).floatFormat();
            
            // Output
            for (ItemStack item : recipe.output.getItems()) tmiRecipe.addProductionInteger(TooManyItems.itemsManager.getItem(item.item), item.amount);
            for (LiquidStack liquid : recipe.output.getLiquids()) tmiRecipe.addProductionFloat(TooManyItems.itemsManager.getItem(liquid.liquid), liquid.amount);
            
            if (recipe.output.hasPower()) tmiRecipe.addProductionPersec(PowerMark.INSTANCE, recipe.output.power).setType(RecipeItemType.POWER);
            if (recipe.output.hasHeat()) tmiRecipe.addProductionPersec(HeatMark.INSTANCE, recipe.output.heat).setType(RecipeItemType.POWER).floatFormat();
            
            recipes.add(tmiRecipe);
        }
        
        return recipes;
    }
}
