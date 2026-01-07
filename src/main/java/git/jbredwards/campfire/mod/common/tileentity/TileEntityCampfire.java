/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.tileentity;

import git.jbredwards.campfire.api.CampfireAPI;
import git.jbredwards.campfire.api.block.campfire.ICampfireType;
import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.api.recipe.campfire.CampfireRecipe;
import git.jbredwards.campfire.mod.common.capability.ICampfireWoodType;
import git.jbredwards.campfire.mod.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.mod.common.tileentity.slot.CampfireSlotInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
public class TileEntityCampfire extends AbstractCampfireTE implements IInventory, ILootContainer
{
    @Nullable
    public IForgeRegistry<?> woodTypes;

    @Nullable
    protected ResourceLocation lootTable;
    public long lootTableSeed;

    @Nonnull
    public final List<CampfireSlotInfo> slotInfo = new ArrayList<>();
    protected boolean checkConfig = true;

    public TileEntityCampfire() { initSlots(); }
    public TileEntityCampfire(@Nonnull final ICampfireType<?> campfireType) {
        woodTypes = campfireType.getWoodTypes();
        initSlots();
    }

    protected void initSlots() {
        slotInfo.add(new CampfireSlotInfo(this, 0).setOffset(-0.3125, -0.05078125, -0.3125).setItemRotation(0));
        slotInfo.add(new CampfireSlotInfo(this, 1).setOffset(-0.3125, -0.05078125, 0.3125).setItemRotation(90));
        slotInfo.add(new CampfireSlotInfo(this, 2).setOffset(0.3125, -0.05078125, 0.3125).setItemRotation(180));
        slotInfo.add(new CampfireSlotInfo(this, 3).setOffset(0.3125, -0.05078125, -0.3125).setItemRotation(270));
        //extra slots can be disabled via the config
        slotInfo.add(new CampfireSlotInfo(this, 4).setOffset(-0.3125, -0.05078125, 0).setItemRotation(45).setActive(CampfireConfigHandler.Campfire.hasExtraSlots));
        slotInfo.add(new CampfireSlotInfo(this, 5).setOffset(0, -0.05078125, 0.3125).setItemRotation(135).setActive(CampfireConfigHandler.Campfire.hasExtraSlots));
        slotInfo.add(new CampfireSlotInfo(this, 6).setOffset(0.3125, -0.05078125, 0).setItemRotation(225).setActive(CampfireConfigHandler.Campfire.hasExtraSlots));
        slotInfo.add(new CampfireSlotInfo(this, 7).setOffset(0, -0.05078125, -0.3125).setItemRotation(315).setActive(CampfireConfigHandler.Campfire.hasExtraSlots));
    }

    @Override
    public void update() {
        updateConditionalSlotIsActive();
        if(hasWorld()) {
            if(lootTable != null) setLootTable(lootTable, lootTableSeed);

            //update certain config slot y offset values
            final boolean xAxis = (getBlockMetadata() & 8) != 0;
            slotInfo.forEach(slot -> {
                if(slot.offsetX == 0) { if(xAxis) slot.offsetY = -0.23828125; }
                else if(slot.offsetZ == 0) { if(!xAxis) slot.offsetY = -0.23828125; }
            });

            if(isLit()) {
                if(world.isRemote) addParticles();
                slotInfo.forEach(CampfireSlotInfo::cookTick);
            }

            else slotInfo.forEach(slot -> slot.cookTime = 0);
        }
    }

    protected void updateConditionalSlotIsActive() {
        slotInfo.get(4).setActive(CampfireConfigHandler.Campfire.hasExtraSlots);
        slotInfo.get(5).setActive(CampfireConfigHandler.Campfire.hasExtraSlots);
        slotInfo.get(6).setActive(CampfireConfigHandler.Campfire.hasExtraSlots);
        slotInfo.get(7).setActive(CampfireConfigHandler.Campfire.hasExtraSlots);
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
        // write wood type registry data
        if(woodTypes != null) compound.setString("WoodRegistry", String.valueOf(RegistryManager.ACTIVE.getName((IForgeRegistry)woodTypes)));
        super.writeToNBT(compound);

        //write slots
        final NBTTagList slots = new NBTTagList();
        slotInfo.forEach(slot -> slots.appendTag(slot.serializeNBT()));
        compound.setTag("Slots", slots);

        //write loot table
        if(lootTable != null) {
            compound.setLong("LootTableSeed", lootTableSeed);
            compound.setString("LootTable", lootTable.toString());
        }

        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        // read wood registry
        if(compound.hasKey("WoodRegistry", Constants.NBT.TAG_STRING))
            woodTypes = RegistryManager.ACTIVE.getRegistry(new ResourceLocation(compound.getString("WoodRegistry")));
        if(woodTypes == null) woodTypes = CampfireAPI.WOOD_TYPES; // for old campfires

        // read pos and cap data
        super.readFromNBT(compound);

        //read slots
        final NBTTagList slots = compound.getTagList("Slots", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < slots.tagCount() && i < slotInfo.size(); i++)
            slotInfo.get(i).deserializeNBT(slots.getCompoundTagAt(i));

        //read loot table
        if(compound.hasKey("LootTable", Constants.NBT.TAG_STRING))
            setLootTable(new ResourceLocation(compound.getString("LootTable")), compound.getLong("LootTableSeed"));
    }

    @Override
    protected void setWorldCreate(@Nonnull final World worldIn) { setWorld(worldIn); }
    public void setLootTable(@Nullable final ResourceLocation lootTableIn, final long lootTableSeedIn) {
        lootTable = lootTableIn;
        lootTableSeed = lootTableSeedIn;

        if(lootTableIn != null && hasWorld()) {
            if(world instanceof WorldServer) {
                @Nonnull final LootTable table = world.getLootTableManager().getLootTableFromLocation(lootTableIn);
                @Nonnull final IInventory dummyInventory = new InventoryBasic("", true, CampfireConfigHandler.Campfire.hasExtraSlots ? 8 : 4);

                for(int slot = 0; slot < dummyInventory.getSizeInventory(); slot++) dummyInventory.setInventorySlotContents(slot, slotInfo.get(slot).stack);
                table.fillInventory(dummyInventory, lootTableSeedIn == 0 ? new Random() : new Random(lootTableSeedIn), new LootContext.Builder((WorldServer)world).build());
                for(int slot = 0; slot < dummyInventory.getSizeInventory(); slot++) if(slotInfo.get(slot).stack.isEmpty()) {
                    slotInfo.get(slot).reset();
                    slotInfo.get(slot).stack = dummyInventory.getStackInSlot(slot);
                    slotInfo.get(slot).sendToTracking();
                }
            }

            lootTable = null;
            lootTableSeed = 0;
        }
    }

    @Nullable
    @Override
    public ResourceLocation getLootTable() { return lootTable; }

    // =========
    // INVENTORY
    // =========

    @Override
    public int getInventoryStackLimit() { return 1; }

    @Override
    public int getSizeInventory() { return CampfireConfigHandler.Campfire.campfireAutomation ? slotInfo.size() : 0; }

    @Override
    public boolean isEmpty() {
        return slotInfo.stream().allMatch(slot -> checkConfig && !slot.canSelectForAutomation() || slot.stack.isEmpty());
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(final int index) {
        return !checkConfig || slotInfo.get(index).canSelectForAutomation() ? slotInfo.get(index).stack : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(final int index, final int count) {
        if(count <= 0 || checkConfig && !slotInfo.get(index).canSelectForAutomation() || slotInfo.get(index).stack.isEmpty()) return ItemStack.EMPTY;
        @Nonnull final ItemStack extracted = slotInfo.get(index).stack.splitStack(count);

        if(slotInfo.get(index).stack.isEmpty()) slotInfo.get(index).resetAndSendToTracking();
        else slotInfo.get(index).sendToTracking();

        return extracted;
    }

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(final int index) {
        if(!slotInfo.get(index).canSelectForAutomation() || slotInfo.get(index).stack.isEmpty()) return ItemStack.EMPTY;
        @Nonnull final ItemStack removed = slotInfo.get(index).stack;

        slotInfo.get(index).resetAndSendToTracking();
        return removed;
    }

    @Override
    public void setInventorySlotContents(final int index, @Nonnull final ItemStack stack) {
        if(checkConfig && !slotInfo.get(index).canSelectForAutomation() || !slotInfo.get(index).stack.isEmpty()) return;
        boolean foundRecipe = false;

        @Nullable final ICampfireWoodType woodType = ICampfireWoodType.get(this);
        if(woodType != null && getBlock().isPresent()) {
            @Nonnull final Optional<? extends CampfireRecipe<?>> recipe = getBlock().get().campfireType.getRecipe(stack, woodType);
            if(recipe.isPresent()) {
                slotInfo.get(index).output = recipe.get().output.copy();
                slotInfo.get(index).maxCookTime = recipe.get().cookTime;
                slotInfo.get(index).experience = recipe.get().experience;

                foundRecipe = true;
            }
        }

        if(!foundRecipe) {
            if(CampfireConfigHandler.Campfire.recipeItemsOnCampfireOnly) return;
            slotInfo.get(index).reset();
        }

        slotInfo.get(index).stack = stack.splitStack(getInventoryStackLimit());
        slotInfo.get(index).sendToTracking();
    }

    @Override
    public boolean isItemValidForSlot(final int index, @Nonnull final ItemStack stack) {
        if(checkConfig && !slotInfo.get(index).canSelectForAutomation() || !slotInfo.get(index).stack.isEmpty()) return false;
        else if(!CampfireConfigHandler.Campfire.recipeItemsOnCampfireOnly) return true;

        @Nullable final ICampfireWoodType woodType = ICampfireWoodType.get(this);
        return woodType != null && getBlock().isPresent() && getBlock().get().campfireType.getRecipe(stack, woodType).isPresent();
    }

    @Override
    public void clear() {
        slotInfo.stream().filter(CampfireSlotInfo::canSelectForAutomation).forEach(CampfireSlotInfo::resetAndSendToTracking);
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return CampfireConfigHandler.Campfire.campfireAutomation && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        return CampfireConfigHandler.Campfire.campfireAutomation && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new InvWrapper(this)) : super.getCapability(capability, facing);
    }

    // ======
    // UNUSED
    // ======

    @Override
    public boolean isUsableByPlayer(@Nonnull final EntityPlayer player) { return false; }

    @Override
    public void openInventory(@Nonnull final EntityPlayer player) {}

    @Override
    public void closeInventory(@Nonnull final EntityPlayer player) {}

    @Override
    public int getField(final int id) { return 0; }

    @Override
    public void setField(final int id, final int value) {}

    @Override
    public int getFieldCount() { return 0; }

    @Override
    public boolean hasCustomName() { return false; }

    @Nonnull
    @Override
    public String getName() { return getBlockType().getTranslationKey() + ".name"; }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() { return new TextComponentTranslation(getName()); }
}
