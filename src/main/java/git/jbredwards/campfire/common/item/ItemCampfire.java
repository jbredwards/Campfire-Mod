package git.jbredwards.campfire.common.item;

import git.jbredwards.campfire.common.capability.ICampfireType;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

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
    public static ItemStack applyType(@Nonnull ItemStack campfire, @Nonnull ItemStack type) {
        final ICampfireType cap = ICampfireType.get(campfire);
        if(cap != null) cap.set(type);
        return campfire;
    }
}
