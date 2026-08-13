package dev.jojofr.multicrafter;

import dev.jojofr.multicrafter.type.Recipe;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.TechTree;
import mindustry.type.ItemStack;

public class TestTechTree {
    public static Recipe exampleRecipe = new Recipe("example-recipe-research");
    
    public static void load() {
        // For more details on tech tree, reference the vanilla tech tree.
        new TechTree.TechNode(
            // Any unlockable content, including any other recipes from this or other block (must have been loaded before!). The content *has* to be in the research tree.
            Blocks.coreShard.techNode,
            // The recipe to unlock. It has to be referenced by its object, you CANNOT create a new recipe with the same name.
            // You can go about this in various ways, but one example is to create a class like this one, the blocks,
            //   or any other content, but for recipes, and reference the recipe object directly here and in the block.
            exampleRecipe,
            ItemStack.with(Items.copper, 1)
        );
    }
}
