package git.jbredwards.campfire.common.init;

import git.jbredwards.campfire.common.block.BlockBrazier;
import git.jbredwards.campfire.common.block.BlockCampfire;
import git.jbredwards.campfire.common.block.BlockCampfireAsh;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class CampfireBlocks
{
    @Nonnull
    public static final BlockBrazier BRAZIER = new BlockBrazier(Material.IRON, MapColor.OBSIDIAN, true);

    @Nonnull
    public static final BlockCampfire CAMPFIRE = new BlockCampfire(Material.WOOD, MapColor.OBSIDIAN, true);

    @Nonnull
    public static final BlockCampfireAsh CAMPFIRE_ASH = new BlockCampfireAsh(Material.CIRCUITS);
}
