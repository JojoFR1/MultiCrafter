package dev.jojofr.multicrafter.type;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import dev.jojofr.multicrafter.meta.SimpleStatValues;
import mindustry.type.*;
import mindustry.world.blocks.payloads.Payload;

/**
 * Represents the input or output of a recipe, with items, liquids, power, heat and payloads.
 */
public class IOEntry {
    public @Nullable ItemStack[] items;
    public @Nullable LiquidStack[] liquids;
    public float power = 0;
    public float heat = 0;
    public @Nullable Seq<PayloadStack> payloads;
    
    public IOEntry() {}
    public IOEntry(ItemStack[] items) { this.items = items; }
    public IOEntry(LiquidStack[] liquids) { this.liquids = liquids; }
    public IOEntry(ItemStack[] items, LiquidStack[] liquids) { this.items = items; this.liquids = liquids; }
    
    public void copy(IOEntry other) {
        this.items = other.items;
        this.liquids = other.liquids;
        this.power = other.power;
        this.heat = other.heat;
        this.payloads = other.payloads;
    }
    
    public IOEntry withItems(ItemStack... items) {
        this.items = items;
        return this;
    }
    
    public IOEntry withItems(Object... items) {
        this.items = ItemStack.with(items);
        return this;
    }
    
    public IOEntry withLiquids(LiquidStack... liquids) {
        this.liquids = liquids;
        return this;
    }
    
    public IOEntry withLiquids(Object... liquids) {
        this.liquids = LiquidStack.with(liquids);
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
        this.payloads = new Seq<>(payloads);
        return this;
    }
    
    public IOEntry withPayloads(Object... payloads) {
        PayloadStack[] stacks = PayloadStack.with(payloads);
        this.payloads = new Seq<>(stacks);
        return this;
    }
    
    /**
     * Intended for internal use.
     * <p>
     * Builds a UI table representing this entry.
     */
    public Table buildTable(boolean perSecond, float craftTime) { return buildTable(true, perSecond, craftTime); }
    public Table buildTable(boolean tooltip, boolean perSecond, float craftTime) {
        Table table = new Table();
        Table materialTable = new Table();
        
        SimpleStatValues.count = 0;
        SimpleStatValues.perSecond = perSecond;
        SimpleStatValues.craftTime = craftTime;
        
        SimpleStatValues.items(false, tooltip, items).display(materialTable);
        SimpleStatValues.liquids(false, tooltip, liquids).display(materialTable);
        SimpleStatValues.payloads(false, tooltip, payloads).display(materialTable);
        
        Table smallIndictor = new Table();
        if (power > 0) SimpleStatValues.power(power).display(smallIndictor);
        if (heat > 0) SimpleStatValues.heat(heat).display(smallIndictor);
        
        table.add(materialTable);
        table.row();
        table.add(smallIndictor);
        
        return table;
    }
    
    public Table buildTableRandom(boolean perSecond, float craftTime) { return buildTableRandom(true, perSecond, craftTime); }
    public Table buildTableRandom(boolean tooltip, boolean perSecond, float craftTime) {
        Table table = new Table();
        Table materialTable = new Table();
        
        SimpleStatValues.count = 0;
        SimpleStatValues.perSecond = perSecond;
        SimpleStatValues.craftTime = craftTime;
        
        int sum = 0;
        for (ItemStack stack : items) sum += stack.amount;
        
        SimpleStatValues.itemsPercent(false, tooltip, sum, items).display(materialTable);
        
        table.add(materialTable);
        
        return table;
    }
    
    public IOEntry removeDuplicate(String name) {
        if (hasItems()) {
            Seq<ItemStack> uniqueItems = new Seq<>(items.length);
            
            for (ItemStack stack : items) {
                if (uniqueItems.contains(other -> other.item == stack.item)) {
                    Log.warn("Duplicate item '@' found in IOEntry for recipe '@', ignoring.", stack.item.name, name);
                    continue;
                }
                uniqueItems.add(stack);
            }
            
            items = uniqueItems.toArray(ItemStack.class);
        }
        
        if (hasLiquids()) {
            Seq<LiquidStack> uniqueLiquids = new Seq<>(liquids.length);
            
            for (LiquidStack stack : liquids) {
                if (uniqueLiquids.contains(other -> other.liquid == stack.liquid)) {
                    Log.warn("Duplicate liquid '@' found in IOEntry for recipe '@', ignoring.", stack.liquid.name, name);
                    continue;
                }
                uniqueLiquids.add(stack);
            }
            
            liquids = uniqueLiquids.toArray(LiquidStack.class);
            
        }
        
        if (hasPayloads()) {
            Seq<PayloadStack> uniquePayloads = new Seq<>(payloads.size);
            
            for (PayloadStack stack : payloads) {
                if (uniquePayloads.contains(other -> other.item == stack.item)) {
                    Log.warn("Duplicate payload '@' found in IOEntry for recipe '@', ignoring.", stack.item.name, name);
                    continue;
                }
                uniquePayloads.add(stack);
            }
            
            payloads = uniquePayloads;
        }
        
        return this;
    }
    
    public boolean isEmpty() { return (items == null || items.length == 0) && (liquids == null || liquids.length == 0) && power <= 0 && heat <= 0 && (payloads == null || payloads.size == 0); }
    
    public boolean hasItems() { return items != null && items.length > 0; }
    public boolean acceptItem(Item item) {
        if (items == null) return false;
        
        for (ItemStack stack : items) if (item == stack.item) return true;
        return false;
    }
    
    public boolean hasLiquids() { return liquids != null && liquids.length > 0; }
    public boolean acceptLiquid(Liquid liquid) {
        if (liquids == null) return false;
        
        for (LiquidStack stack : liquids) if (liquid == stack.liquid) return true;
        return false;
    }
    
    public boolean hasPower() { return power > 0; }
    public boolean hasHeat() { return heat > 0; }
    
    public boolean hasPayloads() { return payloads != null && payloads.size > 0; }
    public boolean hasUnits() { return hasPayloads() && payloads.contains(stack -> stack.item instanceof UnitType); }
    public boolean acceptPayload(Payload payload) {
        if (payloads == null) return false;
        
        for (PayloadStack stack : payloads) if (payload.content() == stack.item) return true;
        return false;
    }
    
    public int getPayloadRequirements(Payload payload) {
        if (payloads == null) return 0;
        
        for (PayloadStack stack : payloads) if (payload.content() == stack.item) return stack.amount;
        return 0;
    }
    
    public ItemStack[] getItems() { return items == null ? ItemStack.empty : items; }
    public LiquidStack[] getLiquids() { return liquids == null ? LiquidStack.empty : liquids; }
    public Seq<PayloadStack> getPayloads() { return payloads == null ? new Seq<>(0) : payloads; }
}
