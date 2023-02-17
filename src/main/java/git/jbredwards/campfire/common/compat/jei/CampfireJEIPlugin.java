package git.jbredwards.campfire.common.compat.jei;

import git.jbredwards.campfire.common.capability.ICampfireType;
import git.jbredwards.campfire.common.compat.jei.category.CampfireJEICategory;
import git.jbredwards.campfire.common.compat.jei.recipe.CampfireJEIRecipeWrapper;
import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.init.CampfireItems;
import git.jbredwards.campfire.common.item.ItemCampfire;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipeHandler;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipe;
import git.jbredwards.campfire.common.recipe.crafting.BrazierCraftingRecipe;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.plugins.vanilla.crafting.ShapedOreRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

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

        //show extinguished brazier recipes
        final IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        final Set<Ingredient> instances = ObfuscationReflectionHelper.getPrivateValue(Ingredient.class, null, "INSTANCES");
        registry.handleRecipes(BrazierCraftingRecipe.class, recipe -> new ShapedOreRecipeWrapper(jeiHelpers, recipe) {
            @Override
            public void getIngredients(@Nonnull IIngredients ingredients) {
                if(!CampfireConfigHandler.unlitOnCraft) {
                    super.getIngredients(ingredients);
                    return;
                }

                ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
                ingredients.setInputLists(VanillaTypes.ITEM, jeiHelpers.getStackHelper().expandRecipeItemStackInputs(
                    recipe.getIngredients().stream().map(ingredient -> {
                        final ItemStack[] stacks = ingredient.getMatchingStacks().clone();
                        for(int i = 0; i < stacks.length; i++) {
                            if(stacks[i].getItem() instanceof ItemCampfire) {
                                final ItemStack unlit = stacks[i].copy();
                                unlit.setItemDamage(1);
                                stacks[i] = unlit;
                            }
                        }

                        final Ingredient input = Ingredient.fromStacks(stacks);
                        instances.remove(input); //prevents memory leak
                        return input;
                    }
                ).collect(Collectors.toList())));
            }
        }, VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public void registerItemSubtypes(@Nonnull ISubtypeRegistry registry) {
        registry.registerSubtypeInterpreter(CampfireItems.BRAZIER, stack -> "");
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
