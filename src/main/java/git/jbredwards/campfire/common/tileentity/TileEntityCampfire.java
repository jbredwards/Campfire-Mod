package git.jbredwards.campfire.common.tileentity;

import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    public TileEntityCampfire() { initSlots(); }

    protected void initSlots() {
        slotInfo.add(new CampfireSlotInfo(this, 0).setOffset(-0.3125, -0.05078125, -0.3125).setItemRotation(0));
        slotInfo.add(new CampfireSlotInfo(this, 1).setOffset(-0.3125, -0.05078125, 0.3125).setItemRotation(90));
        slotInfo.add(new CampfireSlotInfo(this, 2).setOffset(0.3125, -0.05078125, 0.3125).setItemRotation(180));
        slotInfo.add(new CampfireSlotInfo(this, 3).setOffset(0.3125, -0.05078125, -0.3125).setItemRotation(270));
        //extra slots can be disabled via the config
        slotInfo.add(new CampfireSlotInfo(this, 4).setOffset(-0.3125, -0.05078125, 0).setItemRotation(45).setActive(CampfireConfigHandler.hasExtraSlots));
        slotInfo.add(new CampfireSlotInfo(this, 5).setOffset(0.3125, -0.05078125, 0).setItemRotation(135).setActive(CampfireConfigHandler.hasExtraSlots));
        slotInfo.add(new CampfireSlotInfo(this, 6).setOffset(0, -0.05078125, 0.3125).setItemRotation(225).setActive(CampfireConfigHandler.hasExtraSlots));
        slotInfo.add(new CampfireSlotInfo(this, 7).setOffset(0, -0.05078125, -0.3125).setItemRotation(315).setActive(CampfireConfigHandler.hasExtraSlots));
    }

    @Override
    public void update() {
        updateConditionalSlotIsActive();
        if(hasWorld()) {
            //particles
            if(world.isRemote) {
                if(isLit()) addParticles();
            }
            //tick slots
            else {
                if(isLit()) slotInfo.forEach(CampfireSlotInfo::cookTick);
                else slotInfo.forEach(slot -> slot.cookTime = 0);
            }
        }
    }

    public boolean isLit() { return (getBlockMetadata() & 2) != 0; }
    protected void updateConditionalSlotIsActive() {
        slotInfo.get(4).setActive(CampfireConfigHandler.hasExtraSlots);
        slotInfo.get(5).setActive(CampfireConfigHandler.hasExtraSlots);
        slotInfo.get(6).setActive(CampfireConfigHandler.hasExtraSlots);
        slotInfo.get(7).setActive(CampfireConfigHandler.hasExtraSlots);
    }

    public void dropAllItems() {
        for(CampfireSlotInfo slot : slotInfo) {
            if(!slot.stack.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), slot.stack);
                slot.reset();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void addParticles() {

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
        for(int i = 0; i < slots.tagCount() && i < slotInfo.size(); i++)
            slotInfo.get(i).deserializeNBT(slots.getCompoundTagAt(i));
    }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
