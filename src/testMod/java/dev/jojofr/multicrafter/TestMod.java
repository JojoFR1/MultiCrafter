package dev.jojofr.multicrafter;

import mindustry.mod.Mod;

public class TestMod extends Mod {
    
    @Override
    public void loadContent() {
        TestBlock.load();
        TestTechTree.load();
    }
}

