package dev.jojofr.multicrafter.type.menu;

import arc.scene.ui.layout.Table;
import dev.jojofr.multicrafter.MultiCrafterBlock;

public interface MenuType {
    void build(Table table, MultiCrafterBlock.MultiCrafterBuild build);
}
