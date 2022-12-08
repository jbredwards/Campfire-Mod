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
import net.minecraft.util.text.translation.I18n;
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
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        final ICampfireType cap = ICampfireType.get(stack);
        if(cap != null) {
            final ItemStack type = cap.get();
            //if a defined special case exists in .lang file, use that instead of auto generating a name
            final String specialCase = String.format("%s.type.%s.name", stack.getTranslationKey(), type.getTranslationKey());
            if(I18n.canTranslate(specialCase)) return I18n.translateToLocal(specialCase);
            //auto generate a name
            return type.getDisplayName().replaceFirst(
                    I18n.translateToLocal("regex.campfire.target"),
                    I18n.translateToLocal("regex.campfire.replacement"));
        }

        //should never pass
        return super.getItemStackDisplayName(stack);
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
