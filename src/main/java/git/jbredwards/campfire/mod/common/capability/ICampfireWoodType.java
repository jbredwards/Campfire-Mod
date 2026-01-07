/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.capability;

import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.api.capability.CapabilityProvider;
import git.jbredwards.campfire.mod.common.item.ItemCampfire;
import git.jbredwards.campfire.mod.common.tileentity.TileEntityCampfire;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("ConstantConditions")
@Mod.EventBusSubscriber(modid = "campfire")
public interface ICampfireWoodType extends INBTSerializable<NBTBase>
{
    @CapabilityInject(ICampfireWoodType.class)
    @Nonnull Capability<ICampfireWoodType> CAPABILITY = null;
    @Nonnull ResourceLocation CAPABILITY_ID = new ResourceLocation("campfire", "type");

    @Nullable
    CampfireWoodType<?> get();
    void set(@Nullable final CampfireWoodType<?> logIn);

    @Nonnull
    @Override
    default NBTBase serializeNBT() {
        return getOptional().<NBTBase>map(CampfireWoodType::serializeNBT).orElseGet(() -> new NBTTagByte((byte)0));
    }

    @Nonnull
    default Optional<CampfireWoodType<?>> getOptional() { return Optional.ofNullable(get()); }

    // ------------------------
    // get and set capabilities
    // ------------------------

    @Nullable
    static ICampfireWoodType get(@Nullable ICapabilityProvider provider) {
        return provider != null && provider.hasCapability(CAPABILITY, null) ? provider.getCapability(CAPABILITY, null) : null;
    }

    @SubscribeEvent
    static void attachToItem(@Nonnull AttachCapabilitiesEvent<ItemStack> event) {
        if(event.getObject().getItem() instanceof ItemCampfire) event.addCapability(CAPABILITY_ID,
            new CapabilityProvider<>(CAPABILITY, new StackImpl(event.getObject(), (ItemCampfire<?>)event.getObject().getItem())));
    }

    @SubscribeEvent
    static void attachToTile(@Nonnull AttachCapabilitiesEvent<TileEntity> event) {
        if(event.getObject() instanceof TileEntityCampfire) event.addCapability(CAPABILITY_ID,
            new CapabilityProvider<>(CAPABILITY, new TileImpl((TileEntityCampfire)event.getObject())));
    }

    // ----------------------------
    // default capability instances
    // ----------------------------

    class StackImpl implements ICampfireWoodType
    {
        @Nonnull final IForgeRegistry<? extends CampfireWoodType<?>> woodTypes;
        @Nonnull final ItemStack owner;

        StackImpl(@Nonnull final ItemStack ownerIn, @Nonnull final ItemCampfire<?> campfire) {
            woodTypes = campfire.campfireType.getWoodTypes();
            owner = ownerIn;
        }

        @Nullable
        @Override
        public CampfireWoodType<?> get() {
            return CampfireWoodType.deserializeNBT(woodTypes, owner.getOrCreateSubCompound(CAPABILITY_ID.toString())).orElse(null);
        }

        @Override
        public void set(@Nullable final CampfireWoodType<?> logIn) { owner.setTagInfo(CAPABILITY_ID.toString(), logIn.serializeNBT()); }

        @Nonnull
        @Override
        public NBTBase serializeNBT() { return new NBTTagByte((byte)1); } // NO-OP

        @Override
        public void deserializeNBT(@Nullable final NBTBase nbt) {
            if(nbt instanceof NBTTagCompound) CampfireWoodType.deserializeNBT(woodTypes, (NBTTagCompound)nbt).ifPresent(this::set);
        }
    }

    class TileImpl implements ICampfireWoodType
    {
        @Nonnull final TileEntityCampfire campfire;
        @Nullable CampfireWoodType<?> woodType;

        public TileImpl(@Nonnull final TileEntityCampfire campfireIn) {
            campfire = campfireIn;
        }

        @Nullable
        @Override
        public CampfireWoodType<?> get() { return woodType; }

        @Override
        public void set(@Nullable final CampfireWoodType<?> logIn) { woodType = logIn; }

        @Override
        public void deserializeNBT(@Nullable final NBTBase nbt) {
            if(nbt instanceof NBTTagCompound && campfire.woodTypes != null) CampfireWoodType.deserializeNBT(
                    (IForgeRegistry<? extends CampfireWoodType<?>>)campfire.woodTypes, (NBTTagCompound)nbt).ifPresent(this::set);
        }
    }
}
