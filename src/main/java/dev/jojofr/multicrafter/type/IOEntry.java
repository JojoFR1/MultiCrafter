package dev.jojofr.multicrafter.type;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import dev.jojofr.multicrafter.meta.SimpleStatValues;
import mindustry.type.*;
import mindustry.world.blocks.payloads.Payload;

/**
 * Represents the input or output of a recipe, with items, liquids, power, heat and payloads.
 */
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
    
    /**
     * Intended for internal use.
     * <p>
     * Builds a UI table representing this entry.
     */
    public Table buildTable() { return buildTable(true); }
    public Table buildTable(boolean tooltip) {
        Table table = new Table();
        Table materialTable = new Table();
        
        SimpleStatValues.count = 0;
        SimpleStatValues.items(false, tooltip, items).display(materialTable);
        SimpleStatValues.liquids(false, tooltip, liquids).display(materialTable);
        SimpleStatValues.payloads(false, tooltip, payloads).display(materialTable);
        materialTable.row();
        
        Table smallIndictor = new Table();
        if (power > 0) SimpleStatValues.power(power).display(smallIndictor);
        if (heat > 0) SimpleStatValues.heat(heat).display(smallIndictor);
        
        table.add(materialTable);
        table.row();
        table.add(smallIndictor);
        
        return table;
    }
    
    public IOEntry removeDuplicate(String name) {
        Seq<ItemStack> uniqueItems = new Seq<>();
        Seq<LiquidStack> uniqueLiquids = new Seq<>();
        Seq<PayloadStack> uniquePayloads = new Seq<>();
        
        for (ItemStack stack : items) {
            if (uniqueItems.contains(other -> other.item == stack.item)) {
                Log.warn("Duplicate item '@' found in IOEntry for recipe '@', ignoring.", stack.item.name, name);
                continue;
            }
            uniqueItems.add(stack);
        }
        
        for (LiquidStack stack : liquids) {
            if (uniqueLiquids.contains(other -> other.liquid == stack.liquid)) {
                Log.warn("Duplicate liquid '@' found in IOEntry for recipe '@', ignoring.", stack.liquid.name, name);
                continue;
            }
            uniqueLiquids.add(stack);
        }
        
        for (PayloadStack stack : payloads) {
            if (uniquePayloads.contains(other -> other.item == stack.item)) {
                Log.warn("Duplicate payload '@' found in IOEntry for recipe '@', ignoring.", stack.item.name, name);
                continue;
            }
            uniquePayloads.add(stack);
        }
        
        items = uniqueItems.toArray(ItemStack.class);
        liquids = uniqueLiquids.toArray(LiquidStack.class);
        payloads = uniquePayloads.toArray(PayloadStack.class);
        
        return this;
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
