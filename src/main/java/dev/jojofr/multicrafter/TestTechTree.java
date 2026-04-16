package dev.jojofr.multicrafter;

import arc.struct.Seq;
import dev.jojofr.multicrafter.type.Recipe;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Planets;
import mindustry.content.TechTree;
import mindustry.type.ItemStack;

import static mindustry.content.TechTree.*;

public class TestTechTree {
    
    public static void load() {
        new TechTree.TechNode(Blocks.coreShard.techNode, TestBlock.testRecipe, ItemStack.with(Items.copper, 1));
    }
}
