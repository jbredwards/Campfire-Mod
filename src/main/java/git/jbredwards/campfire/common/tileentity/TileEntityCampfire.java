package git.jbredwards.campfire.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class TileEntityCampfire extends TileEntityLockableLoot implements ITickable
{
    @Nonnull
    protected final NonNullList<ItemStack> contents = NonNullList.withSize(4, ItemStack.EMPTY);
    protected final int[] cookData = new int[8];

    @Override
    public void update() {

    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(checkLootAndRead(compound)) fillWithLoot(null);
        else ItemStackHelper.loadAllItems(compound, contents);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        if(!checkLootAndWrite(compound))
            ItemStackHelper.saveAllItems(compound, contents);

        return compound;
    }

    @Nonnull
    @Override
    protected NonNullList<ItemStack> getItems() { return contents; }

    @Override
    public int getSizeInventory() { return 4; }

    @Override
    public boolean isEmpty() {
        for(ItemStack stack : contents)
            if(!stack.isEmpty())
                return false;

        return true;
    }

    @Override
    public int getInventoryStackLimit() { return 1; }

    @Nonnull
    @Override
    public Container createContainer(@Nonnull InventoryPlayer playerInventory, @Nonnull EntityPlayer playerIn) {
        return null;
    }

    @Nonnull
    @Override
    public String getGuiID() { return "campfire:campfire"; }

    @Nonnull
    @Override
    public String getName() { return "container.campfire"; }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
