package dev.jojofr.multicrafter;

import arc.util.Log;
import mindustry.mod.ClassMap;
import mindustry.mod.Mod;

public class MultiCrafterMod extends Mod {

    public MultiCrafterMod() {
        Log.info("[MultiCrafter] Library successfully loaded!");
        
        ClassMap.classes.put("MultiCrafter", MultiCrafterBlock.class);
    }
}
