package dev.jojofr.multicrafter.type;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import dev.jojofr.multicrafter.meta.SimpleStatValues;
import mindustry.type.*;
import mindustry.world.blocks.payloads.Payload;

/**
 * Represents the inputs or outputs of a {@link Recipe}.
 * Supports items, liquids, power, heat, and payloads.
 */
public class IOEntry {
    /**
     * Items consumed or produced. Can be {@code null} if no items are present.
     * <p>
     * Use {@link #getItems()} and {@link #withItems(Object...)} to access and modify the items safely. Direct access is not recommended.
     */
    public @Nullable ItemStack[] items;
    /**
     * Liquids consumed or produced, per ticks. Can be {@code null} if no liquids are present.
     * <p>
     * Use {@link #getLiquids()} and {@link #withLiquids(Object...)} to access and modify the liquids safely. Direct access is not recommended.
     */
    public @Nullable LiquidStack[] liquids;
    /** Power consumed or produced, per ticks. */
    public float power = 0;
    /** Heat consumed or produced. */
    public float heat = 0;
    /**
     * Payloads consumed or produced. Can be {@code null} if no payloads are present.
     * <p>
     * Use {@link #getPayloads()} and {@link #withPayloads(Object...)} to access and modify the payloads safely. Direct access is not recommended.
     */
    public @Nullable Seq<PayloadStack> payloads;
    
    /** Create an empty {@link IOEntry}. */
    public IOEntry() {}
    /** Create an {@link IOEntry} with the given items. */
    public IOEntry(ItemStack[] items) { this.items = items; }
    /** Create an {@link IOEntry} with the given liquids. */
    public IOEntry(LiquidStack[] liquids) { this.liquids = liquids; }
    /** Create an {@link IOEntry} with the given items and liquids. */
    public IOEntry(ItemStack[] items, LiquidStack[] liquids) { this.items = items; this.liquids = liquids; }
    
    /**
     * Creates a shallow copy of {@code other}.
     * @param other {@link IOEntry} to copy.
     */
    public void copy(IOEntry other) {
        this.items = other.items;
        this.liquids = other.liquids;
        this.power = other.power;
        this.heat = other.heat;
        this.payloads = other.payloads;
    }
    
    /**
     * Sets the {@link #items} directly.
     *
     * @deprecated  Use {@link #withItems(Object...)} instead.
     * @param items the {@link ItemStack} pre-built array to set for this entry.
     * @return      itself, for chaining.
     */
    @Deprecated(since = "1.5.0")
    public IOEntry withItems(ItemStack... items) {
        this.items = items;
        return this;
    }
    /**
     * Sets the {@link #items} by converting the given objects to {@link ItemStack}.
     *
     * @param items the alternating {@code Item, amount} pairs, passed to {@link ItemStack#with(Object...)}.
     * @return      itself, for chaining.
     */
    public IOEntry withItems(Object... items) {
        this.items = ItemStack.with(items);
        return this;
    }
    
    /**
     * Sets the {@link #liquids} directly.
     *
     * @deprecated    Use {@link #withLiquids(Object...)} instead.
     * @param liquids the {@link LiquidStack} pre-built array to set for this entry.
     * @return        itself, for chaining.
     */
    @Deprecated(since = "1.5.0")
    public IOEntry withLiquids(LiquidStack... liquids) {
        this.liquids = liquids;
        return this;
    }
    /**
     * Sets the {@link #liquids} by converting the given objects to {@link LiquidStack}.
     *
     * @param liquids the alternating {@code Liquid, amount} pairs, passed to {@link LiquidStack#with(Object...)}.
     * @return        itself, for chaining.
     */
    public IOEntry withLiquids(Object... liquids) {
        this.liquids = LiquidStack.with(liquids);
        return this;
    }
    
    /**
     * Sets the {@link #power} directly.
     *
     * @param power the amount of power to set for this entry, per tick.
     * @return      itself, for chaining.
     */
    public IOEntry withPower(float power) {
        this.power = power;
        return this;
    }
    
    /**
     * Sets the {@link #heat} directly.
     *
     * @param heat the amount of heat to set for this entry.
     * @return     itself, for chaining.
     */
    public IOEntry withHeat(float heat) {
        this.heat = heat;
        return this;
    }
    
    /**
     * Sets the {@link #payloads} directly.
     *
     * @deprecated     Use {@link #withPayloads(Object...)} instead.
     * @param payloads the {@link PayloadStack} pre-built array to set for this entry.
     * @return         itself, for chaining.
     */
    @Deprecated(since = "1.5.0")
    public IOEntry withPayloads(PayloadStack... payloads) {
        this.payloads = new Seq<>(payloads);
        return this;
    }
    /**
     * Sets the {@link #payloads} by converting the given objects to {@link PayloadStack}.
     *
     * @param payloads the alternating {@code Payload, amount} pairs, passed to {@link PayloadStack#with(Object...)}.
     * @return         itself, for chaining.
     */
    public IOEntry withPayloads(Object... payloads) {
        PayloadStack[] stacks = PayloadStack.with(payloads);
        this.payloads = new Seq<>(stacks);
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
    public boolean hasPayloadsUnit() { return hasPayloads() && payloads.contains(stack -> stack.item instanceof UnitType); }
    public boolean acceptPayload(Payload payload) {
        if (payloads == null) return false;
        
        for (PayloadStack stack : payloads) if (payload.content() == stack.item) return true;
        return false;
    }
    
    /**
     * Returns the amount of the given payload required by this entry.
     *
     * @param payload the payload to check.
     * @return        the required amount of the payload, or {@code 0} if the payload is not present in this entry.
     */
    public int getPayloadRequirements(Payload payload) {
        if (payloads == null) return 0;
        
        for (PayloadStack stack : payloads) if (payload.content() == stack.item) return stack.amount;
        return 0;
    }
    
    /**
     * Return the items in this entry.
     *
     * @return The items in this entry, or an empty array if none are present.
     */
    public ItemStack[] getItems() { return items == null ? ItemStack.empty : items; }
    /**
     * Return the liquids in this entry.
     *
     * @return The liquids in this entry, or an empty array if none are present.
     */
    public LiquidStack[] getLiquids() { return liquids == null ? LiquidStack.empty : liquids; }
    public float getPower() { return power; }
    public float getHeat() { return heat; }
    /**
     * Return the payloads in this entry.
     *
     * @return The payloads in this entry, or an empty sequence if none are present.
     */
    public Seq<PayloadStack> getPayloads() { return payloads == null ? new Seq<>(0) : payloads; }
    
    
    /**
     * Builds a table displaying the contents of this entry, in a grid format, with a maximum of 5 items per row.
     *
     * @param perSecond whether to display the amounts as per second or per craft.
     * @param craftTime the time it takes to craft the recipe, in ticks.
     * @return          the table displaying the contents of this entry.
     */
    public Table buildTable(boolean perSecond, float craftTime) { return buildTable(true, perSecond, craftTime, false); }
    
    /**
     * Builds a table displaying the contents of this entry, in a grid format, with a maximum of 5 items per row.
     * <p>
     * If {@code random} is true, the table is limited to only displaying the items, and the amounts are displayed as percentages of the total amount, instead of absolute values.
     *
     * @param perSecond whether to display the amounts as per second or per craft.
     * @param craftTime the time it takes to craft the recipe, in ticks.
     * @param random    whether to display the amounts as percentages of the total amount, instead of absolute values.
     * @return          the table displaying the contents of this entry.
     */
    public Table buildTable(boolean perSecond, float craftTime, boolean random) { return buildTable(true, perSecond, craftTime, random); }
    /**
     * Builds a table displaying the contents of this entry, in a grid format, with a maximum of 5 items per row.
     *
     * @param tooltip   whether to display tooltips for the items, liquids, and payloads.
     * @param perSecond whether to display the amounts as per second or per craft.
     * @param craftTime the time it takes to craft the recipe, in ticks.
     * @return          the table displaying the contents of this entry.
     */
    public Table buildTable(boolean tooltip, boolean perSecond, float craftTime) { return buildTable(tooltip, perSecond, craftTime, false); }
    /**
     * Builds a table displaying the contents of this entry, in a grid format, with a maximum of 5 items per row.
     * <p>
     * If {@code random} is true, the table is limited to only displaying the items, and the amounts are displayed as percentages of the total amount, instead of absolute values.
     *
     * @param tooltip   whether to display tooltips for the items, liquids, and payloads.
     * @param perSecond whether to display the amounts as per second or per craft.
     * @param craftTime the time it takes to craft the recipe, in ticks.
     * @param random    whether to display the amounts as percentages of the total amount, instead of absolute values.
     * @return          the table displaying the contents of this entry.
     */
    public Table buildTable(boolean tooltip, boolean perSecond, float craftTime, boolean random) {
        Table table = new Table();
        if (random && !hasItems()) return table;
        Table materialTable = new Table();
        
        SimpleStatValues.count = 0;
        SimpleStatValues.perSecond = perSecond;
        SimpleStatValues.craftTime = craftTime;
        
        if (random) {
            int sum = 0;
            for (ItemStack stack : items) sum += stack.amount;
            
            SimpleStatValues.itemsPercent(false, tooltip, sum, items).display(materialTable);
            table.add(materialTable);
            return table;
        }
        
        if (hasItems()) SimpleStatValues.items(false, tooltip, items).display(materialTable);
        if (hasLiquids()) SimpleStatValues.liquids(false, tooltip, liquids).display(materialTable);
        if (hasPayloads()) SimpleStatValues.payloads(false, tooltip, payloads).display(materialTable);
        
        Table smallIndictor = new Table();
        if (hasPower()) SimpleStatValues.power(power).display(smallIndictor);
        if (hasHeat()) SimpleStatValues.heat(heat).display(smallIndictor);
        
        table.add(materialTable);
        table.row();
        table.add(smallIndictor);
        
        return table;
    }
    
    /**
     * Removes duplicate resources from this entry.
     * <p>
     * When duplicates are found, only the first occurrence is kept and a warning is logged.
     *
     * @param name the recipe name, used for logging.
     * @return     itself, for chaining.
     */
    public IOEntry removeDuplicate(String name) {
        if (hasItems()) items = removeDuplicates(items).toArray(ItemStack.class);
        if (hasLiquids()) liquids = removeDuplicates(liquids).toArray(LiquidStack.class);
        if (hasPayloads()) payloads = removeDuplicates(payloads.toArray(PayloadStack.class));
        
        return this;
    }
    
    private <T> Seq<T> removeDuplicates(T[] array) {
        Seq<T> unique = new Seq<>(array.length);
        
        for (T element : array) {
            if (unique.contains(other -> other == element)) {
                Log.warn("Duplicate element '@' found in IOEntry, ignoring.", element.toString());
                continue;
            }
            unique.add(element);
        }
        
        return unique;
    }
}
