package dev.jojofr.multicrafter;

import mindustry.mod.Mod;

public class ExampleMod extends Mod {
    
    @Override
    public void loadContent() {
        ExampleBlocks.load();
        TestTechTree.load();
    }
}

