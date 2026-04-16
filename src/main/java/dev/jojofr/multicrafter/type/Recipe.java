package dev.jojofr.multicrafter.type;

import mindustry.content.Fx;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Effect;

public class Recipe extends UnlockableContent {
    public final IOEntry input, output;
    
    public float craftTime = 80;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public float updateEffectChance = 0.04f;
    public float updateEffectSpread = 4f;
    public float warmupSpeed = 0.019f;
    
    public Recipe(String name) { this(name, new IOEntry(), new IOEntry()); }
    public Recipe(String name, IOEntry input) { this(name, input, new IOEntry()); }
    public Recipe(String name, IOEntry input, IOEntry output) {
        super(name);
        this.input = input;
        this.output = output;
    }
    
    @Override
    public ContentType getContentType() {
        return ContentType.loadout_UNUSED;
    }
}
