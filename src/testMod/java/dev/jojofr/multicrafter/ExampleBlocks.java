package dev.jojofr.multicrafter;

import dev.jojofr.multicrafter.type.DrawRecipe;
import dev.jojofr.multicrafter.type.IOEntry;
import dev.jojofr.multicrafter.type.Recipe;
import dev.jojofr.multicrafter.world.AttributeMultiCrafterBlock;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
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
            // A special drawer, 'DrawRecipe', can be used to draw the recipes drawer at its place. The two versions (above and below) are just a choice.
            drawer = new DrawMulti(new DrawRecipe(), new DrawDefault());
            
            ////// The library specific variables, and functions, all default values. You do not need to define all of them, only the ones you want to change.
            
            // If true, the selection menu will be disabled and the recipe will be selected based off the input and weights.
            // This system is experimental and may not work as expected in all cases.
            autoSelectRecipe = false;
            
            /// Adding a recipe
            // This is the new way to add a recipe, it is recommended over manipulating the recipes list directly.
            addRecipe(
                // Inputs and outputs are defined in the constructor, they do not have a builder setter.
                // You can have an empty recipe, or only input by not defining any input or output.
                // If you want no input but output, you can use 'in -> {}' which will be an empty input, or 'out -> {}' for an empty output.
                new Recipe("recipe-example",
                    in -> in.withItems().withLiquids().withPower(0).withHeat(0).withPayloads(),
                    out -> out.withItems().withLiquids().withPower(0).withHeat(0).withPayloads(),
                    80f
                )   // The name displayed in-game instead of the internal name. It is recommended to use bundles for localization.
                    // For bundles: "recipe.name.key=value", key can be: 'name', 'description', 'details' or 'credit'.
                    .withLocalizedName("Example Recipe")
                    // When 'autoSelectRecipe' is true, this weight will influence which recipe will be selected in case of multiple valid recipes.
                    // The higher weighted recipe will be selected.
                    .withWeight(1f)
                    // The time, in tick, it takes to craft the recipe.
                    .withCraftTime(80f)
                    .withCraftEffect(Fx.none)
                    .withUpdateEffect(Fx.none, 0.04f, 4f)
                    .withWarmupSpeed(0.019f)
                    // Only for recipes with heat
                    .withWarmupRate(0.15f)
                    .withOverheatScale(1f)
                    .withMaxEfficiency(4f)
                    // If true, the crafter will output a random item from the output list, like a Separator/Disassembler.
                    // Only support items, all other output types will be forbidden and will throw an error if defined.
                    .isRandomOutput(false)
                    // If false, the recipe will need to be researched to unlock. Useful for recipes that needs items unlocked later in your progression.
                    .isUnlocked(false)
                    // If true, the recipe will always be unlocked, even after a research tree reset.
                    .isAlwaysUnlocked(false)
                    // If you have previously defined your recipe as unlocked and loaded the game, then later switched to locked,
                    //   the recipe will not automatically be locked. You will need to reset the research tree to lock it again, or lock this specific recipe using the console.
                    // To make a recipe locked, in the console: Vars.content.getByName(ContentType.typeid_UNUSED, "modname-recipename").clearUnlock()
                    
                    // For research, you need to define it inside a Tech Tree. See the 'TestTechTree' class for an example, or the game's vanilla tech tree for reference.
                    
                    // This drawer will be drawn at the place of a 'DrawRecipe' in the block drawer.
                    .withDrawer(new DrawDefault())
            );
            
            // This is the legacy way to add a recipe, it is still supported for backward compatibility, but it is not recommended. Use the 'addRecipe' function instead, seen above.
            recipes.add(
                // This is the deprecated way to add a recipe, it is still supported but not recommended.
                // Use the callback constructor instead, as seen above.
                new Recipe("deprecated-recipe-def", new IOEntry(), new IOEntry(), 80f)
            );
            
            // Back to the default vanilla game variables, all values are examples.
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
            
            addRecipe(
                new Recipe("empty-recipe-attribute")
                    // The values below are considered 'not defined'.
                    // If you want to use the block value, do not define them at all.
                    // If you want to override the block value, define them with a valid value (not NaN or 'null').
                    .withAttribute(null)
                    .withBaseEfficiency(Float.NaN)
                    .withBoostScale(Float.NaN)
                    .withMaxBoost(Float.NaN)
                    .withMinEfficiency(Float.NaN)
            );
            
            requirements(Category.crafting, ItemStack.with(Items.copper, 10));
        }};
    }
}
