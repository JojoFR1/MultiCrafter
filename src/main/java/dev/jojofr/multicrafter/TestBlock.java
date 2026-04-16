package dev.jojofr.multicrafter;

import dev.jojofr.multicrafter.type.IOEntry;
import dev.jojofr.multicrafter.type.Recipe;
import mindustry.content.Items;
import mindustry.type.ItemStack;
import mindustry.world.Block;

public class TestBlock {
    public static Block testBlock;
    public static Recipe testRecipe = new Recipe("test", new IOEntry().withItems(ItemStack.with(Items.copper, 10, Items.lead, 5)));
    
    public static void load() {
        testBlock = new MultiCrafterBlock("test-multi") {{
            health = 100;
            size = 3;
            
            recipes.add(testRecipe);
        }};
    }
}
