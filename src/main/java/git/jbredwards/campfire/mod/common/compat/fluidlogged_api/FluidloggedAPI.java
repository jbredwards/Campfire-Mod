/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.fluidlogged_api;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

/**
 * handles optional fluidlogged api compat, class exists to prevent CNFE while fluidlogged api isn't installed
 * @author jbred
 *
 */
public final class FluidloggedAPI
{
    public static boolean isFluidlogged(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        return !FluidState.get(world, pos).isEmpty();
    }
}
