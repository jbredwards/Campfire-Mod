package git.jbredwards.campfire.common.tileentity;

import git.jbredwards.campfire.common.block.AbstractCampfire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public abstract class AbstractCampfireTE extends TileEntity implements ITickable
{
    public boolean isLit() { return (getBlockMetadata() & 2) != 0; }
    public boolean isSignal() { return (getBlockMetadata() & 4) != 0; }
    public boolean isPowered() { return (getBlockMetadata() & 1) != 0; }

    @Nonnull
    public Optional<AbstractCampfire<?>> getBlock() {
        return getBlockType() instanceof AbstractCampfire ? Optional.of((AbstractCampfire<?>)getBlockType()) : Optional.empty();
    }

    @SideOnly(Side.CLIENT)
    public void addParticles() {
        final Optional<AbstractCampfire<?>> block = getBlock();
        if(block.isPresent() && world.rand.nextFloat() < 0.11)
            for(int i = 0; i < world.rand.nextInt(2) + 2; i++)
                block.get().addParticles(world, pos, isSignal(), isPowered());
    }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
