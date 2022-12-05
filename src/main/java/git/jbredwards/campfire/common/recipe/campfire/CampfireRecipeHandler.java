package git.jbredwards.campfire.common.recipe.campfire;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public final class CampfireRecipeHandler
{
    @Nonnull
    protected static final Map<ResourceLocation, CampfireRecipe> RECIPES = new HashMap<>();

    @Nonnull
    public static Optional<CampfireRecipe> getRecipeFromInput(@Nonnull ItemStack in) {
        if(in.isEmpty()) return Optional.empty();
        for(CampfireRecipe recipe : RECIPES.values())
            if(recipe.canAccept(in)) return Optional.of(recipe);

        return Optional.empty();
    }

    public static void removeRecipe(@Nonnull ResourceLocation id) { RECIPES.remove(id); }
    public static void createRecipe(@Nonnull ResourceLocation id, @Nonnull List<ItemStack> inputs, @Nonnull ItemStack output, int cookTime, float experience) {
        //do nothing if the item can already be used for a different recipe
        for(ItemStack input : inputs) {
            final Optional<CampfireRecipe> recipe = getRecipeFromInput(input);
            if(recipe.isPresent()) return;
        }

        final CampfireRecipe recipe = new CampfireRecipe(id, inputs, output, cookTime, experience);
        RECIPES.put(recipe.id, recipe);
    }

    @Nonnull
    public static List<CampfireRecipe> getAll() { return ImmutableList.copyOf(RECIPES.values()); }
}
