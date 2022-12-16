package git.jbredwards.campfire.common.compat.jei;

import git.jbredwards.campfire.common.capability.ICampfireType;
import git.jbredwards.campfire.common.compat.jei.category.CampfireJEICategory;
import git.jbredwards.campfire.common.compat.jei.recipe.CampfireJEIRecipeWrapper;
import git.jbredwards.campfire.common.init.CampfireItems;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipeHandler;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipe;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@JEIPlugin
public final class CampfireJEIPlugin implements IModPlugin
{
    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.handleRecipes(CampfireRecipe.class, CampfireJEIRecipeWrapper::new, CampfireJEICategory.NAME);
        registry.addRecipes(CampfireRecipeHandler.getAll(), CampfireJEICategory.NAME);

        final ItemStack campfire = new ItemStack(CampfireItems.CAMPFIRE, 1, OreDictionary.WILDCARD_VALUE);
        registry.addRecipeCatalyst(campfire, CampfireJEICategory.NAME);
    }

    @Override
    public void registerItemSubtypes(@Nonnull ISubtypeRegistry registry) {
        registry.registerSubtypeInterpreter(CampfireItems.CAMPFIRE, stack -> {
            final ICampfireType type = ICampfireType.get(stack);
            return type != null ? type.get().serializeNBT().toString() : "";
        });
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(CampfireJEICategory.getOrBuildInstance(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        CampfireJEICategory.catalysts.addAll(jeiRuntime.getRecipeRegistry().getRecipeCatalysts(CampfireJEICategory.instance));
    }
}
