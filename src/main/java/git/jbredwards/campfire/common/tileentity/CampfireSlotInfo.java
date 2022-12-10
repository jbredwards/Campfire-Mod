package git.jbredwards.campfire.common.tileentity;

import git.jbredwards.campfire.Campfire;
import git.jbredwards.campfire.common.message.MessageSyncCampfireSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class CampfireSlotInfo implements INBTSerializable<NBTTagCompound>
{
    @Nonnull
    public final TileEntityCampfire tile;
    public final int slotIndex; //needed to sync with client

    public ItemStack stack = ItemStack.EMPTY, output = ItemStack.EMPTY;
    public int maxCookTime, cookTime;
    public float experience;
    public boolean isActive = true;
    public float itemRotation; //client-side item rotation
    public double offsetX, offsetY, offsetZ; //relative to the TileEntity's center position

    public CampfireSlotInfo(@Nonnull TileEntityCampfire tileIn, int slotIndexIn) {
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
        if(isActive && !output.isEmpty()) {
            if(cookTime < maxCookTime) cookTime++;
            else {
                stack = output.copy();
                output = ItemStack.EMPTY;
                cookTime = 0;
                maxCookTime = 0;
                sendToTracking();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void spawnCookParticles() {

    }

    @SideOnly(Side.CLIENT)
    public void render() {
        if(isActive && !stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(offsetX, offsetY, offsetZ);
            GlStateManager.rotate(itemRotation, 0, 1, 0);
            GlStateManager.rotate(90, 1, 0, 0);
            GlStateManager.scale(0.375, 0.375, 0.375);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
    }

    public void reset() {
        stack = ItemStack.EMPTY;
        output = ItemStack.EMPTY;
        cookTime = 0;
        maxCookTime = 0;
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
        maxCookTime = Math.max(0, nbt.getInteger("maxCookTime"));
        cookTime = Math.max(0, nbt.getInteger("cookTime"));
        experience = Math.max(0, nbt.getFloat("experience"));
    }

    //utility function that sends a packet to all players tracking the TileEntity
    public void sendToTracking() {
        if(tile.getWorld() instanceof WorldServer) {
            final PlayerChunkMapEntry entry = ((WorldServer)tile.getWorld()).getPlayerChunkMap()
                    .getEntry(tile.getPos().getX() >> 4, tile.getPos().getZ() >> 4);

            if(entry != null) {
                final MessageSyncCampfireSlot message = new MessageSyncCampfireSlot(this);
                entry.getWatchingPlayers().forEach(player -> Campfire.wrapper.sendTo(message, player));
            }
        }
    }
}