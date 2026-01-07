/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.jei.category;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import git.jbredwards.campfire.mod.common.compat.jei.recipe.CampfireRecipeWrapper;
import git.jbredwards.campfire.mod.common.init.CampfireItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.config.Constants;
import mezz.jei.plugins.vanilla.furnace.FurnaceRecipeCategory;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class CampfireRecipeCategory extends FurnaceRecipeCategory<CampfireRecipeWrapper> implements GenericRecipeCategory<CampfireRecipeWrapper>
{
    @Nonnull public static final String NAME = "campfire:campfire";

    @Nonnull public final LoadingCache<Integer, IDrawableAnimated> cachedArrows;
    @Nonnull public final IDrawable background, icon;

    public static CampfireRecipeCategory instance;
    protected CampfireRecipeCategory(@Nonnull IGuiHelper guiHelper, @Nonnull ItemStack campfire) {
        super(guiHelper);
        cachedArrows = CacheBuilder.newBuilder().maximumSize(64).build(CacheLoader.from(cookTime -> guiHelper
                .drawableBuilder(Constants.RECIPE_GUI_VANILLA, 82, 128, 24, 17)
                .buildAnimated(cookTime, IDrawableAnimated.StartDirection.LEFT, false)));

        icon = guiHelper.createDrawableIngredient(campfire);
        background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 114, 82, 54);
    }

    @Nonnull
    public static CampfireRecipeCategory getOrBuildInstance(@Nonnull IGuiHelper guiHelper) {
        return instance == null ? (instance = new CampfireRecipeCategory(guiHelper, new ItemStack(CampfireItems.CAMPFIRE))) : instance;
    }

    @Nonnull
    @Override
    public String getUid() { return NAME; }

    @Nonnull
    @Override
    public String getModName() { return "campfire"; }

    @Nonnull
    @Override
    public String getTitle() { return Translator.translateToLocal("gui.campfire.jei.category.campfire"); }

    @Nonnull
    @Override
    public IDrawable getBackground() { return background; }

    @Nonnull
    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) { staticFlame.draw(minecraft, 1, 20); }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull CampfireRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        recipeLayout.getItemStacks().init(inputSlot, true, 0, 0);
        recipeLayout.getItemStacks().init(fuelSlot, true, 0, 36);
        recipeLayout.getItemStacks().init(outputSlot, false, 60, 18);
        setRecipeEnd(recipeLayout, recipeWrapper, ingredients);
    }
}
