package git.jbredwards.campfire.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public class ItemBlockColored extends ItemBlock
{
    public ItemBlockColored(@Nonnull Block block) { super(block); }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState) {
        final boolean placed = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if(placed) { //ensure the block is re-rendered if broken & placed in quick succession
            world.markBlockRangeForRenderUpdate(pos, pos);
            return true;
        }

        return false;
    }

    //use nbt for color as to be compatible with certain existing vanilla & modded systems
    public static int getColor(@Nonnull ItemStack campfire) {
        final @Nullable NBTTagCompound display = campfire.getSubCompound("display");
        return display != null && display.hasKey("color", Constants.NBT.TAG_INT) ? display.getInteger("color") : -1;
    }

    //use nbt for color as to be compatible with certain existing vanilla & modded systems
    @Nonnull
    public static ItemStack applyColor(@Nonnull ItemStack campfire, int colorIn) {
        if(colorIn != -1 || campfire.getSubCompound("display") != null) {
            final NBTTagCompound display = campfire.getOrCreateSubCompound("display");
            if(display.hasKey("color", Constants.NBT.TAG_INT)) {
                if(colorIn == -1) display.removeTag("color");
                else display.setInteger("color", colorIn);
            }

            else if(colorIn != -1) display.setInteger("color", colorIn);
        }

        return campfire;
    }
}
