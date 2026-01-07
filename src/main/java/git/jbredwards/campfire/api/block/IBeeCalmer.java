/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.block;

import git.jbredwards.campfire.mod.common.block.AbstractCampfire;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;

/**
 * Have your block implement this if it can calm beehives (such as the ones added by Future MC).
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public interface IBeeCalmer
{
    /**
     * @param state IBlockState to test.
     * @return True if this block can be used to calm beehives.
     *
     * @throws NullPointerException If state is null.
     * @since 2.0.0
     */
    default boolean canCalmBeeHive(@Nonnull final IBlockState state) { return state.getValue(AbstractCampfire.LIT); }
}
