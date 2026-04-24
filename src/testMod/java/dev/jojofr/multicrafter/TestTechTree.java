package dev.jojofr.multicrafter;

import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.TechTree;
import mindustry.type.ItemStack;

public class TestTechTree {
    
    public static void load() {
        new TechTree.TechNode(Blocks.coreShard.techNode, TestBlock.testRecipe, ItemStack.with(Items.copper, 1));
    }
}
