package git.jbredwards.campfire.common.recipe.campfire;

import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.*;

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
    public static Optional<CampfireRecipe> getFromInput(@Nonnull ItemStack in) {
        if(in.isEmpty()) return Optional.empty();
        for(CampfireRecipe recipe : RECIPES)
            if(recipe.canAccept(in)) return Optional.of(recipe);

        return Optional.empty();
    }

    public static void removeInput(@Nonnull ItemStack in) { getFromInput(in).ifPresent(RECIPES::remove); }
    public static void removeOutput(@Nonnull ItemStack out) {
        RECIPES.removeIf(recipe -> ItemHandlerHelper.canItemStacksStack(out, recipe.output));
    }

    public static void createRecipe(@Nonnull List<ItemStack> inputs, @Nonnull ItemStack output, int cookTime, float experience) {
        createRecipe(CampfireConfigHandler.getAllTypes(), inputs, output, cookTime, experience);
    }

    public static void createRecipe(@Nonnull List<ItemStack> campfireTypes, @Nonnull List<ItemStack> inputs, @Nonnull ItemStack output, int cookTime, float experience) {
        if(!output.isEmpty()) {
            inputs.forEach(CampfireRecipeHandler::removeInput);
            RECIPES.add(new CampfireRecipe(campfireTypes, inputs, output, cookTime, experience));
        }
    }

    @Nonnull
    public static List<CampfireRecipe> getAll() { return Collections.unmodifiableList(RECIPES); }
}
