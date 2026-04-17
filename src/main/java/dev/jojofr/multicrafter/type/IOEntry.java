package dev.jojofr.multicrafter.type;

import arc.scene.ui.layout.Table;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.world.meta.StatValues;


public class IOEntry {
    public ItemStack[] items = {};
    public LiquidStack[] liquids = {};
    public float power = 0;
    public float heat = 0;
    public PayloadStack[] payloads = {};
    
    public IOEntry() {}
    
    public IOEntry withItems(ItemStack... items) {
        this.items = items;
        return this;
    }
    
    public IOEntry withLiquids(LiquidStack... liquids) {
        this.liquids = liquids;
        return this;
    }
    
    public IOEntry withPower(float power) {
        this.power = power;
        return this;
    }
    
    public IOEntry withHeat(float heat) {
        this.heat = heat;
        return this;
    }
    
    public IOEntry withPayloads(PayloadStack... payloads) {
        this.payloads = payloads;
        return this;
    }
    
    public Table buildTable() {
        Table table = new Table();
        // TODO input/output check
        table.left();
        table.right();
        
        Table materialTable = new Table();
        StatValues.items(false, items).display(materialTable);
        // StatValues.liquids
        // StatValues.
        
        // for (LiquidStack liquid : liquids) {
        //     Cell<Stack> iconCell = materialTable.add(StatValues.stack(liquid)).pad(2);
        // }
        
        table.add(materialTable);
        
        return table;
    }
    
    public boolean isEmpty() {
        return items.length == 0 && liquids.length == 0 && power <= 0 && heat <= 0 && payloads.length == 0;
    }
}
