package git.jbredwards.campfire.common.compat.jei;

import git.jbredwards.campfire.common.capability.ICampfireType;
import git.jbredwards.campfire.common.compat.jei.category.CampfireCategory;
import git.jbredwards.campfire.common.compat.jei.recipe.CampfireRecipeWrapper;
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
public final class CampfirePlugin implements IModPlugin
{
    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.handleRecipes(CampfireRecipe.class, CampfireRecipeWrapper::new, CampfireCategory.NAME);
        registry.addRecipes(CampfireRecipeHandler.getAll(), CampfireCategory.NAME);

        final ItemStack campfire = new ItemStack(CampfireItems.CAMPFIRE, 1, OreDictionary.WILDCARD_VALUE);
        registry.addRecipeCatalyst(campfire, CampfireCategory.NAME);
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
        registry.addRecipeCategories(CampfireCategory.getOrBuildInstance(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        CampfireCategory.catalysts.addAll(jeiRuntime.getRecipeRegistry().getRecipeCatalysts(CampfireCategory.instance));
    }
}
