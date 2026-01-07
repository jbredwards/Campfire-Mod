/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.jei.recipe.dummy;

import com.google.common.collect.ImmutableList;
import git.jbredwards.campfire.mod.common.compat.jei.category.GenericRecipeCategory;
import git.jbredwards.campfire.api.recipe.ingredient.CompoundIngredient;
import git.jbredwards.campfire.mod.common.block.BlockColorEmitting;
import git.jbredwards.campfire.mod.common.item.ItemBlockColored;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import mezz.jei.util.Translator;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author jbred
 *
 */
public final class DyeableRecipeHandler implements ICustomCraftingRecipeWrapper
{
    @Nonnull public final IJeiHelpers helpers;
    @Nonnull public final ItemStack dyeable;

    public DyeableRecipeHandler(@Nonnull final IJeiHelpers helpersIn, @Nonnull final ItemStack dyeableIn) {
        helpers = helpersIn;
        dyeable = dyeableIn;
    }

    @Nonnull
    public static List<DyeableRecipeHandler> generateDummyRecipes(@Nonnull final IJeiHelpers helpers) {
        return ForgeRegistries.ITEMS.getValuesCollection().stream()
                .filter(i -> Block.getBlockFromItem(i) instanceof BlockColorEmitting)
                .flatMap(i -> {
                    @Nonnull final NonNullList<ItemStack> stacks = NonNullList.create();
                    i.getSubItems(CreativeTabs.SEARCH, stacks);
                    return stacks.stream();
                })
                .map(s -> new DyeableRecipeHandler(helpers, s))
                .collect(Collectors.toList());
    }

    @Override
    public void getIngredients(@Nonnull final IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, ImmutableList.<List<ItemStack>>builder()
                .add(Collections.singletonList(dyeable))
                .addAll(helpers.getStackHelper().expandRecipeItemStackInputs(
                        new ArrayList<>(CompoundIngredient.from(
                        "dyeWhite", "dyeOrange", "dyeMagenta", "dyeLightBlue",
                        "dyeYellow", "dyeLime", "dyePink", "dyeGray",
                        "dyeLightGray", "dyeCyan", "dyePurple", "dyeBlue",
                        "dyeBrown", "dyeGreen", "dyeRed", "dyeBlack")
                        .getChildren())))
                .build());

        @Nonnull final ItemStack rainbow = ItemBlockColored.applyColor(dyeable.copy(), 0);
        rainbow.getOrCreateSubCompound("display").setBoolean("campfire:rainbow", true);
        ingredients.setOutput(VanillaTypes.ITEM, rainbow);
    }

    @Override
    public void setRecipe(@Nonnull final IRecipeLayout recipeLayout, @Nonnull final IIngredients ingredients) {
        // add random dyes to recipe
        @Nonnull final List<List<ItemStack>> inputs = new ArrayList<>(ingredients.getInputs(VanillaTypes.ITEM));
        inputs.remove(0);
        Collections.shuffle(inputs);
        inputs.add(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);

        // restore recipe ID tooltip
        recipeLayout.setShapeless();
        recipeLayout.getItemStacks().set(ingredients);
        recipeLayout.getItemStacks().addTooltipCallback(GenericRecipeCategory.createIDTooltipCallback(getRegistryName(),
                s -> Optional.ofNullable(s.getItem().getRegistryName()).map(ResourceLocation::getNamespace).orElse(null)));
    }

    @Override
    public void drawInfo(@Nonnull final Minecraft mc, final int recipeWidth, final int recipeHeight, final int mouseX, final int mouseY) {
        mc.fontRenderer.drawString(Translator.translateToLocal("tooltip.campfire.dyeable"), 60, 46, Color.GRAY.getRGB());
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryName() { return new ResourceLocation("campfire", "builtin/dyeables"); }
}
