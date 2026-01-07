/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A general purpose capability storage class.
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public class CapabilityStorage<T extends INBTSerializable<NBTBase>> implements Capability.IStorage<T>
{
    @Nullable
    @Override
    public NBTBase writeNBT(@Nonnull final Capability<T> capability, @Nonnull final T instance,
                            @Nullable final EnumFacing side) {
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(@Nonnull final Capability<T> capability, @Nonnull final T instance,
                        @Nullable final EnumFacing side, @Nullable final NBTBase nbt) {
        instance.deserializeNBT(nbt);
    }
}
