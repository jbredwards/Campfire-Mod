/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Called by {@link World#setBlockState(BlockPos, IBlockState, int)} to update the input state
 * to what it should be. For example, this is used by fire to become soul fire if BPNether is installed.
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public interface IHasWorldState
{
    /**
     * @param world World.
     * @param pos BlockPos.
     * @param state IBlockState.
     * @return The state to set in the world (called by {@link World#setBlockState(BlockPos, IBlockState, int)}).
     * This can be thought of as an improved version of {@link IBlockState#getActualState(net.minecraft.world.IBlockAccess, BlockPos) IBlockState::getActualState()}.
     *
     * @throws NullPointerException If world, pos, or state are null.
     * @since 2.0.0
     */
    @Nonnull
    IBlockState getStateForWorld(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state);
}
