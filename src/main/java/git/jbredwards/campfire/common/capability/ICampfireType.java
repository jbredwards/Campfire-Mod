package git.jbredwards.campfire.common.capability;

import git.jbredwards.campfire.common.item.ItemCampfire;
import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("ConstantConditions")
@Mod.EventBusSubscriber(modid = "campfire")
public interface ICampfireType
{
    @CapabilityInject(ICampfireType.class)
    @Nonnull Capability<ICampfireType> CAPABILITY = null;
    @Nonnull ResourceLocation CAPABILITY_ID = new ResourceLocation("campfire", "type");

    //do not mutate!
    @Nonnull ItemStack get();
    void set(@Nonnull ItemStack logIn);

    @Nullable
    static ICampfireType get(@Nullable ICapabilityProvider provider) {
        return provider != null && provider.hasCapability(CAPABILITY, null) ? provider.getCapability(CAPABILITY, null) : null;
    }

    @SubscribeEvent
    static void attachToItem(@Nonnull AttachCapabilitiesEvent<ItemStack> event) {
        if(event.getObject().getItem() instanceof ItemCampfire)
            event.addCapability(CAPABILITY_ID, new Provider());
    }

    @SubscribeEvent
    static void attachToTile(@Nonnull AttachCapabilitiesEvent<TileEntity> event) {
        if(event.getObject() instanceof TileEntityCampfire)
            event.addCapability(CAPABILITY_ID, new Provider());
    }

    class Impl implements ICampfireType
    {
        @Nonnull
        ItemStack log = new ItemStack(Blocks.LOG);

        @Nonnull
        @Override
        public ItemStack get() { return log; }

        @Override
        public void set(@Nonnull ItemStack logIn) { log = logIn; }
    }

    final class Provider implements ICapabilitySerializable<NBTBase>
    {
        @Nonnull
        final ICampfireType instance = CAPABILITY.getDefaultInstance();

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CAPABILITY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return hasCapability(capability, facing) ? CAPABILITY.cast(instance) : null;
        }

        @Nonnull
        @Override
        public NBTBase serializeNBT() { return CAPABILITY.writeNBT(instance, null); }

        @Override
        public void deserializeNBT(@Nonnull NBTBase nbt) { CAPABILITY.readNBT(instance, null, nbt); }
    }

    enum Storage implements Capability.IStorage<ICampfireType>
    {
        INSTANCE;

        @Nullable
        @Override
        public NBTBase writeNBT(@Nonnull Capability<ICampfireType> capability, @Nonnull ICampfireType instance, @Nullable EnumFacing side) {
            return instance.get().serializeNBT();
        }

        @Override
        public void readNBT(@Nonnull Capability<ICampfireType> capability, @Nonnull ICampfireType instance, @Nullable EnumFacing side, @Nullable NBTBase nbt) {
            if(nbt instanceof NBTTagCompound) instance.set(new ItemStack((NBTTagCompound)nbt));
        }
    }
}
