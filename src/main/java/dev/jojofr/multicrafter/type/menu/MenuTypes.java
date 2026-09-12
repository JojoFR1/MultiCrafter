package dev.jojofr.multicrafter.type.menu;

import arc.scene.ui.layout.Table;
import arc.util.Log;
import dev.jojofr.multicrafter.MultiCrafterBlock;

public enum MenuTypes implements MenuType {
    DEFAULT("default"),
    DEFAULT_LARGE("defaultLarge"),
    FULLSCREEN("fullScreen");
    
    public final String name;
    
    MenuTypes(String name) { this.name = name; }
    
    @Override
    public void build(Table table, MultiCrafterBlock.MultiCrafterBuild build) {
        switch (this) {
            case DEFAULT -> new Table();
            case DEFAULT_LARGE -> new Table();
            case FULLSCREEN -> new Table();
        };
    }
    
    public static MenuTypes get(String name) {
        for (MenuTypes type : values()) if (type.name.equals(name)) return type;
        
        Log.warn("[MultiCrafter] MenuType with name '" + name + "' not found, using default.");
        return DEFAULT;
    }
}
