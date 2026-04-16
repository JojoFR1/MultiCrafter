package dev.jojofr.multicrafter;

import arc.util.Log;
import mindustry.mod.Mod;

public class MultiCrafterMod extends Mod {

    public MultiCrafterMod() {
        Log.info("[MultiCrafter] Library successfully loaded!");
    }
    
    @Override
    public void loadContent() {
        TestBlock.load();
        TestTechTree.load();
    }
}
