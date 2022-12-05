package git.jbredwards.campfire.common.compat.jei;

import git.jbredwards.campfire.common.compat.jei.category.CampfireCategory;
import git.jbredwards.campfire.common.compat.jei.recipe.CampfireRecipeWrapper;
import git.jbredwards.campfire.common.init.ModItems;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipeHandler;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@JEIPlugin
public final class CampfirePlugin implements IModPlugin
{
    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.handleRecipes(CampfireRecipe.class, r -> new CampfireRecipeWrapper(r.inputs, r.output, r.cookTime, r.experience), CampfireCategory.NAME);
        registry.addRecipes(CampfireRecipeHandler.getAll(), CampfireCategory.NAME);
        //campfire can be used for its recipes
        registry.addRecipeCatalyst(new ItemStack(ModItems.CAMPFIRE), CampfireCategory.NAME);
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(CampfireCategory.getOrBuildInstance(registry.getJeiHelpers().getGuiHelper()));
    }
}
