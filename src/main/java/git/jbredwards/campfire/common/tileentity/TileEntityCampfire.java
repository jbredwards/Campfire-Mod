package git.jbredwards.campfire.common.tileentity;

import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.tileentity.slot.CampfireSlotInfo;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
public class TileEntityCampfire extends AbstractCampfireTE
{
    @Nonnull
    public final List<CampfireSlotInfo> slotInfo = new ArrayList<>();
    public TileEntityCampfire() { initSlots(); }

    @Override
    public void resetFireStrength() { fireStrength = CampfireConfigHandler.campfireBurnOut; }
    protected void initSlots() {
        slotInfo.add(new CampfireSlotInfo(this, 0).setOffset(-0.3125, -0.05078125, -0.3125).setItemRotation(0));
        slotInfo.add(new CampfireSlotInfo(this, 1).setOffset(-0.3125, -0.05078125, 0.3125).setItemRotation(90));
        slotInfo.add(new CampfireSlotInfo(this, 2).setOffset(0.3125, -0.05078125, 0.3125).setItemRotation(180));
        slotInfo.add(new CampfireSlotInfo(this, 3).setOffset(0.3125, -0.05078125, -0.3125).setItemRotation(270));
        //extra slots can be disabled via the config
        slotInfo.add(new CampfireSlotInfo(this, 4).setOffset(-0.3125, -0.05078125, 0).setItemRotation(45).setActive(CampfireConfigHandler.hasExtraSlots));
        slotInfo.add(new CampfireSlotInfo(this, 5).setOffset(0, -0.05078125, 0.3125).setItemRotation(135).setActive(CampfireConfigHandler.hasExtraSlots));
        slotInfo.add(new CampfireSlotInfo(this, 6).setOffset(0.3125, -0.05078125, 0).setItemRotation(225).setActive(CampfireConfigHandler.hasExtraSlots));
        slotInfo.add(new CampfireSlotInfo(this, 7).setOffset(0, -0.05078125, -0.3125).setItemRotation(315).setActive(CampfireConfigHandler.hasExtraSlots));
    }

    @Override
    public void update() {
        updateConditionalSlotIsActive();
        if(hasWorld()) {
            //update certain config slot y offset values
            final boolean xAxis = (getBlockMetadata() & 8) != 0;
            slotInfo.forEach(slot -> {
                if(slot.offsetX == 0) { if(xAxis) slot.offsetY = -0.23828125; }
                else if(slot.offsetZ == 0) { if(!xAxis) slot.offsetY = -0.23828125; }
            });

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

    protected void updateConditionalSlotIsActive() {
        slotInfo.get(4).setActive(CampfireConfigHandler.hasExtraSlots);
        slotInfo.get(5).setActive(CampfireConfigHandler.hasExtraSlots);
        slotInfo.get(6).setActive(CampfireConfigHandler.hasExtraSlots);
        slotInfo.get(7).setActive(CampfireConfigHandler.hasExtraSlots);
    }

    public void dropAllItems() {
        for(CampfireSlotInfo slot : slotInfo) {
            if(!slot.stack.isEmpty()) {
                final double x = pos.getX() + 0.5 + slot.offsetX;
                final double y = pos.getY() + 0.5 + slot.offsetY;
                final double z = pos.getZ() + 0.5 + slot.offsetZ;

                InventoryHelper.spawnItemStack(world, x, y, z, slot.stack);
                slot.reset();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addParticles() {
        super.addParticles();
        slotInfo.forEach(CampfireSlotInfo::spawnCookParticles);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        //write slots
        final NBTTagList slots = new NBTTagList();
        slotInfo.forEach(slot -> slots.appendTag(slot.serializeNBT()));
        compound.setTag("Slots", slots);

        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        //read slots
        final NBTTagList slots = compound.getTagList("Slots", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < slots.tagCount() && i < slotInfo.size(); i++)
            slotInfo.get(i).deserializeNBT(slots.getCompoundTagAt(i));
    }
}
