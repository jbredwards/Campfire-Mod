/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.block.state;

import git.jbredwards.campfire.api.capability.CampfireWoodType;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public enum CampfireWoodTypeProperty implements IUnlistedProperty<CampfireWoodType>
{
    INSTANCE;

    @Nonnull
    @Override
    public String getName() { return "stackTex"; }

    @Override
    public boolean isValid(@Nullable final CampfireWoodType value) { return value != null; }

    @Nonnull
    @Override
    public Class<CampfireWoodType> getType() { return CampfireWoodType.class; }

    @Nonnull
    @Override
    public String valueToString(@Nonnull final CampfireWoodType value) { return String.valueOf(value.getRegistryName()); }
}
