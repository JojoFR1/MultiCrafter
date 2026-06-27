package dev.jojofr.multicrafter.tmi;

import tmi.RecipeEntry;
import tmi.TooManyItems;

public class MultiCrafterRecipeEntry implements RecipeEntry {
    
    @Override
    public void init() { TooManyItems.recipesManager.registerParser(new MultiCrafterRecipeParser()); }
}
