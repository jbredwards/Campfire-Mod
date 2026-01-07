/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.jei;

import git.jbredwards.campfire.api.CampfireAPI;
import git.jbredwards.campfire.mod.common.block.AbstractCampfire;
import git.jbredwards.campfire.mod.common.block.BlockBrazier;
import git.jbredwards.campfire.mod.common.block.BlockCampfire;
import git.jbredwards.campfire.mod.common.capability.ICampfireWoodType;
import git.jbredwards.campfire.mod.common.compat.jei.category.CampfireRecipeCategory;
import git.jbredwards.campfire.mod.common.compat.jei.recipe.dummy.CampfireCraftingRecipeHandler;
import git.jbredwards.campfire.mod.common.compat.jei.recipe.CampfireRecipeWrapper;
import git.jbredwards.campfire.mod.common.compat.jei.recipe.dummy.DyeableRecipeHandler;
import git.jbredwards.campfire.mod.common.init.CampfireItems;
import git.jbredwards.campfire.mod.common.item.ItemCampfire;
import git.jbredwards.campfire.api.recipe.campfire.CampfireRecipe;
import git.jbredwards.campfire.mod.common.recipe.crafting.BrazierCraftingRecipe;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.plugins.vanilla.crafting.ShapedOreRecipeWrapper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Optional;
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
        registry.handleRecipes(CampfireRecipe.Normal.class, recipe -> new CampfireRecipeWrapper(recipe, CampfireItems.CAMPFIRE), CampfireRecipeCategory.NAME);
        registry.addRecipes(CampfireAPI.CAMPFIRE_RECIPES.getValuesCollection(), CampfireRecipeCategory.NAME);
        // registry.addRecipeCatalyst(new ItemStack(CampfireItems.CAMPFIRE, 1, OreDictionary.WILDCARD_VALUE), CampfireJEICategory.NAME);

        // add dummy recipes to display in JEI for campfires and dyeable items
        final IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        registry.addRecipes(CampfireCraftingRecipeHandler.generateDummyRecipes(jeiHelpers), VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipes(DyeableRecipeHandler.generateDummyRecipes(jeiHelpers), VanillaRecipeCategoryUid.CRAFTING);

        //show extinguished brazier recipes
        final Set<Ingredient> instances = ObfuscationReflectionHelper.getPrivateValue(Ingredient.class, null, "INSTANCES");
        registry.handleRecipes(BrazierCraftingRecipe.class, recipe -> new ShapedOreRecipeWrapper(jeiHelpers, recipe) {
            @Override
            public void getIngredients(@Nonnull IIngredients ingredients) {
                ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
                ingredients.setInputLists(VanillaTypes.ITEM, jeiHelpers.getStackHelper().expandRecipeItemStackInputs(
                    recipe.getIngredients().stream().map(ingredient -> {
                        final ItemStack[] stacks = ingredient.getMatchingStacks().clone();
                        for(int i = 0; i < stacks.length; i++) {
                            if(stacks[i].getItem() instanceof ItemCampfire
                            && ((AbstractCampfire<?>)((ItemCampfire<?>)stacks[i].getItem()).getBlock()).campfireSettings.unlitOnCraft()) {
                                final ItemStack unlit = stacks[i].copy();
                                unlit.setItemDamage(1);
                                stacks[i] = unlit;
                            }
                        }

                        final Ingredient input = Ingredient.fromStacks(stacks);
                        instances.remove(input);
                        return input;
                    }
                ).collect(Collectors.toList())));
            }
        }, VanillaRecipeCategoryUid.CRAFTING);

        //add description of how to obtain campfire ash
        registry.addIngredientInfo(new ItemStack(CampfireItems.CAMPFIRE_ASH), VanillaTypes.ITEM, "gui.campfire.jei.info.campfire_ash");
    }

    @Override
    public void registerItemSubtypes(@Nonnull ISubtypeRegistry registry) {
        ForgeRegistries.ITEMS.getEntries().stream()
                .filter(e -> e.getValue() instanceof ItemBlock && ((ItemBlock)e.getValue()).getBlock() instanceof BlockBrazier)
                .forEach(e -> registry.registerSubtypeInterpreter(e.getValue(), stack -> e.getKey().toString()));
        ForgeRegistries.ITEMS.getEntries().stream()
                .filter(e -> e.getValue() instanceof ItemBlock && ((ItemBlock)e.getValue()).getBlock() instanceof BlockCampfire)
                .forEach(e -> registry.registerSubtypeInterpreter(e.getValue(), stack -> e.getKey() + " " + Optional
                        .ofNullable(ICampfireWoodType.get(stack))
                        .flatMap(ICampfireWoodType::getOptional)
                        .map(wt -> String.valueOf(wt.getRegistryName()))
                        .orElse("")));
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(CampfireRecipeCategory.getOrBuildInstance(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        // CampfireJEICategory.catalysts.addAll(jeiRuntime.getRecipeRegistry().getRecipeCatalysts(CampfireJEICategory.instance));
    }
}
