/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.block.util;

import net.minecraft.dispenser.ILocatableSource;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class LocationImpl implements ILocatableSource
{
    @Nonnull
    protected final World world;
    protected final double x, y, z;

    public LocationImpl(@Nonnull final World worldIn, final double xIn, final double yIn, final double zIn) {
        world = worldIn;
        x = xIn;
        y = yIn;
        z = zIn;
    }

    @Nonnull
    @Override
    public World getWorld() { return world; }

    @Override
    public double getX() { return x; }

    @Override
    public double getY() { return y; }

    @Override
    public double getZ() { return z; }
}
