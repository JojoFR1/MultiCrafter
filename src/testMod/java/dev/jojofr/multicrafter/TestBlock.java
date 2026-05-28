package dev.jojofr.multicrafter;

import dev.jojofr.multicrafter.type.IOEntry;
import dev.jojofr.multicrafter.type.Recipe;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;

public class TestBlock {
    public static Block testBlock, otherTest;
    public static Recipe testRecipe = new Recipe("test",
        new IOEntry().withItems(ItemStack.with(Items.copper, 10, Items.lead, 5))
            .withLiquids(LiquidStack.with(Liquids.cryofluid, 20f / 60f)),
        new IOEntry().withItems(ItemStack.with(Items.silicon, 5)).withLiquids(LiquidStack.with(Liquids.water, 10f)), 3f * 60f
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
                    new IOEntry().withLiquids(LiquidStack.with(Liquids.water, 5)),
                    180f
                )
            );
            
            requirements(Category.crafting, ItemStack.empty);
        }};
        otherTest = new MultiCrafterBlock("tesssssst") {{
            health = 100;
            size = 4;
            
            itemCapacity = 50;
            liquidCapacity = 100;
            
            recipes.addAll(
                new Recipe("test-heati",
                    new IOEntry().withItems(ItemStack.with(Items.silicon, 2)).withHeat(10),
                    new IOEntry().withItems(ItemStack.with(Items.lead, 5)),
                    180f
                ).isUnlocked(),
                new Recipe("test-heato",
                    new IOEntry().withItems(ItemStack.with(Items.silicon, 2)),
                    new IOEntry().withItems(ItemStack.with(Items.lead, 5)).withHeat(10),
                    180f
                ).isUnlocked(),
                new Recipe("test-heatio",
                    new IOEntry().withItems(ItemStack.with(Items.silicon, 2)).withHeat(10),
                    new IOEntry().withItems(ItemStack.with(Items.lead, 5)).withHeat(5),
                    180f
                ).isUnlocked(),
                new Recipe("test-poweri",
                    new IOEntry().withItems(ItemStack.with(Items.silicon, 3)).withPower(10),
                    new IOEntry().withItems(ItemStack.with(Items.lead, 5)),
                    180f
                ).isUnlocked(),
                new Recipe("test-powero",
                    new IOEntry().withItems(ItemStack.with(Items.silicon, 3)),
                    new IOEntry().withItems(ItemStack.with(Items.lead, 5)).withPower(10),
                    180f
                ).isUnlocked(),
                new Recipe("test-powerio",
                    new IOEntry().withItems(ItemStack.with(Items.silicon, 3)).withPower(10),
                    new IOEntry().withItems(ItemStack.with(Items.lead, 5)).withPower(5),
                    180f
                ).isUnlocked()
            );
            
            requirements(Category.crafting, ItemStack.empty);
        }};
    }
}
