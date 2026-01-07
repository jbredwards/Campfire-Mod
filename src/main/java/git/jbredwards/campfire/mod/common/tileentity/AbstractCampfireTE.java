/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.tileentity;

import git.jbredwards.campfire.api.block.campfire.ICampfireSettings;
import git.jbredwards.campfire.mod.common.block.AbstractCampfire;
import net.minecraft.block.material.MapColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public abstract class AbstractCampfireTE extends TileEntityColorEmitting implements ITickable
{
    public int forcedSmokeColor = -1;
    //randomly ticks down over time (if enabled), when this reaches 0 the fire will burn out.
    //the logic for this is handled in the AbstractCampfire block class
    public int fireStrength;
    public boolean isLegacyFireStrength = false;

    public boolean isLit() { return (getBlockMetadata() & 2) != 0; }
    public boolean isSignal() { return (getBlockMetadata() & 4) != 0; }
    public boolean isPowered() { return (getBlockMetadata() & 1) != 0; }

    @Nonnull
    public Optional<AbstractCampfire> getBlock() {
        return getBlockType() instanceof AbstractCampfire ? Optional.of((AbstractCampfire)getBlockType()) : Optional.empty();
    }

    @SideOnly(Side.CLIENT)
    public void addParticles() {
        final Optional<AbstractCampfire> block = getBlock();
        if(block.isPresent() && block.get().isSmokey() && world.rand.nextFloat() < 0.11) {
            final int smokeColor = getSmokeColor();
            final int fallbackColor = getFallbackColor();

            for(int i = 0; i < world.rand.nextInt(2) + 2; i++)
                block.get().addParticles(this, smokeColor, fallbackColor, forcedSmokeColor != -1, isSignal(), isPowered(), -1);
        }
    }

    public int getFallbackColor() {
        return forcedSmokeColor != -1 || !getBlock().isPresent() || !getBlock().get().campfireSettings.doesSmokeFollowDye() ? forcedSmokeColor : color;
    }

    //returns the map color below, or -1 if this shouldn't use that color
    public int getSmokeColor() {
        if(forcedSmokeColor != -1 || !isPowered()
        || !getBlock().isPresent() || getBlock().get().campfireSettings.getPoweredAction() != ICampfireSettings.PoweredAction.COLOR) return -1;
        final MapColor mapColor = world.getBlockState(pos.down(isSignal() ? 2 : 1)).getMapColor(world, pos.down(isSignal() ? 2 : 1));
        return mapColor != MapColor.AIR ? mapColor.colorValue : -1;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        if(forcedSmokeColor != -1) compound.setInteger("ForcedSmokeColor", forcedSmokeColor);

        compound.setInteger(isLegacyFireStrength ? "FireStrength" : "FireTime", fireStrength);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("ForcedSmokeColor", Constants.NBT.TAG_INT)) forcedSmokeColor = compound.getInteger("ForcedSmokeColor");
        if(compound.hasKey("FireTime", Constants.NBT.TAG_INT)) fireStrength = compound.getInteger("FireTime");

        // compat with old v1 worlds
        else if(compound.hasKey("FireStrength", Constants.NBT.TAG_INT)) {
            fireStrength = compound.getInteger("FireStrength");
            if(fireStrength != -1) isLegacyFireStrength = true;
        }
    }
}
