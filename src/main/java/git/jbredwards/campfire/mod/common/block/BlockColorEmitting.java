/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.block;

import com.google.common.collect.Lists;
import git.jbredwards.campfire.mod.common.block.state.ColorProperty;
import git.jbredwards.campfire.mod.common.item.ItemBlockColored;
import git.jbredwards.campfire.mod.common.tileentity.TileEntityColorEmitting;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Optional;
import vazkii.quark.experimental.features.ColoredLights;
import vazkii.quark.experimental.lighting.ColoredLightSystem;
import vazkii.quark.experimental.lighting.IColoredLightSource;
import vazkii.quark.experimental.lighting.LightSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
@Optional.Interface(iface = "vazkii.quark.experimental.lighting.IColoredLightSource", modid = "quark")
public class BlockColorEmitting extends Block implements ITileEntityProvider, IColoredLightSource
{
    public BlockColorEmitting(@Nonnull final Material materialIn) { this(materialIn, materialIn.getMaterialMapColor()); }
    public BlockColorEmitting(@Nonnull final Material materialIn, @Nonnull final MapColor mapColorIn) {
        super(materialIn, mapColorIn);
        setLightLevel(1);
    }

    @Nonnull
    @Override
    protected final BlockStateContainer createBlockState() { return createStateBuilder().build(); }

    @Nonnull
    protected BlockStateContainer.Builder createStateBuilder() {
        return new BlockStateContainer.Builder(this).add(ColorProperty.INSTANCE);
    }

    @Nonnull
    @Override
    public final TileEntity createNewTileEntity(@Nonnull final World world, final int meta) {
        return createTileEntity(world, getStateFromMeta(meta));
    }

    @Nonnull
    @Override
    public TileEntityColorEmitting createTileEntity(@Nonnull final World world, @Nonnull final IBlockState state) {
        return new TileEntityColorEmitting();
    }

    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
        return state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).withProperty(ColorProperty.INSTANCE, TileEntityColorEmitting.getColor(world.getTileEntity(pos))) : state;
    }

    @Override
    public void onBlockPlacedBy(@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, @Nonnull final EntityLivingBase placer, @Nonnull final ItemStack stack) {
        @Nullable final TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileEntityColorEmitting) ((TileEntityColorEmitting)tile).color = ItemBlockColored.getColor(stack);
    }

    @Nonnull
    @Override
    public ItemStack getItem(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        return getItem(state, worldIn.getTileEntity(pos));
    }

    @Nonnull
    public ItemStack getItem(@Nonnull IBlockState state, @Nullable TileEntity tile) {
        return ItemBlockColored.applyColor(getSilkTouchDrop(state), TileEntityColorEmitting.getColor(tile));
    }

    @Nonnull
    @Override
    protected ItemStack getSilkTouchDrop(@Nonnull final IBlockState state) { return new ItemStack(this); }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void harvestBlock(@Nonnull World worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity tile, @Nonnull ItemStack stack) {
        if(!(tile instanceof TileEntityColorEmitting)) {
            super.harvestBlock(worldIn, player, pos, state, tile, stack);
            return;
        }

        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005f);

        //ensure silk touch drop captures type stored in tile entity
        if(canSilkHarvest(worldIn, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
            final List<ItemStack> drops = Lists.newArrayList(getItem(state, tile));
            ForgeEventFactory.fireBlockHarvesting(drops, worldIn, pos, state, 0, 1, true, player);
            drops.forEach(drop -> spawnAsEntity(worldIn, pos, drop));
        }

        //old code for no silk touch
        else {
            harvesters.set(player);
            dropBlockAsItem(worldIn, pos, state, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack));
            harvesters.set(null);
        }
    }

    @Nullable
    @Override
    public float[] getBeaconColorMultiplier(@Nonnull final IBlockState state, @Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final BlockPos beaconPos) {
        final int color = TileEntityColorEmitting.getColor(world.getTileEntity(pos));
        return color == -1 ? null : new Color(color).brighter().getColorComponents(new float[3]);
    }

    // ===============================
    // MODDED COLORED LIGHTING SUPPORT
    // ===============================

    @Nonnull
    public static float[] getColoredLight(@Nullable final TileEntity tile) {
        final int color = TileEntityColorEmitting.getColor(tile);
        return color == -1 ? new float[0] : new float[] {(color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f};
    }

    @Optional.Method(modid = "quark")
    @Override
    public int getLightOpacity(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
        @Nonnull final Field enabled = ColoredLights.class.getDeclaredFields()[0];
        if(!enabled.isAccessible()) enabled.setAccessible(true);
        final boolean canAddSource;

        try { canAddSource = enabled.getBoolean(null); }
        catch(@Nonnull final IllegalAccessException e) { return lightOpacity; }

        if(canAddSource && TileEntityColorEmitting.getColor(world.getTileEntity(pos)) != -1) ColoredLights.addLightSource(world, pos, state);
        else if(canAddSource) {
            @Nonnull final Field lightSources = ColoredLightSystem.class.getDeclaredFields()[0];
            if(!lightSources.isAccessible()) lightSources.setAccessible(true);

            @Nonnull final List<LightSource> sources;
            try { sources = (List<LightSource>)lightSources.get(null); }
            catch(@Nonnull final IllegalAccessException e) { return lightOpacity; }

            sources.removeIf(src -> src == null || src.pos.equals(pos));
        }

        return lightOpacity;
    }

    @Optional.Method(modid = "quark")
    @Nonnull
    @Override
    public float[] getColoredLight(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
        return getColoredLight(world.getTileEntity(pos));
    }
}
