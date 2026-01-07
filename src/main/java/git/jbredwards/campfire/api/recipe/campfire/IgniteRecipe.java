/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.recipe.campfire;

import git.jbredwards.campfire.api.recipe.IGenericRecipe;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Allows users to specify which items can be used to ignite campfires.
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public class IgniteRecipe extends IForgeRegistryEntry.Impl<IgniteRecipe> implements IGenericRecipe<IgniteRecipe>
{
    @Nonnull public Predicate<ItemStack> condition;
    @Nonnull public BiConsumer<World, BlockPos> soundAction;
    @Nonnull public Predicate<ITooltipFlag> showTooltip = tooltipFlag -> false;
    public boolean consume; // If this is true, the item will be fully consumed. Otherwise, only durability will be consumed.

    public IgniteRecipe(@Nonnull final Predicate<ItemStack> conditionIn, @Nonnull final BiConsumer<World, BlockPos> soundActionIn, final boolean consumeIn) {
        condition = Objects.requireNonNull(conditionIn, "Null condition!");
        soundAction = Objects.requireNonNull(soundActionIn, "Null sound action!");
        consume = consumeIn;
    }

    /**
     * Utility constructor that generates the sound action.
     */
    public IgniteRecipe(@Nonnull final Predicate<ItemStack> conditionIn, @Nonnull final SoundEvent sound, final boolean consumeIn) {
        this(conditionIn, (world, pos) -> world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1, 1), consumeIn);
    }

    /**
     * Utility constructor that generates the sound action (using vanilla sounds).
     */
    public IgniteRecipe(@Nonnull final Predicate<ItemStack> conditionIn, final boolean consumeIn) {
        this(conditionIn, consumeIn
                ? (world, pos) -> world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1, (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2f + 1)
                : (world, pos) -> world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, world.rand.nextFloat() * 0.4f + 0.8f),
                consumeIn);
    }

    @Nonnull
    @Override
    public final Collection<Predicate<ItemStack>> getInputs() { return Collections.singletonList(condition); }

    @Nonnull
    @Override
    public final Collection<ItemStack> getOutputs() { return Collections.emptyList(); }
}
