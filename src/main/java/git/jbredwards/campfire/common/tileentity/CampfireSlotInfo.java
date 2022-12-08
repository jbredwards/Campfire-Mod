package git.jbredwards.campfire.common.tileentity;

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
    public int maxCookTime = 400, cookTime;
    public float experience;
    public boolean isActive = true;
    //relative to the TileEntity's center position
    public double offsetX, offsetY, offsetZ;

    @Nonnull
    public CampfireSlotInfo setOffset(double xOffsetIn, double yOffsetIn, double zOffsetIn) {
        offsetX = xOffsetIn;
        offsetY = yOffsetIn;
        offsetZ = zOffsetIn;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public void render() {
        if(isActive) {

        }
    }
}
