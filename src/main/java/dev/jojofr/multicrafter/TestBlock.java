package dev.jojofr.multicrafter;

import dev.jojofr.multicrafter.type.IOEntry;
import dev.jojofr.multicrafter.type.Recipe;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.world.Block;

public class TestBlock {
    public static Block testBlock;
    public static Recipe testRecipe = new Recipe("test",
        new IOEntry().withItems(ItemStack.with(Items.copper, 10, Items.lead, 5))
            .withLiquids(LiquidStack.with(Liquids.cryofluid, 20)).withPower(5).withHeat(10),
        new IOEntry().withItems(ItemStack.with(Items.silicon, 5)).withLiquids(LiquidStack.with(Liquids.water, 10)).withPayloads(PayloadStack.with(Blocks.vault, 3)), 60
    );
    
    public static void load() {
        testBlock = new MultiCrafterBlock("test-multi") {{
            health = 100;
            size = 3;
            
            recipes.add(testRecipe,
                new Recipe("test2",
                    new IOEntry().withItems(ItemStack.with(Items.copper, 5)).withLiquids(LiquidStack.with(Liquids.cryofluid, 10)).withPower(2).withHeat(5),
                    new IOEntry().withItems(ItemStack.with(Items.silicon, 2)).withLiquids(LiquidStack.with(Liquids.water, 5)).withPayloads(PayloadStack.with(Blocks.vault, 1)),
                    180
                )
            );
        }};
    }
}
