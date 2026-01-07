/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.recipe.campfire;

import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.api.recipe.IGenericRecipe;
import git.jbredwards.campfire.mod.common.capability.ICampfireWoodType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;

/**
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public abstract class CampfireRecipe<V extends CampfireRecipe<V>> extends IForgeRegistryEntry.Impl<V> implements IGenericRecipe<V>
{
    public static final class Normal extends CampfireRecipe<Normal>
    {
        public Normal(@Nonnull final ItemStack outputIn, @Nonnull final Predicate<ItemStack> woodTypesIn,
                      @Nonnull final Object inputIn, final int cookTimeIn, final float experienceIn) {
            super(outputIn, woodTypesIn, inputIn, cookTimeIn, experienceIn);
        }
    }

    @Nonnull public Predicate<ItemStack> woodTypes;
    @Nonnull public Ingredient input;
    @Nonnull public ItemStack output;
    public int cookTime;
    public float experience;

    public CampfireRecipe(@Nonnull final ItemStack outputIn, @Nonnull final Predicate<ItemStack> woodTypesIn,
                          @Nonnull final Object inputIn, final int cookTimeIn, final float experienceIn) {
        assert cookTimeIn > 0;
        assert experienceIn >= 0;

        woodTypes = Objects.requireNonNull(woodTypesIn);
        input = Objects.requireNonNull(CraftingHelper.getIngredient(inputIn));
        output = Objects.requireNonNull(outputIn);
        experience = experienceIn;
        cookTime = cookTimeIn;
    }

    public boolean accepts(@Nonnull final ItemStack inputIn, @Nonnull final ICampfireWoodType woodTypeIn) {
        @Nullable final CampfireWoodType<?> woodType = woodTypeIn.get();
        return woodType != null && woodTypes.test(woodType.asItemStack()) && input.test(inputIn);
    }

    @Nonnull
    @Override
    public final Collection<Ingredient> getInputs() { return Collections.singletonList(input); }

    @Nonnull
    @Override
    public final Collection<ItemStack> getOutputs() { return Collections.singletonList(output); }
}
