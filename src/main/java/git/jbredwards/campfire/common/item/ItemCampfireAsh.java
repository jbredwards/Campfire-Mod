package git.jbredwards.campfire.common.item;

import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class ItemCampfireAsh extends ItemBlock
{
    public ItemCampfireAsh(@Nonnull Block block) { super(block); }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if(CampfireConfigHandler.campfireBurnOut > 0) super.getSubItems(tab, items);
    }
}
