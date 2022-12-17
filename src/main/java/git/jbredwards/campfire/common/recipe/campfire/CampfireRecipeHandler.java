package git.jbredwards.campfire.common.recipe.campfire;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public final class CampfireRecipeHandler
{
    @Nonnull
    static final List<CampfireRecipe> RECIPES = new ArrayList<>();

    @Nonnull
    public static Optional<CampfireRecipe> getFromInput(@Nonnull ItemStack in, @Nonnull ItemStack campfireTypeIn) {
        if(in.isEmpty()) return Optional.empty();
        for(CampfireRecipe recipe : RECIPES)
            if(recipe.canAccept(in, campfireTypeIn))
                return Optional.of(recipe);

        return Optional.empty();
    }

    //removes the recipe that uses the provided input
    public static void removeInput(@Nonnull ItemStack in) {
        getFromInput(in, ItemStack.EMPTY).ifPresent(RECIPES::remove);
    }

    //remove all recipes with the provided output
    public static void removeOutput(@Nonnull ItemStack out) {
        RECIPES.removeIf(recipe -> ItemHandlerHelper.canItemStacksStack(out, recipe.output));
    }

    public static void createRecipe(@Nonnull CampfireRecipe recipe) {
        if(!recipe.output.isEmpty() && !recipe.inputs.isEmpty()) {
            recipe.inputs.forEach(CampfireRecipeHandler::removeInput);
            RECIPES.add(recipe);
        }
    }

    public static void createRecipe(@Nonnull List<ItemStack> inputsIn, @Nonnull ItemStack output, int cookTime, float experience) {
        createRecipe(CampfireRecipe.of(inputsIn, output, cookTime, experience));
    }

    public static void createRecipe(@Nonnull List<ItemStack> campfireTypes, @Nonnull List<ItemStack> inputsIn, @Nonnull ItemStack output, int cookTime, float experience) {
        createRecipe(CampfireRecipe.of(campfireTypes, inputsIn, output, cookTime, experience));
    }

    @Nonnull
    public static List<CampfireRecipe> getAll() { return Collections.unmodifiableList(RECIPES); }
}
