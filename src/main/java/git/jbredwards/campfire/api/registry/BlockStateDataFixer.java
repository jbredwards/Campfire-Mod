/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.registry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class BlockStateDataFixer implements IFixableData
{
    @Nonnull
    public final Remapper remapper;
    public final int version;

    public BlockStateDataFixer(final int versionIn, @Nonnull final Remapper remapperIn) {
        version = versionIn;
        remapper = remapperIn;
    }

    @Override
    public int getFixVersion() {
        return version;
    }

    @Nonnull
    @Override
    public NBTTagCompound fixTagCompound(@Nonnull final NBTTagCompound compound) {
        @Nonnull final NBTTagCompound level = compound.getCompoundTag("Level");
        @Nonnull final NBTTagList sections = level.getTagList("Sections", Constants.NBT.TAG_COMPOUND);

        for(int i = 0; i < sections.tagCount(); i++) {
            @Nonnull final NBTTagCompound section = sections.getCompoundTagAt(i);

            @Nonnull final byte[] blockIDs = section.getByteArray("Blocks");
            /* Use a blank extended ID array if not present */
            @Nonnull final NibbleArray extIDs = section.hasKey("Add", Constants.NBT.TAG_BYTE_ARRAY) ? new NibbleArray(section.getByteArray("Add")) : new NibbleArray();
            @Nonnull final NibbleArray metadataArray = new NibbleArray(section.getByteArray("Data"));

            for(int pos = 0; pos < blockIDs.length; pos++) {
                final int x = pos & 15, y = pos >> 8 & 15, z = pos >> 4 & 15;

                /* Find the real block ID by combining the extended ID and the normal ID */
                final int blockID = extIDs.get(x, y, z) << 8 | (blockIDs[pos] & 255);
                final int blockMeta = metadataArray.get(x, y, z);

                final int remappedMeta = remapper.remapMeta(blockID, blockMeta);
                if(blockMeta != remappedMeta) metadataArray.set(x, y, z, remappedMeta);
            }
        }

        return compound;
    }

    @FunctionalInterface
    public interface Remapper
    {
        int remapMeta(final int blockID, final int blockMeta);
    }
}
