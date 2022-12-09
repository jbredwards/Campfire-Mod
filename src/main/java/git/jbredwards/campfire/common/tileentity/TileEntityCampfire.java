package git.jbredwards.campfire.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

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
    }

    @Override
    public void update() {

    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        compound = super.writeToNBT(compound);

        //write slots
        final NBTTagList slots = new NBTTagList();
        slotInfo.forEach(slot -> slots.appendTag(slot.serializeNBT()));
        compound.setTag("slots", slots);

        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);

        //read slots
        final NBTTagList slots = compound.getTagList("slots", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < slots.tagCount(); i++) slotInfo.get(i).deserializeNBT(slots.getCompoundTagAt(i));
    }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
