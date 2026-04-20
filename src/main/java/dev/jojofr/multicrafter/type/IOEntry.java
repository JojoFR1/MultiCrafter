package dev.jojofr.multicrafter.type;

import arc.scene.ui.layout.Table;
import dev.jojofr.multicrafter.meta.SimpleStatValues;
import mindustry.type.*;
import mindustry.world.blocks.payloads.Payload;
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
        
        Table materialTable = new Table();
        StatValues.items(false, items).display(materialTable);
        SimpleStatValues.liquids(false, liquids).display(materialTable);
        if (power > 0) SimpleStatValues.power(power).display(materialTable);
        if (heat > 0) SimpleStatValues.heat(heat).display(materialTable);
        SimpleStatValues.payloads(false, payloads).display(materialTable);
        
        table.add(materialTable);
        
        return table;
    }
    
    public boolean isEmpty() {
        return items.length == 0 && liquids.length == 0 && power <= 0 && heat <= 0 && payloads.length == 0;
    }
    
    public boolean hasItems() {
        return items.length > 0;
    }
    
    public boolean acceptItem(Item item) {
        for (ItemStack stack : items) if (item.equals(stack.item)) return true;
        return false;
    }
    
    public boolean hasLiquids() {
        return liquids.length > 0;
    }
    
    public boolean acceptLiquid(Liquid liquid) {
        for (LiquidStack stack : liquids) if (liquid.equals(stack.liquid)) return true;
        return false;
    }
    
    public boolean hasPower() {
        return power > 0;
    }
    
    public boolean hasHeat() {
        return heat > 0;
    }
    
    public boolean hasPayloads() {
        return payloads.length > 0;
    }
    
    public boolean acceptPayload(Payload payload) {
        for (PayloadStack stack : payloads) if (payload.equals(stack.item)) return true;
        return false;
    }
}
