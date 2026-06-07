package dev.jojofr.multicrafter;

import arc.util.Log;
import mindustry.mod.ClassMap;
import mindustry.mod.Mod;

public class MultiCrafterMod extends Mod {

    public MultiCrafterMod() {
        ClassMap.classes.put("MultiCrafter", MultiCrafterBlock.class);
        
        Log.info("[MultiCrafter] Library successfully loaded!");
    }
}
