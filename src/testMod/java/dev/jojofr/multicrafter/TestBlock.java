package dev.jojofr.multicrafter;

import dev.jojofr.multicrafter.type.IOEntry;
import dev.jojofr.multicrafter.type.Recipe;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.world.Block;

public class TestBlock {
    public static Block testBlock;
    public static Recipe testRecipe = new Recipe("test",
        new IOEntry().withItems(ItemStack.with(Items.copper, 10, Items.lead, 5))
            .withLiquids(LiquidStack.with(Liquids.cryofluid, 20f / 60f)),
        new IOEntry().withItems(ItemStack.with(Items.silicon, 5)).withLiquids(LiquidStack.with(Liquids.water, 10f)).withPower(5f), 3f * 60f
    );
    
    public static void load() {
        testBlock = new MultiCrafterBlock("test-multi") {{
            health = 100;
            size = 3;
            
            itemCapacity = 30;
            liquidCapacity = 50;
            
            recipes.add(testRecipe,
                new Recipe("test2",
                    new IOEntry().withLiquids(LiquidStack.with(Liquids.cryofluid, 10)),
                    new IOEntry().withLiquids(LiquidStack.with(Liquids.water, 5)).withPayloads(PayloadStack.with(Blocks.vault, 1)),
                    180f
                )
            );
            
            requirements(Category.crafting, ItemStack.empty);
        }};
    }
}
