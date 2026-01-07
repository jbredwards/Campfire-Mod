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
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.quark.api.INonSticky;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 *
 * @author jbred
 *
 */
@Optional.InterfaceList({
@Optional.Interface(iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggable", modid = "fluidlogged_api"),
@Optional.Interface(iface = "git.jbredwards.piston_api.api.block.IStickyBehavior", modid = "piston_api"),
@Optional.Interface(iface = "vazkii.quark.api.INonSticky", modid = "quark")})
public class BlockChain extends BlockRotatedPillar implements IFluidloggable, IStickyBehavior, INonSticky
{
    @Nonnull
    public static final AxisAlignedBB[] AABB = new AxisAlignedBB[] {
            new AxisAlignedBB(0, 0.4, 0.4, 1, 0.6, 0.6),
            new AxisAlignedBB(0.4, 0, 0.4, 0.6, 1, 0.6),
            new AxisAlignedBB(0.4, 0.4, 0, 0.6, 0.6, 1)
    };

    public BlockChain(@Nonnull final Material materialIn) { this(materialIn, materialIn.getMaterialMapColor()); }
    public BlockChain(@Nonnull final Material materialIn, @Nonnull final MapColor color) {
        super(materialIn, color);
        setSoundType(CampfireSoundEvents.CHAIN_TYPE).setCreativeTab(CampfireCreativeTab.INSTANCE)
                .setHardness(5).setResistance(10).setHarvestLevel("pickaxe", 0);
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(@Nonnull final IBlockState state, @Nonnull final IBlockAccess source, @Nonnull final BlockPos pos) {
        return AABB[state.getValue(AXIS).ordinal()];
    }

    @Override
    public boolean isOpaqueCube(@Nonnull final IBlockState state) { return false; }

    @Override
    public boolean isFullCube(@Nonnull final IBlockState state) { return false; }

    @Override
    public boolean isSideSolid(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing side) { return false; }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull final IBlockAccess worldIn, @Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }

    // =====================
    // PISTON API CHAINSTONE
    // =====================

    @Optional.Method(modid = "piston_api")
    @Override
    public boolean hasStickySide(@Nonnull final IBlockSource source, @Nonnull final IPistonInfo pistonInfo) {
        return CampfireConfigHandler.Chain.chainstone != CampfireConfigHandler.Chain.Chainstone.DISABLED;
    }

    @Nonnull
    @Optional.Method(modid = "piston_api")
    @Override
    public EnumStickReaction getStickReaction(@Nonnull final IBlockSource source, @Nonnull final IBlockSource other, @Nonnull final IPistonInfo pistonInfo) {
        final boolean connects = CampfireConfigHandler.Chain.chainstone == CampfireConfigHandler.Chain.Chainstone.STICKY_AXIS
                ? IStickyBehavior.getConnectingSide(source, other).getAxis() == source.getBlockState().getValue(AXIS)
                : chainConnects(
                source.getBlockState().getBoundingBox(source.getWorld(), source.getBlockPos()).offset(source.getBlockPos()),
                source.getWorld(), other.getBlockPos(), other.getBlockState());

        return connects ? EnumStickReaction.STICK : EnumStickReaction.PASS;
    }

    public static boolean chainConnects(@Nonnull final World world, @Nonnull final BlockPos pos) {
        @Nonnull final AxisAlignedBB chainBB = new AxisAlignedBB(pos.getX() + 0.4999, pos.getY() + 1, pos.getZ() + 0.4999, pos.getX() + 0.5001, pos.getY() + 1, pos.getZ() + 0.5001);
        return chainConnects(chainBB, world, pos.up(), world.getBlockState(pos.up()));
    }

    public static boolean chainConnects(@Nonnull final AxisAlignedBB chainBB, @Nonnull final World world, @Nonnull final BlockPos neighborPos, @Nonnull final IBlockState neighbor) {
        @Nonnull final List<AxisAlignedBB> boxes = new ArrayList<>();
        neighbor.addCollisionBoxToList(world, neighborPos, chainBB.grow(0.001), boxes, null, false);
        // different area-check based on chain axis
        @Nonnull final ToDoubleFunction<AxisAlignedBB> areaGetter;
        if(chainBB.maxX - chainBB.minX == 1) areaGetter =
        bb -> (Math.min(bb.maxY, chainBB.maxY) - Math.max(bb.minY, chainBB.minY)) * (Math.min(bb.maxZ, chainBB.maxZ) - Math.max(bb.minZ, chainBB.minZ));
        else if(chainBB.maxZ - chainBB.minZ == 1) areaGetter =
        bb -> (Math.min(bb.maxX, chainBB.maxX) - Math.max(bb.minX, chainBB.minX)) * (Math.min(bb.maxY, chainBB.maxY) - Math.max(bb.minY, chainBB.minY));
        else areaGetter =
        bb -> (Math.min(bb.maxX, chainBB.maxX) - Math.max(bb.minX, chainBB.minX)) * (Math.min(bb.maxZ, chainBB.maxZ) - Math.max(bb.minZ, chainBB.minZ));
        // check full collision
        final double area = areaGetter.applyAsDouble(chainBB);
        for(@Nonnull final AxisAlignedBB bb : boxes) if(areaGetter.applyAsDouble(bb) == area) return true;
        return false;
    }

    // ========================
    // QUARK-SUPPORT CHAINSTONE
    // ========================

    @Nonnull
    private static final ThreadLocal<Boolean> checkingConnection = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Optional.Method(modid = "quark")
    @Override
    public boolean isStickyBlock(@Nonnull final IBlockState state) {
        return CampfireConfigHandler.Chain.chainstone != CampfireConfigHandler.Chain.Chainstone.DISABLED;
    }

    @Optional.Method(modid = "quark")
    @Override
    public boolean canStickToBlock(@Nonnull final World world,
                                   @Nonnull final BlockPos pistonPos, @Nonnull final BlockPos pos, @Nonnull final BlockPos slimePos,
                                   @Nonnull final IBlockState state, @Nonnull final IBlockState slimeState, @Nonnull final EnumFacing broken) {
        if(CampfireConfigHandler.Chain.chainstone == CampfireConfigHandler.Chain.Chainstone.STICKY_AXIS) switch(state.getValue(AXIS)) {
            case X: if(pos.getX() != slimePos.getX()) return true; break;
            case Y: if(pos.getY() != slimePos.getY()) return true; break;
            case Z: if(pos.getZ() != slimePos.getZ()) return true; break;
        }

        else if(chainConnects(state.getBoundingBox(world, pos).offset(pos), world, slimePos, slimeState)) return true;
        return checkOtherSticky(world, pistonPos, pos, slimePos, state, slimeState, broken);
    }

    /**
     * `canStickToBlock` returns a boolean of whether to stick at all, meaning that this method has to exist to check neighbor stickiness as well. Thanks Quark
     */
    @Optional.Method(modid = "quark")
    public static boolean checkOtherSticky(@Nonnull final World world,
                                           @Nonnull final BlockPos pistonPos, @Nonnull final BlockPos pos, @Nonnull final BlockPos slimePos,
                                           @Nonnull final IBlockState state, @Nonnull final IBlockState slimeState, @Nonnull final EnumFacing direction) {
        if(!slimeState.getBlock().isStickyBlock(slimeState)) return false;
        else if(!(slimeState.getBlock() instanceof INonSticky)) return true;
        else if(checkingConnection.get()) return false; // prevents a stack overflow exception

        checkingConnection.set(true);
        final boolean connects;
        try { connects = ((INonSticky)slimeState.getBlock()).canStickToBlock(world, pistonPos, slimePos, pos, slimeState, state, direction.getOpposite()); }
        finally { checkingConnection.set(false); }

        return connects;
    }
}
