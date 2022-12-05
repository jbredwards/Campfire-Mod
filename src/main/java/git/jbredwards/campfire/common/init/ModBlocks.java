package git.jbredwards.campfire.common.init;

import git.jbredwards.campfire.common.block.BlockCampfire;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class ModBlocks
{
    @Nonnull
    public static final BlockCampfire CAMPFIRE = new BlockCampfire(Material.WOOD, MapColor.OBSIDIAN);
}
