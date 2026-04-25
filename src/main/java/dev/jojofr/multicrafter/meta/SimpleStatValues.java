package dev.jojofr.multicrafter.meta;

import arc.Core;
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
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.ui.Styles;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.mobile;

public class SimpleStatValues {
    
    public static StatValue liquids(LiquidStack... liquids) {
        return liquids(true, liquids);
    }
    
    public static StatValue liquids(boolean displayName, LiquidStack... liquids) {
        return table -> {
            for (LiquidStack liquid : liquids) {
                table.add(displayLiquid(liquid.liquid, liquid.amount, displayName)).padRight(5);
            }
        };
    }
    
    public static StatValue power(float amount) {
        return table -> {
            Stack stack = simpleStack(Icon.power, amount, Pal.power);
            stack.addListener(Tooltip.Tooltips.getInstance().create("@bar.power", mobile));
            
            table.add(stack).padRight(5);
        };
    }
    
    public static StatValue heat(float amount) {
        return table ->  {
            Stack stack = simpleStack(Icon.waves, amount, new Color(1f, 0.22f, 0.22f, 0.8f), false);
            stack.addListener(Tooltip.Tooltips.getInstance().create("@bar.heat", mobile));
            
            table.add(stack).padRight(5);
        };
    }
    
    public static StatValue payloads(PayloadStack... stacks){
        return payloads(true, stacks);
    }
    
    public static StatValue payloads(boolean displayName, PayloadStack... stacks){
        return table -> {
            for(PayloadStack stack : stacks){
                table.add(displayPayloads(stack.item, stack.amount, displayName)).padRight(5);
            }
        };
    }
    
    public static Table displayLiquid(Liquid liquid, float amount, boolean showName) {
        Table t = new Table();
        t.add(floatStack(liquid, amount));
        if (showName) t.add(liquid.localizedName).padLeft(4 + amount > 99 ? 4 : 0);
        return t;
    }
    
    public static Table displayPayloads(UnlockableContent item, int amount, boolean showName){
        Table t = new Table();
        t.add(StatValues.stack(item, amount, !showName));
        if(showName) t.add(item.localizedName).padLeft(4 + amount > 99 ? 4 : 0);
        return t;
    }
    
    /** A copy of {@link StatValues} stack functions but using a float amount. */
    private static Stack floatStack(TextureRegion region, float amount, @Nullable UnlockableContent content) {
        return floatStack(region, amount, content, true, true);
    }
    
    public static Stack floatStack(LiquidStack stack) {
        return floatStack(stack.liquid.uiIcon, stack.amount, stack.liquid);
    }
    
    public static Stack floatStack(UnlockableContent item, float amount) {
        return floatStack(item.uiIcon, amount, item);
    }
    
    public static Stack floatStack(UnlockableContent item, float amount, boolean tooltip) {
        return floatStack(item.uiIcon, amount, item, tooltip,  true);
    }
    
    public static Stack floatStack(UnlockableContent item, float amount, boolean tooltip, boolean perSecond) {
        return floatStack(item.uiIcon, amount, item, tooltip,  perSecond);
    }
    
    public static Stack floatStack(Liquid liquid) {
        return floatStack(liquid.uiIcon, 0, liquid);
    }
    
    private static Stack floatStack(TextureRegion region, float amount, @Nullable UnlockableContent content, boolean tooltip, boolean perSecond) {
        Stack stack = new Stack();
        
        stack.add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f).scaling(Scaling.fit);
        }));
        
        if(amount != 0f) {
            float amountPerSecond = perSecond ? amount * 60f : amount;
            stack.add(new Table(t -> {
                t.left().bottom();
                t.add(amountPerSecond >= 1000f ? UI.formatAmount((long) amountPerSecond) : Mathf.round(amountPerSecond) + "").name("stack amount").style(Styles.outlineLabel);
                t.pack();
            }));
        }
        
        StatValues.withTooltip(stack, content, tooltip);
        
        return stack;
    }
    
    private static Stack simpleStack(TextureRegionDrawable region, float amount, Color color) {
        return simpleStack(region, amount, color, true);
    }
    private static Stack simpleStack(TextureRegionDrawable region, float amount, Color color, boolean perSecond) {
        Stack stack = new Stack();
        
        stack.add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f).scaling(Scaling.fit).color(color);
        }));
        
        if(amount != 0f) {
            float amountPerSecond = perSecond ? amount * 60f : amount;
            stack.add(new Table(t -> {
                t.left().bottom();
                t.add(amountPerSecond >= 1000f ? UI.formatAmount((long) amountPerSecond) : Mathf.round(amountPerSecond) + "").name("stack amount").style(Styles.outlineLabel);
                t.pack();
            }));
        }
        
        return stack;
    }
    
}
