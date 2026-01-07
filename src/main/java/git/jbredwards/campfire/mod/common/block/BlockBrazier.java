/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.block;

import com.google.common.collect.ImmutableList;
import git.jbredwards.campfire.api.block.campfire.ICampfireSettings;
import git.jbredwards.campfire.api.block.campfire.ICampfireType;
import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.mod.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.mod.common.init.CampfireSoundEvents;
import git.jbredwards.campfire.mod.common.item.tab.CampfireCreativeTab;
import git.jbredwards.campfire.mod.common.tileentity.TileEntityBrazier;
import git.jbredwards.piston_api.api.block.IStickyBehavior;
import git.jbredwards.piston_api.api.piston.EnumStickReaction;
import git.jbredwards.piston_api.api.piston.IPistonInfo;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.quark.api.INonSticky;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
@Optional.InterfaceList({
@Optional.Interface(iface = "git.jbredwards.piston_api.api.block.IStickyBehavior", modid = "piston_api"),
@Optional.Interface(iface = "vazkii.quark.api.INonSticky", modid = "quark")})
public class BlockBrazier<V extends CampfireWoodType<V>> extends AbstractCampfire<V> implements IStickyBehavior, INonSticky
{
    @Nonnull public static final PropertyBool HANGING = PropertyBool.create("hanging");

    @Nonnull public static final AxisAlignedBB AABB = box(0, 0, 0, 16, 14, 16);
    @Nonnull public static final List<AxisAlignedBB> BOUNDING_BOXES = ImmutableList.of(
            //ash
            box(0,  0, 0,  16, 2,  16),
            //cage
            box(0,  2, 0,  16, 14, 0),
            box(0,  2, 0,  0,  14, 16),
            box(0,  2, 16, 16, 14, 16),
            box(16, 2, 0,  16, 14, 16)
    );

    public BlockBrazier(@Nonnull final Material materialIn, @Nonnull final ICampfireType<V> campfireTypeIn, @Nonnull final ICampfireSettings campfireSettingsIn) {
        this(materialIn, materialIn.getMaterialMapColor(), campfireTypeIn, campfireSettingsIn);
    }

    public BlockBrazier(@Nonnull final Material materialIn, @Nonnull final MapColor mapColorIn, @Nonnull final ICampfireType<V> campfireTypeIn, @Nonnull final ICampfireSettings campfireSettingsIn) {
        super(materialIn, mapColorIn, campfireTypeIn, campfireSettingsIn);
        setDefaultState(getDefaultState().withProperty(HANGING, false));
        setSoundType(CampfireSoundEvents.LANTERN_TYPE).setCreativeTab(CampfireCreativeTab.INSTANCE)
                .setHardness(3.5f).setLightOpacity(2).setHarvestLevel("pickaxe", 0);
    }

    @Override
    public int damageDropped(@Nonnull final IBlockState state) {
        return !campfireSettings.unlitOnCraft() && state.getValue(LIT) ? 0 : 1;
    }

    @Nonnull
    @Override
    protected BlockStateContainer.Builder createStateBuilder() {
        return super.createStateBuilder().add(HANGING);
    }

    @Override
    public int getMetaFromState(@Nonnull final IBlockState state) {
        return super.getMetaFromState(state) | (state.getValue(HANGING) ? 8 : 0);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return super.getStateFromMeta(meta).withProperty(HANGING, (meta & 8) != 0);
    }

    @Nonnull
    @Override
    public IBlockState getStateForWorld(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state) {
        @Nonnull List<AxisAlignedBB> boxes = new ArrayList<>();
        world.getBlockState(pos.down()).addCollisionBoxToList(world, pos.down(), AABB.offset(pos).grow(0.001), boxes, null, false);
        return super.getStateForWorld(world, pos, state).withProperty(HANGING, boxes.isEmpty() && BlockChain.chainConnects(world, pos));
    }

    @Nonnull
    @Override
    public TileEntityBrazier createTileEntity(@Nonnull final World world, @Nonnull final IBlockState state) {
        return new TileEntityBrazier();
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(@Nonnull final IBlockState state, @Nonnull final IBlockAccess source, @Nonnull final BlockPos pos) {
        return AABB;
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        return rayTrace(pos, start, end, blockState.getBoundingBox(worldIn, pos));
    }

    @Nonnull
    @Override
    public List<AxisAlignedBB> getCollisionBoxList(@Nonnull IBlockState state) { return BOUNDING_BOXES; }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        if(face == EnumFacing.DOWN) return BlockFaceShape.SOLID;
        else return face == EnumFacing.UP ? BlockFaceShape.BOWL : BlockFaceShape.UNDEFINED;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || layer == campfireType.getFireLayer();
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
