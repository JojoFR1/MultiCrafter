package dev.jojofr.multicrafter;

import dev.jojofr.multicrafter.type.DrawRecipe;
import dev.jojofr.multicrafter.type.IOEntry;
import dev.jojofr.multicrafter.type.Recipe;
import dev.jojofr.multicrafter.world.AttributeMultiCrafterBlock;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.world.Block;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawMulti;
import mindustry.world.meta.Attribute;

public class ExampleBlocks {
    public static Block exampleBlock, attributeExampleBlock;
    
    public static void load() {
        exampleBlock = new MultiCrafterBlock("example-multi") {{
            // You can define any vanilla block variable or functions below, as long as it is supported by the game. All values are examples.
            size = 2;
            
            itemCapacity = 15;
            liquidCapacity = 50;
            
            drawer = new DrawDefault();
            
            // A special drawer, 'DrawRecipe', can be used to draw the recipes drawer at its place. You CANNOT have the two versions (above and below) at the same time, it is just a choice.
            drawer = new DrawMulti(new DrawRecipe(), new DrawDefault());
            
            ////// The library specific variables, and functions, all default values. You do not need to define all of them, only the ones you want to change.
            
            // If true, the selection menu will be disabled and the recipe will be selected based off the input and weights.
            // This system is experimental and may not work as expected in all cases.
            autoSelectRecipe = false;
            
            /// Adding a recipe
            recipes.addAll(
                // Multiple ways to create a recipe, the order is: name, input, output, craftTime.
                // The letters 'A', 'B', 'C', 'D', 'E', and 'F' are just to differentiate the recipes, you can use any name you want.
                new Recipe("empty-recipeA"),
                new Recipe("empty-recipeB", new IOEntry()),
                new Recipe("empty-recipeC", new IOEntry(), new IOEntry()),
                new Recipe("empty-recipeD", new IOEntry(), new IOEntry(), 80f),
                // You can use an anonymous class to define a recipe, like Mindustry blocks do. There's no recommended way, it depends on your preference.
                // This is used to document all the available variables and functions, and their default values. You can change any of them.
                // The 'J' prefix is just to differentiate the JSON empty recipe from the Java empty recipe, you can use any name you want.
                new Recipe("Jempty-recipe") {{
                    localizedName = "Empty Recipe";
                    
                    // When 'autoSelectRecipe' is true, this weight will influence which recipe will be selected in case of multiple valid recipes.
                    // The higher weighted recipe will be selected.
                    weight = 1f;
                    
                    // The time, in tick, it takes to craft the recipe.
                    craftTime = 80f;
                    
                    input.withItems(ItemStack.empty).withLiquids(LiquidStack.empty).withPower(0f).withHeat(0f).withPayloads(PayloadStack.with());
                    output.withItems(ItemStack.empty).withLiquids(LiquidStack.empty).withPower(0f).withHeat(0f).withPayloads(PayloadStack.with());
                    // Example for amount to amount/tick conversion:
                    //     10 water/second = 10 / 60 = 0.167 water/tick
                    //     0.83 water/tick = 0.83 * 60 = 50 water/second
                    // It works for power or liquids quantity.
                    
                    craftEffect = Fx.none;
                    updateEffect = Fx.none;
                    updateEffectChance = 0.04f;
                    updateEffectSpread = 4f;
                    warmupSpeed = 0.019f;
                    
                    // Only for recipes with heat
                    warmupRate = 0.15f;
                    overheatScale = 1f;
                    maxEfficiency = 4f;
                    
                    // If true, the crafter will output a random item from the output list, like a Separator/Disassembler.
                    // Only support items, all other output types will be forbidden and will throw an error if defined.
                    randomOutput = false;
                    
                    // If false, the recipe will need to be researched to unlock. Useful for recipes that needs items unlocked later in your progression.
                    unlocked = false;
                    // If true, the recipe will always be unlocked, even after a research tree reset.
                    alwaysUnlocked = false;
                    
                    // If you have previously defined your recipe as unlocked and loaded the game, then later switched to locked,
                    //   the recipe will not automatically be locked. You will need to reset the research tree to lock it again, or lock this specific recipe using the console.
                    // To make a recipe locked, in the console: Vars.content.getByName(ContentType.typeid_UNUSED, "modname-recipename").clearUnlock()
                    
                    // For research, you need to define it inside a Tech Tree. See the 'TestTechTree' class for an example, or the game's vanilla tech tree for reference.
                    
                    // This drawer will be drawn at the place of a 'DrawRecipe' in the block drawer.
                    drawer = new DrawDefault();
                }}
                // Recipes can also be defined using a builder setter style. There's no recommended way, it depends on your preference.
                // They all start with 'with' or 'is' for booleans (they are not getters!), they allow you to chain them together to define a recipe variables.
                // Inputs and outputs are defined in the constructor, they do not have a builder setter.
            );
            
            requirements(Category.crafting, ItemStack.with(Items.copper, 10));
        }};
        attributeExampleBlock = new AttributeMultiCrafterBlock("example-attribute-multi") {{
            // You can define any vanilla block variable or functions below, as long as it is supported by the game. All values are examples.
            size = 2;
            
            itemCapacity = 15;
            liquidCapacity = 50;
            
            drawer = new DrawDefault();
            
            ////// The library specific variables, and functions, all default values. You do not need to define all of them, only the ones you want to change.
            /// This block is a subclass of MultiCrafterBlock, so it inherits all the variables and functions from it. For an example, look at the 'exampleBlock' above.
            
            // Same variables as the vanilla AttributeCrafter
            attribute = Attribute.heat;
            baseEfficiency = 1.0f;
            boostScale = 1.0f;
            maxBoost = 1.0f;
            minEfficiency = -1.0f;
            
            recipes.add(
                new Recipe("empty-recipe-attribute") {{
                    //// All values will take precedence over the block values, if defined. If not defined, the block values will be used.
                    
                    // The values below are considered 'not defined'.
                    // If you want to use the block value, do not define them at all, or keep them as 'null' or 'NaN' (for float values).
                    // If you want to override the block value, define them with a valid value (not NaN or 'null').
                    attribute =  null;
                    baseEfficiency = Float.NaN;
                    boostScale = Float.NaN;
                    maxBoost = Float.NaN;
                    minEfficiency = Float.NaN;
                    
                }}
            );
            
            requirements(Category.crafting, ItemStack.with(Items.copper, 10));
        }};
    }
}
