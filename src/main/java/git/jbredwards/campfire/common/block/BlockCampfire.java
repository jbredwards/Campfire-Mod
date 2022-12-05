package git.jbredwards.campfire.common.block;

import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
@Optional.Interface(modid = "fluidlogged_api", iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggable")
public class BlockCampfire extends Block implements ITileEntityProvider, IFluidloggable
{
    @Nonnull
    public static final PropertyBool LIT = PropertyBool.create("lit"), POWERED = PropertyBool.create("powered");

    public BlockCampfire(@Nonnull Material materialIn) { this(materialIn, materialIn.getMaterialMapColor()); }
    public BlockCampfire(@Nonnull Material materialIn, @Nonnull MapColor mapColorIn) {
        super(materialIn, mapColorIn);
        setDefaultState(getDefaultState().withProperty(LIT, true).withProperty(POWERED, false));
        setSoundType(SoundType.WOOD).setHardness(2).setHarvestLevel("axe", 0);
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return (state.getValue(LIT) ? 2 : 0) | (state.getValue(POWERED) ? 1 : 0);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LIT, (meta & 2) != 0).withProperty(POWERED, (meta & 1) != 0);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(LIT, POWERED).build();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) { return new TileEntityCampfire(); }

    @Override
    public int getLightValue(@Nonnull IBlockState state) { return state.getValue(LIT) ? 15 : 0; }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) { return false; }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) { return false; }


    @Override
    public boolean isSideSolid(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return false;
    }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT;
    }

    @Optional.Method(modid = "fluidlogged_api")
    @Nonnull
    @Override
    public EnumActionResult onFluidFill(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState here, @Nonnull FluidState newFluid, int blockFlags) {
        world.setBlockState(pos, here.withProperty(LIT, false), 2);
        return EnumActionResult.PASS;
    }
}
