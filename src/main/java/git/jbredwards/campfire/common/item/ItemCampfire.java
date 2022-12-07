package git.jbredwards.campfire.common.item;

import git.jbredwards.campfire.common.capability.ICampfireType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class ItemCampfire extends ItemBlock
{
    public ItemCampfire(@Nonnull Block block) { super(block); }

    @Nonnull
    @Override
    public String getTranslationKey(@Nonnull ItemStack stack) {
        final ICampfireType type = ICampfireType.get(stack);
        if(type != null) {
            final String typeTranslation = type.get().getTranslationKey();

        }

        return super.getTranslationKey(stack);
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState) {
        final boolean placed = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if(placed) { //ensure the block is re-rendered if broken & placed in quick succession
            world.markBlockRangeForRenderUpdate(pos, pos);
            return true;
        }

        return false;
    }

    @Nonnull
    public static ItemStack applyType(@Nonnull Block campfire, @Nonnull ItemStack type) {
        return applyType(new ItemStack(campfire), type);
    }

    @Nonnull
    public static ItemStack applyType(@Nonnull Item campfire, @Nonnull ItemStack type) {
        return applyType(new ItemStack(campfire), type);
    }

    @Nonnull
    public static ItemStack applyType(@Nonnull ItemStack campfire, @Nonnull ItemStack type) {
        final ICampfireType cap = ICampfireType.get(campfire);
        if(cap != null) cap.set(type);
        return campfire;
    }
}
