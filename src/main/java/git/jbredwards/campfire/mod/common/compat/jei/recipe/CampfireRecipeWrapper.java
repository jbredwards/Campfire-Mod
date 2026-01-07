/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.jei.recipe;

import com.google.common.collect.ImmutableList;
import git.jbredwards.campfire.api.CampfireAPI;
import git.jbredwards.campfire.mod.common.compat.jei.category.CampfireRecipeCategory;
import git.jbredwards.campfire.api.recipe.campfire.CampfireRecipe;
import git.jbredwards.campfire.mod.common.item.ItemCampfire;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author jbred
 *
 */
public class CampfireRecipeWrapper implements ICraftingRecipeWrapper
{
    @Nonnull public final CampfireRecipe<?> recipe;
    @Nonnull protected final List<ItemStack> validCampfires;

    public CampfireRecipeWrapper(@Nonnull final CampfireRecipe<?> recipeIn, @Nonnull final ItemCampfire<?> campfire) {
        validCampfires = campfire.campfireType.getWoodTypes().getValuesCollection().stream()
                .filter(wt -> recipeIn.woodTypes.test(wt.asItemStack()))
                .map(wt -> ItemCampfire.applyType(new ItemStack(campfire), wt))
                .collect(Collectors.toList());
        recipe = recipeIn;
    }

    @Override
    public void getIngredients(@Nonnull final IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, ImmutableList.of(Arrays.asList(recipe.input.getMatchingStacks()), validCampfires));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.output);
    }

    @Override
    public void drawInfo(@Nonnull final Minecraft minecraft, final int recipeWidth, final int recipeHeight, final int mouseX, final int mouseY) {
        CampfireRecipeCategory.instance.cachedArrows.getUnchecked(recipe.cookTime).draw(minecraft, 24, 18);

        if(recipe.experience > 0) {
            final String experienceString = Translator.translateToLocalFormatted("gui.campfire.jei.category.campfire.experience", recipe.experience);
            minecraft.fontRenderer.drawString(experienceString, recipeWidth - minecraft.fontRenderer.getStringWidth(experienceString), 0, 0xFF808080);
        }

        final String cookTimeString = Translator.translateToLocalFormatted("gui.campfire.jei.category.campfire.cookTime", recipe.cookTime / 20f);
        minecraft.fontRenderer.drawString(cookTimeString, recipeWidth - minecraft.fontRenderer.getStringWidth(cookTimeString), 45, 0xFF808080);
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() { return recipe.getRegistryName(); }
}
