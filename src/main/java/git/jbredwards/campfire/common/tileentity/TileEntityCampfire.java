package git.jbredwards.campfire.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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
        slotInfo.add(new CampfireSlotInfo().setOffset(-0.3125, -0.05078125, -0.3125).setItemRotation(0));
        slotInfo.add(new CampfireSlotInfo().setOffset(-0.3125, -0.05078125, 0.3125).setItemRotation(90));
        slotInfo.add(new CampfireSlotInfo().setOffset(0.3125, -0.05078125, 0.3125).setItemRotation(180));
        slotInfo.add(new CampfireSlotInfo().setOffset(0.3125, -0.05078125, -0.3125).setItemRotation(270));
        //debugging
        slotInfo.forEach(slot -> slot.stack = new ItemStack(Items.PORKCHOP));
    }

    @Override
    public void update() {

    }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
