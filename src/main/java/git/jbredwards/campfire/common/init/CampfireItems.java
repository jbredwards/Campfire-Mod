package git.jbredwards.campfire.common.init;

import git.jbredwards.campfire.common.item.ItemBlockColored;
import git.jbredwards.campfire.common.item.ItemCampfire;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class CampfireItems
{
    @Nonnull
    public static final ItemBlockColored BRAZIER = new ItemBlockColored(CampfireBlocks.BRAZIER);

    @Nonnull
    public static final ItemCampfire CAMPFIRE = new ItemCampfire(CampfireBlocks.CAMPFIRE);
}
