package git.jbredwards.campfire.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public class TileEntityCampfire extends TileEntity implements ITickable
{
    @Nonnull
    public final List<CampfireSlotInfo> slotInfo = new ArrayList<>();
    public TileEntityCampfire() {
        slotInfo.add(new CampfireSlotInfo().setOffset());
        slotInfo.add(new CampfireSlotInfo());
        slotInfo.add(new CampfireSlotInfo());
        slotInfo.add(new CampfireSlotInfo());
    }

    @Override
    public void update() {

    }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
