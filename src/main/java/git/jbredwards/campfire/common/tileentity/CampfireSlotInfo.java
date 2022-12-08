package git.jbredwards.campfire.common.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class CampfireSlotInfo
{
    public ItemStack stack = ItemStack.EMPTY, output = ItemStack.EMPTY;
    public int maxCookTime, cookTime;
    public float experience;
    public boolean isActive = true;
    //client-side item rotation
    public float itemRotation;
    //relative to the TileEntity's center position
    public double offsetX, offsetY, offsetZ;

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
}
