/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.tileentity;

import elucent.albedo.event.GatherLightsEvent;
import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import git.jbredwards.campfire.mod.common.block.BlockColorEmitting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo")
public class TileEntityColorEmitting extends TileEntity implements ILightProvider
{
    public int color = -1;
    public static int getColor(@Nullable final TileEntity tile) {
        return tile instanceof TileEntityColorEmitting ? ((TileEntityColorEmitting)tile).color : -1;
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() { return serializeNBT(); }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() { return new SPacketUpdateTileEntity(pos, 0, getUpdateTag()); }

    @Override
    public void onDataPacket(@Nonnull final NetworkManager net, @Nonnull final SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound compound) {
        super.writeToNBT(compound);
        if(color != -1) compound.setInteger("Color", color);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull final NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("Color", Constants.NBT.TAG_INT)) color = compound.getInteger("Color");
    }

    @Override
    public boolean shouldRefresh(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState oldState, @Nonnull final IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    // ========================
    // ALBEDO MOD SUPPORT (WIP)
    // ========================

    @Optional.Method(modid = "albedo")
    @SideOnly(Side.CLIENT)
    @Override
    public Light provideLight() {
        @Nonnull final float[] rgb = BlockColorEmitting.getColoredLight(this);
        return Light.builder().pos(pos).color(rgb[0], rgb[1], rgb[2]).radius(color == -1 ? 0 : getBlockType().getDefaultState().getLightValue(world, pos)).build();
    }

    @Optional.Method(modid = "albedo")
    @SideOnly(Side.CLIENT)
    @Override
    public void gatherLights(@Nonnull final GatherLightsEvent event, @Nullable final Entity entity) {
        if(color != -1 && hasWorld()) event.add(provideLight());
    }
}
