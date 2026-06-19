package dev.jojofr.multicrafter;

import arc.util.Log;
import dev.jojofr.multicrafter.type.DrawRecipe;
import dev.jojofr.multicrafter.world.AttributeMultiCrafterBlock;
import mindustry.mod.ClassMap;
import mindustry.mod.Mod;

public class MultiCrafterMod extends Mod {

    public MultiCrafterMod() {
        ClassMap.classes.put("MultiCrafter", MultiCrafterBlock.class);
        ClassMap.classes.put("AttributeMultiCrafter", AttributeMultiCrafterBlock.class);
        ClassMap.classes.put("DrawRecipe", DrawRecipe.class);
        
        Log.info("[MultiCrafter] Library successfully loaded!");
    }
}
