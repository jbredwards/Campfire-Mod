/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.tileentity.slot;

import git.jbredwards.campfire.mod.Campfire;
import git.jbredwards.campfire.mod.common.block.util.LocationImpl;
import git.jbredwards.campfire.mod.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.mod.common.message.MessageSyncCampfireSlot;
import git.jbredwards.campfire.mod.common.tileentity.TileEntityCampfire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public class CampfireSlotInfo implements INBTSerializable<NBTTagCompound>
{
    @Nullable
    public final TileEntityCampfire tile;
    public final int slotIndex; //needed to sync with client

    public ItemStack stack = ItemStack.EMPTY, output = ItemStack.EMPTY;
    public int maxCookTime, cookTime;
    public float experience;
    public boolean isActive = true;
    public float itemRotation; //client-side item rotation
    public double offsetX, offsetY, offsetZ; //relative to the TileEntity's center position

    public CampfireSlotInfo(@Nullable TileEntityCampfire tileIn, int slotIndexIn) {
        tile = tileIn;
        slotIndex = slotIndexIn;
    }

    @Nonnull
    public CampfireSlotInfo setOffset(double xOffsetIn, double yOffsetIn, double zOffsetIn) {
        offsetX = xOffsetIn;
        offsetY = yOffsetIn;
        offsetZ = zOffsetIn;
        return this;
    }

    @Nonnull
    public CampfireSlotInfo setItemRotation(float itemRotationIn) {
        itemRotation = itemRotationIn;
        return this;
    }

    @Nonnull
    public CampfireSlotInfo setActive(boolean isActiveIn) {
        isActive = isActiveIn;
        return this;
    }

    public void cookTick() {
        if(isActive && tile != null && !output.isEmpty()) {
            if(cookTime < maxCookTime) cookTime++;
            else if(!tile.getWorld().isRemote) {
                stack = output.copy();
                output = ItemStack.EMPTY;
                cookTime = 0;
                maxCookTime = -1;
                sendToTracking();
            }
        }
    }

    public boolean isWithin(double x, double y, double z) {
        return getBoundingBox().grow(0.001).contains(new Vec3d(x, y, z));
    }

    @Nonnull
    public AxisAlignedBB getBoundingBox() {
        final AxisAlignedBB aabb = new AxisAlignedBB(0.5, 0, 0.5, 0.5, 1, 0.5).offset(offsetX, offsetY, offsetZ);
        return aabb.grow(CampfireConfigHandler.Campfire.hasExtraSlots && offsetX != 0 && offsetZ != 0 ? 0.1875 : 0.25);
    }

    @SideOnly(Side.CLIENT)
    public void spawnCookParticles() {
        if(isActive && tile != null && !output.isEmpty() && tile.getWorld().rand.nextFloat() < 0.2) tile.getBlock().ifPresent(block -> {
            final double x = tile.getPos().getX() + 0.5 + offsetX;
            final double y = tile.getPos().getY() + 0.5 + offsetY;
            final double z = tile.getPos().getZ() + 0.5 + offsetZ;

            final int fireColor = tile.getFallbackColor();
            final int smokeColor = tile.isPowered() ? tile.getSmokeColor() : fireColor;

            for(int i = 0; i < 4; i++) block.campfireType.spawnCookingParticle(block.campfireSettings, new LocationImpl(tile.getWorld(), x, y, z), fireColor, smokeColor, tile.isSignal(), tile.isPowered());
        });
    }

    @SideOnly(Side.CLIENT)
    public void render() {
        if(isActive) {
            // render ItemStack
            if(!stack.isEmpty()) {
                GlStateManager.pushMatrix();
                {
                    GlStateManager.translate(offsetX, offsetY, offsetZ);
                    GlStateManager.rotate(itemRotation, 0, 1, 0);
                    GlStateManager.rotate(90, 1, 0, 0);
                    GlStateManager.scale(0.375, 0.375, 0.375);
                    Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
                }
                GlStateManager.popMatrix();
            }

            // render slot debug info
            if(FMLLaunchHandler.isDeobfuscatedEnvironment()) {
                @Nonnull final RenderManager manager = Minecraft.getMinecraft().getRenderManager();
                if(manager.isDebugBoundingBox()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(0, -0.5, 0);
                    {
                        // render slot index
                        @Nonnull final AxisAlignedBB box = getBoundingBox();
                        EntityRenderer.drawNameplate(manager.getFontRenderer(), String.valueOf(slotIndex), (float)offsetX, (float)(offsetY + box.maxY - box.minY), (float)offsetZ, 0, manager.playerViewY, manager.playerViewX, manager.options.thirdPersonView == 2, false);
                        // render slot bounding box
                        GlStateManager.pushMatrix();
                        {
                            GlStateManager.enableBlend();
                            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                            GlStateManager.glLineWidth(2);
                            GlStateManager.disableTexture2D();
                            GlStateManager.depthMask(false);
                            {
                                GlStateManager.translate(-0.5, 0, -0.5);
                                RenderGlobal.drawSelectionBoundingBox(box, 0.25f, 1, 0, 1);
                                // render cooking progress
                                @Nonnull final AxisAlignedBB progress = new AxisAlignedBB(0.45, 0.5, 0.45, 0.55, 1, 0.55).offset(offsetX, 0, offsetZ);
                                RenderGlobal.drawSelectionBoundingBox(progress.grow(0.001), 0, 0, 0, 1);
                                if(maxCookTime > 0) RenderGlobal.renderFilledBox(progress.setMaxY(progress.minY + (progress.maxY - progress.minY) * cookTime / maxCookTime), 1, 1, 0, 1);
                                else if(maxCookTime == -1) RenderGlobal.renderFilledBox(progress, 0, 1, 0, 1);
                            }
                            GlStateManager.depthMask(true);
                            GlStateManager.enableTexture2D();
                            GlStateManager.disableBlend();
                        }
                        GlStateManager.popMatrix();
                    }
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    public void reset() {
        stack = ItemStack.EMPTY;
        output = ItemStack.EMPTY;
        cookTime = 0;
        maxCookTime = 0;
        experience = 0;
    }

    @Nonnull
    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("stack", stack.serializeNBT());
        nbt.setTag("output", output.serializeNBT());
        nbt.setInteger("maxCookTime", maxCookTime);
        nbt.setInteger("cookTime", cookTime);
        nbt.setFloat("experience", experience);
        return nbt;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound nbt) {
        stack = new ItemStack(nbt.getCompoundTag("stack"));
        output = new ItemStack(nbt.getCompoundTag("output"));
        maxCookTime = Math.max(-1, nbt.getInteger("maxCookTime"));
        cookTime = Math.max(0, nbt.getInteger("cookTime"));
        experience = Math.max(0, nbt.getFloat("experience"));
    }

    // utility function that sends a packet to all players tracking the TileEntity
    public void sendToTracking() {
        if(tile != null && tile.getWorld() instanceof WorldServer) {
            final PlayerChunkMapEntry entry = ((WorldServer)tile.getWorld()).getPlayerChunkMap()
                    .getEntry(tile.getPos().getX() >> 4, tile.getPos().getZ() >> 4);

            if(entry != null) {
                final MessageSyncCampfireSlot message = new MessageSyncCampfireSlot(this);
                entry.getWatchingPlayers().forEach(player -> Campfire.WRAPPER.sendTo(message, player));
            }
        }
    }

    public void resetAndSendToTracking() { reset(); sendToTracking(); }
    public boolean canSelectForAutomation() { return CampfireConfigHandler.Campfire.campfireAutomation && isActive && output.isEmpty(); }
}
