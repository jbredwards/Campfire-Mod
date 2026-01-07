/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.block;

import git.jbredwards.campfire.mod.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.mod.common.init.CampfireSoundEvents;
import git.jbredwards.campfire.mod.common.item.tab.CampfireCreativeTab;
import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import git.jbredwards.piston_api.api.block.IStickyBehavior;
import git.jbredwards.piston_api.api.piston.EnumStickReaction;
import git.jbredwards.piston_api.api.piston.IPistonInfo;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import vazkii.quark.api.INonSticky;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Optional.InterfaceList({
@Optional.Interface(iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggable", modid = "fluidlogged_api"),
@Optional.Interface(iface = "git.jbredwards.piston_api.api.block.IStickyBehavior", modid = "piston_api"),
@Optional.Interface(iface = "vazkii.quark.api.INonSticky", modid = "quark")})
public class BlockLantern extends BlockColorEmitting implements IFluidloggable, IStickyBehavior, INonSticky
{
    @Nonnull
    public static final AxisAlignedBB GROUND_BB = new AxisAlignedBB(0.3, 0, 0.3, 0.7, 0.7, 0.7), HANGING_BB = new AxisAlignedBB(0.3, 0.0625, 0.3, 0.7, 1, 0.7);

    @Nonnull
    public static final PropertyBool HANGING = PropertyBool.create("hanging");
    public BlockLantern(@Nonnull final Material materialIn) { this(materialIn, materialIn.getMaterialMapColor()); }
    public BlockLantern(@Nonnull final Material materialIn, @Nonnull final MapColor mapColorIn) {
        super(materialIn, mapColorIn);
        setSoundType(CampfireSoundEvents.LANTERN_TYPE).setCreativeTab(CampfireCreativeTab.INSTANCE)
                .setHardness(3.5f).setLightLevel(1).setHarvestLevel("pickaxe", 0);
    }

    @Nonnull
    @Override
    protected BlockStateContainer.Builder createStateBuilder() { return super.createStateBuilder().add(HANGING); }

    @Override
    public int getMetaFromState(@Nonnull final IBlockState state) { return state.getValue(HANGING) ? 1 : 0; }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(final int meta) { return getDefaultState().withProperty(HANGING, (meta & 1) == 1); }

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, @Nonnull final EntityLivingBase placer) {
        @Nonnull final EnumFacing side = facing.getAxis() != EnumFacing.Axis.Y ? EnumFacing.byIndex(MathHelper.floor((placer.rotationPitch - 180) / 360 + 0.5) & 1).getOpposite() : facing;
        return getDefaultState().withProperty(HANGING, side == EnumFacing.DOWN && canPlaceOnSide(worldIn, pos, side));
    }

    @Override
    public void neighborChanged(@Nonnull final IBlockState state, @Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final Block blockIn, @Nonnull final BlockPos fromPos) {
        if(pos.getY() != fromPos.getY() && !canPlaceOnSide(worldIn, pos, state.getValue(HANGING) ? EnumFacing.DOWN : EnumFacing.UP)) worldIn.destroyBlock(pos, true);
    }

    @Override
    public boolean canPlaceBlockAt(@Nonnull final World worldIn, @Nonnull final BlockPos pos) {
        return canPlaceOnSide(worldIn, pos, EnumFacing.UP) || canPlaceOnSide(worldIn, pos, EnumFacing.DOWN);
    }

    public boolean canPlaceOnSide(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing side) {
        if(side == EnumFacing.DOWN) return BlockChain.chainConnects(world, pos);

        @Nonnull final AxisAlignedBB checkBB = new AxisAlignedBB(pos.getX() + 0.4999, pos.getY(), pos.getZ() + 0.4999, pos.getX() + 0.5001, pos.getY(), pos.getZ() + 0.5001);
        return BlockChain.chainConnects(checkBB, world, pos.down(), world.getBlockState(pos.down()));
    }

    @Override
    public boolean canRenderInLayer(@Nonnull final IBlockState state, @Nonnull final BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(@Nonnull final IBlockState state) { return false; }

    @Override
    public boolean isOpaqueCube(@Nonnull final IBlockState state) { return false; }

    @Override
    public boolean isSideSolid(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing side) { return false; }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull final IBlockAccess worldIn, @Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(@Nonnull final IBlockState state, @Nonnull final IBlockAccess source, @Nonnull final BlockPos pos) {
        return state.getValue(HANGING) ? HANGING_BB : GROUND_BB;
    }

    @Override
    public void getDrops(@Nonnull final NonNullList<ItemStack> drops, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, final int fortune) {
        drops.add(getItem(state, world.getTileEntity(pos)));
    }

    // =====================
    // PISTON API CHAINSTONE
    // =====================

    @Optional.Method(modid = "piston_api")
    @Override
    public boolean hasStickySide(@Nonnull final IBlockSource source, @Nonnull final IPistonInfo pistonInfo) {
        return CampfireConfigHandler.Chain.chainstone != CampfireConfigHandler.Chain.Chainstone.DISABLED && source.getBlockState().getValue(HANGING);
    }

    @Nonnull
    @Optional.Method(modid = "piston_api")
    @Override
    public EnumStickReaction getStickReaction(@Nonnull final IBlockSource source, @Nonnull final IBlockSource other, @Nonnull final IPistonInfo pistonInfo) {
        return IStickyBehavior.getConnectingSide(source, other) == EnumFacing.UP ? EnumStickReaction.STICK : EnumStickReaction.PASS;
    }

    // ========================
    // QUARK-SUPPORT CHAINSTONE
    // ========================

    @Optional.Method(modid = "quark")
    @Override
    public boolean isStickyBlock(@Nonnull final IBlockState state) {
        return CampfireConfigHandler.Chain.chainstone != CampfireConfigHandler.Chain.Chainstone.DISABLED && state.getValue(HANGING);
    }

    @Optional.Method(modid = "quark")
    @Override
    public boolean canStickToBlock(@Nonnull final World world,
                                   @Nonnull final BlockPos pistonPos, @Nonnull final BlockPos pos, @Nonnull final BlockPos slimePos,
                                   @Nonnull final IBlockState state, @Nonnull final IBlockState slimeState, @Nonnull final EnumFacing broken) {
        return pos.equals(slimePos.down()) || BlockChain.checkOtherSticky(world, pistonPos, pos, slimePos, state, slimeState, broken);
    }
}
