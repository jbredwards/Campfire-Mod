/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.capability;

import git.jbredwards.campfire.api.block.campfire.ICampfireType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public abstract class CampfireWoodType<V extends CampfireWoodType<V>> extends IForgeRegistryEntry.Impl<V>
{
    public static final class Normal extends CampfireWoodType<Normal>
    {
        public Normal(@Nonnull final ItemStack stackIn) { super(stackIn); }
    }

    @Nonnull
    protected final ItemStack stack;
    public CampfireWoodType(@Nonnull final ItemStack stackIn) {
        assert !Objects.requireNonNull(stackIn).isEmpty();
        stack = stackIn;
    }

    @Nonnull
    public ItemStack asItemStack() {
        return stack;
    }

    @Nonnull
    public static Optional<? extends CampfireWoodType<?>> fromStack(
            @Nonnull final IForgeRegistry<? extends CampfireWoodType<?>> campfireType,
            @Nonnull final ItemStack stack) {
        return fromStack(campfireType, stack.getItem(), stack.getMetadata());
    }

    @Nonnull
    public static Optional<? extends CampfireWoodType<?>> fromStack(
            @Nonnull final IForgeRegistry<? extends CampfireWoodType<?>> campfireType,
            @Nonnull final Block block, final int meta) {
        return fromStack(campfireType, Item.getItemFromBlock(block), meta);
    }

    @Nonnull
    public static Optional<? extends CampfireWoodType<?>> fromStack(
            @Nonnull final IForgeRegistry<? extends CampfireWoodType<?>> campfireType,
            @Nonnull final Item item, final int meta) {
        return campfireType.getValuesCollection().stream()
                .filter(wt -> wt.asItemStack().getItem() == item && wt.asItemStack().getMetadata() == meta)
                .findFirst();
    }

    @Nonnull
    public static Optional<? extends CampfireWoodType<?>> deserializeNBT(
            @Nonnull final IForgeRegistry<? extends CampfireWoodType<?>> campfireType,
            @Nonnull final NBTTagCompound nbt) {
        if(nbt.hasKey("wood_type", Constants.NBT.TAG_STRING)) return Optional.ofNullable(campfireType.getValue(new ResourceLocation(nbt.getString("wood_type"))));
        else return Optional.ofNullable(Item.getByNameOrId(nbt.getString("id"))).flatMap(item -> fromStack(campfireType, item, nbt.getInteger("Damage"))); // old data
    }

    @Nonnull
    public NBTTagCompound serializeNBT() {
        @Nonnull final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("wood_type", String.valueOf(getRegistryName()));
        return nbt;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    public IBakedModel getModel() {
        return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(asItemStack());
    }

    public static <V extends CampfireWoodType<V>> void postInit(
            @Nonnull final IForgeRegistry<V> woodTypes,
            @Nonnull final Function<ItemStack, V> woodTypeConstructor) {
        OreDictionary.getOres("logWood", false).stream()
                // get all sub-items, if applicable
                .flatMap(log -> {
                    if(log.getMetadata() != OreDictionary.WILDCARD_VALUE) return Stream.of(log);
                    @Nonnull final NonNullList<ItemStack> logs = NonNullList.create();

                    log.getItem().getSubItems(CreativeTabs.SEARCH, logs);
                    return logs.stream();
                })
                // only register wood types registered ItemBlocks
                .filter(log -> {
                    if(!(log.getItem() instanceof ItemBlock) || log.getItem().getRegistryName() == null) return false;
                    // don't register duplicate wood types, and don't allow wood types of clear/translucent blocks
                    else return !fromStack(woodTypes, log).isPresent() && ((ItemBlock)log.getItem()).getBlock().getDefaultState().isOpaqueCube();
                })
                // generate wood types from logs, and add to registry
                .forEach(log -> {
                    @Nonnull final String name = "generated/" + String.valueOf(log.getItem().getRegistryName()).replace(':', '/') + '/' + log.getMetadata();
                    woodTypes.register(woodTypeConstructor.apply(log).setRegistryName(name));
                });
    }
}
