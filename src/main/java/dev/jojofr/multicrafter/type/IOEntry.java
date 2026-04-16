package dev.jojofr.multicrafter.type;

import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;

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
}
