package git.jbredwards.campfire.common.init;

import git.jbredwards.campfire.common.item.ItemCampfire;
import net.minecraft.item.ItemBlock;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class CampfireItems
{
    @Nonnull
    public static final ItemBlock BRAZIER = new ItemBlock(CampfireBlocks.BRAZIER);

    @Nonnull
    public static final ItemCampfire CAMPFIRE = new ItemCampfire(CampfireBlocks.CAMPFIRE);
}
