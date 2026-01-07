/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.jei.recipe.dummy;

import git.jbredwards.campfire.mod.common.recipe.crafting.CampfireCraftingRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.plugins.vanilla.crafting.ShapedOreRecipeWrapper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.commons.lang3.ArrayUtils;

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
public class CampfireCraftingRecipeHandler extends ShapedOreRecipeWrapper
{
    @Nonnull
    public final ResourceLocation parentName;
    public CampfireCraftingRecipeHandler(@Nonnull final IJeiHelpers helpers, @Nonnull final ResourceLocation parent, @Nonnull final ShapedOreRecipe recipe) {
        super(helpers, recipe);
        parentName = parent;
    }

    @Nonnull
    public static List<CampfireCraftingRecipeHandler> generateDummyRecipes(@Nonnull final IJeiHelpers helpers) {
        return ForgeRegistries.RECIPES.getEntries().stream()
                .filter(e -> e.getValue() instanceof CampfireCraftingRecipe).flatMap(e -> {
                    @Nonnull final CampfireCraftingRecipe recipe = (CampfireCraftingRecipe)e.getValue();
                    return Arrays.stream(recipe.woodTypes.getMatchingStacks()).map(type ->
                            new CampfireCraftingRecipeHandler(helpers, e.getKey(),
                            new ShapedOreRecipe(recipe.getGroupAsLocation(),
                                recipe.campfire.applyType(type),
                                ArrayUtils.add(recipe.in, type))));
                }).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() { return parentName; }
}
