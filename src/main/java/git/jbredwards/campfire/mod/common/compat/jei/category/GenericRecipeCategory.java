/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.jei.category;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import mezz.jei.startup.ForgeModIdHelper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public interface GenericRecipeCategory<V extends ICraftingRecipeWrapper> extends IRecipeCategory<V>
{
    /**
     * @return The mod id that owns this recipe category, not the mod name.
     */
    @Nonnull
    @Override
    String getModName();

    /**
     * To be called at the end of {@link IRecipeCategory#setRecipe(IRecipeLayout, IRecipeWrapper, IIngredients) IRecipeCategory.setRecipe}.
     *
     * @param recipeLayout  The layout that needs its properties set.
     * @param recipeWrapper The recipeWrapper, for extra information.
     * @param ingredients   The ingredients, already set by the recipeWrapper
     *
     * @throws NullPointerException If any parameters are null.
     * @since 2.0.0
     * @author jbred
     */
    default void setRecipeEnd(@Nonnull final IRecipeLayout recipeLayout, @Nonnull final ICraftingRecipeWrapper recipeWrapper, @Nonnull final IIngredients ingredients) {
        if(recipeWrapper instanceof ICustomCraftingRecipeWrapper) {
            ((ICustomCraftingRecipeWrapper)recipeWrapper).setRecipe(recipeLayout, ingredients);
            return;
        }

        recipeLayout.getItemStacks().set(ingredients);
        @Nullable final ResourceLocation registryName = recipeWrapper.getRegistryName();
        if(registryName != null && !registryName.getNamespace().equals(getModName())) {
            recipeLayout.getItemStacks().addTooltipCallback(createIDTooltipCallback(registryName,
                    s -> Optional.ofNullable(s.getItem().getRegistryName()).map(ResourceLocation::getNamespace).orElse(null)));
            recipeLayout.getFluidStacks().addTooltipCallback(createIDTooltipCallback(registryName, FluidRegistry::getModId));
        }
    }

    /**
     * @param recipeID Registry ID of a specific recipe.
     * @param ingredientNamespaceGetter A function returning the namespace of an ingredient.
     * @return An {@link ITooltipCallback} that mimics the one used by
     * {@link mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory#setRecipe(IRecipeLayout, IRecipeWrapper, IIngredients) CraftingRecipeCategory}.
     * @param <V> The ingredient type for the returned tooltip callback.
     *
     * @throws NullPointerException If recipeID or ingredientModIDGetter are null.
     * @since 2.0.0
     * @author jbred
     */
    @Nonnull
    static <V> ITooltipCallback<V> createIDTooltipCallback(@Nonnull final ResourceLocation recipeID, @Nonnull final Function<V, String> ingredientNamespaceGetter) {
        return (slotIndex, input, ingredient, tooltip) -> {
            if(!input) {
                @Nullable final String ingredientNamespace = ingredientNamespaceGetter.apply(ingredient);
                @Nonnull final String recipeNamespace = recipeID.getNamespace();

                if(ingredientNamespace != null && !recipeNamespace.equals(ingredientNamespace)) {
                    @Nullable final String modName = ForgeModIdHelper.getInstance().getFormattedModNameForModId(recipeNamespace);
                    if(modName != null) tooltip.add(TextFormatting.GRAY + Translator.translateToLocalFormatted("jei.tooltip.recipe.by", modName));
                }

                final boolean showAdvanced = Minecraft.getMinecraft().gameSettings.advancedItemTooltips || GuiScreen.isShiftKeyDown();
                if(showAdvanced) tooltip.add(TextFormatting.DARK_GRAY + Translator.translateToLocalFormatted("jei.tooltip.recipe.id", recipeID.toString()));
            }
        };
    }
}
