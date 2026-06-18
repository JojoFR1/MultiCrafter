package dev.jojofr.multicrafter.meta;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import arc.util.Scaling;
import arc.util.Strings;
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.ui.Styles;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.mobile;
import static mindustry.world.meta.StatValues.withTooltip;

public class SimpleStatValues {
    public static int count = 0;
    public static boolean perSecond = false;
    public static float craftTime = 0f;
    
    public static StatValue items(boolean displayName, ItemStack... stacks) { return items(displayName, true, stacks); }
    public static StatValue items(boolean displayName, boolean tooltip, ItemStack... stacks) {
        return table -> {
            for(ItemStack stack : stacks){
                table.add(displayPayloads(stack.item, stack.amount, displayName, tooltip)).padRight(4f);
                if (++count % 5 == 0) table.row();
            }
        };
    }
    
    public static StatValue liquids(LiquidStack... liquids) {
        return liquids(true, liquids);
    }
    public static StatValue liquids(boolean displayName, LiquidStack... liquids) { return liquids(displayName, true, liquids); }
    public static StatValue liquids(boolean displayName, boolean tooltip, LiquidStack... liquids) {
        return table -> {
            for (LiquidStack liquid : liquids) {
                table.add(displayLiquid(liquid.liquid, liquid.amount, displayName, tooltip)).padRight(4f);
                if (++count % 5 == 0) table.row();
            }
        };
    }
    
    public static StatValue power(float amount) {
        return table -> {
            Stack stack = simpleStack(Icon.power, amount, Pal.power);
            stack.addListener(Tooltip.Tooltips.getInstance().create("@bar.power", mobile));
            
            table.add(stack).padRight(4f);
        };
    }
    
    public static StatValue heat(float amount) {
        return table ->  {
            Stack stack = simpleStack(Icon.waves, amount / 60f, new Color(1f, 0.22f, 0.22f, 0.8f));
            stack.addListener(Tooltip.Tooltips.getInstance().create("@bar.heat", mobile));
            
            table.add(stack).padRight(4f);
        };
    }
    
    public static StatValue payloads(PayloadStack... stacks) { return payloads(true, stacks); }
    public static StatValue payloads(boolean displayName, PayloadStack... stacks) { return payloads(displayName, true, stacks); }
    public static StatValue payloads(boolean displayName, boolean tooltip, PayloadStack... stacks) {
        return table -> {
            for(PayloadStack stack : stacks){
                table.add(displayPayloads(stack.item, stack.amount, displayName, tooltip)).padRight(4f);
                if (++count % 5 == 0) table.row();
            }
        };
    }
    
    public static Table displayLiquid(Liquid liquid, float amount, boolean showName) { return displayLiquid(liquid, amount, showName, true); }
    public static Table displayLiquid(Liquid liquid, float amount, boolean showName, boolean tooltip) {
        Table t = new Table();
        t.add(floatStack(liquid, amount, tooltip));
        if (showName) t.add(liquid.localizedName).padLeft(4 + amount > 99 ? 4 : 0);
        return t;
    }
    
    public static Table displayPayloads(UnlockableContent item, int amount, boolean showName) { return displayPayloads(item, amount, showName, true); }
    public static Table displayPayloads(UnlockableContent item, int amount, boolean showName, boolean tooltip) {
        Table t = new Table();
        t.add(stack(item.uiIcon, amount, item, tooltip));
        if(showName) t.add(item.localizedName).padLeft(4 + amount > 99 ? 4 : 0);
        return t;
    }
    
    private static Stack stack(TextureRegion region, int amount, @Nullable UnlockableContent content, boolean tooltip) {
        Stack stack = new Stack();
        
        stack.add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(28f).scaling(Scaling.fit);
        }));
        
        if(amount != 0){
            stack.add(new Table(t -> {
                t.left().bottom();
                if (perSecond) t.add(formatAmount(amount / (craftTime / 60f)) + "/s").name("stack amount").style(Styles.outlineLabel);
                else t.add(formatAmount(amount)).name("stack amount").style(Styles.outlineLabel);
                t.pack();
            }));
        }
        
        if (tooltip) withTooltip(stack, content, true);
        stack.addListener(Tooltip.Tooltips.getInstance().create(content.localizedName, mobile));
        
        return stack;
    }
    
    /** A copy of {@link StatValues} stack functions but using a float amount. */
    public static Stack floatStack(Liquid liquid) { return floatStack(liquid.uiIcon, 0, liquid); }
    public static Stack floatStack(LiquidStack stack) { return floatStack(stack.liquid.uiIcon, stack.amount, stack.liquid); }
    public static Stack floatStack(UnlockableContent item, float amount) { return floatStack(item.uiIcon, amount, item); }
    public static Stack floatStack(TextureRegion region, float amount, @Nullable UnlockableContent content) { return floatStack(region, amount, content, true); }
    public static Stack floatStack(UnlockableContent item, float amount, boolean tooltip) { return floatStack(item.uiIcon, amount, item, tooltip); }
    
    private static Stack floatStack(TextureRegion region, float amount, @Nullable UnlockableContent content, boolean tooltip) {
        Stack stack = new Stack();
        
        stack.add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(28f).scaling(Scaling.fit);
        }));
        
        if (amount != 0f) {
            float amountPerSecond = amount * 60f;
            stack.add(new Table(t -> {
                t.left().bottom();
                if (perSecond) t.add(formatAmount(amountPerSecond) + "/s").name("stack amount").style(Styles.outlineLabel);
                else t.add(formatAmount(amountPerSecond * (craftTime / 60f))).name("stack amount").style(Styles.outlineLabel);
                t.pack();
            }));
        }
        
        if (tooltip) withTooltip(stack, content, true);
        stack.addListener(Tooltip.Tooltips.getInstance().create(content.localizedName, mobile));
        
        return stack;
    }
    
    private static Stack simpleStack(TextureRegionDrawable region, float amount, Color color) {
        Stack stack = new Stack();
        
        stack.add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(28f).scaling(Scaling.fit).color(color);
        }));
        
        if(amount != 0f) {
            float amountPerSecond = amount * 60f;
            stack.add(new Table(t -> {
                t.left().bottom();
                if (perSecond) t.add(formatAmount(amountPerSecond) + "/s").name("stack amount").style(Styles.outlineLabel);
                else t.add(formatAmount(amountPerSecond * (craftTime / 60f))).name("stack amount").style(Styles.outlineLabel);
                t.pack();
            }));
        }
        
        return stack;
    }
    
    private static String formatAmount(float amount) {
        if (amount >= 1000f) return UI.formatAmount((long) amount);
        return Strings.autoFixed(amount, 2);
    }
}
