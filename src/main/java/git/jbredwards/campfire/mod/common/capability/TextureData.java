/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.capability;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 * @author jbred
 *
 */
public class TextureData implements Comparable<TextureData>
{
    @Nonnull
    public static final Map<Pair<Item, Integer>, ItemStack> STACK_CACHE = new HashMap<>();

    @Nonnull
    public final Item item;
    public final int meta;

    public TextureData(@Nonnull final ItemStack stack) { this(stack.getItem(), stack.getMetadata()); }
    public TextureData(@Nonnull final Item itemIn, final int metaIn) {
        item = itemIn;
        meta = metaIn;
    }

    public TextureData(@Nonnull final NBTTagCompound nbt) {
        item = Optional.ofNullable(Item.getByNameOrId(nbt.getString("id"))).orElseGet(() -> Item.getItemFromBlock(Blocks.LOG));
        meta = nbt.getShort("Damage");
    }

    @Nonnull
    public NBTTagCompound serializeNBT() {
        @Nonnull final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("id", String.valueOf(item.getRegistryName()));
        nbt.setShort("Damage", (short)meta);
        return nbt;
    }

    @Nonnull
    @Override
    public String toString() {
        return "(" + item.getRegistryName() + ',' + meta + ')';
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(item).append(meta).toHashCode();
    }

    @Override
    public int compareTo(@Nullable final TextureData o) {
        return o != null ? new CompareToBuilder().append(item, o.item).append(meta, o.meta).toComparison() : -1;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return obj == this || obj instanceof TextureData && meta == ((TextureData)obj).meta && item == ((TextureData)obj).item;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    public IBakedModel getModel() {
        return runStackAction(Minecraft.getMinecraft().getRenderItem().getItemModelMesher()::getItemModel);
    }

    @Nonnull
    public <T> T runStackAction(@Nonnull final Function<ItemStack, T> action) {
        return action.apply(STACK_CACHE.computeIfAbsent(Pair.of(item, meta), p -> new ItemStack(item, 1, meta)));
    }
}
