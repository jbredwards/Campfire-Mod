package git.jbredwards.campfire.common.compat.futuremc;

import git.jbredwards.campfire.common.block.AbstractCampfire;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;

/**
 * blocks that implement this can calm beehives from Future MC
 * @author jbred
 *
 */
public interface IBeeCalmer
{
    default boolean canCalmBeeHive(@Nonnull IBlockState campfire) { return campfire.getValue(AbstractCampfire.LIT); }
}
