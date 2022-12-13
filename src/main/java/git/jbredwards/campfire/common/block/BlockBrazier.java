package git.jbredwards.campfire.common.block;

import com.google.common.collect.ImmutableList;
import git.jbredwards.campfire.common.tileentity.TileEntityBrazier;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public class BlockBrazier extends AbstractCampfire<TileEntityBrazier>
{
    @Nonnull
    public static final List<AxisAlignedBB> BOUNDING_BOXES = ImmutableList.of();

    public BlockBrazier(@Nonnull Material materialIn, boolean isSmokeyIn) {
        this(materialIn, materialIn.getMaterialMapColor(), isSmokeyIn);
    }

    public BlockBrazier(@Nonnull Material materialIn, @Nonnull MapColor mapColorIn, boolean isSmokeyIn) {
        super(materialIn, mapColorIn, isSmokeyIn);
        setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Nonnull
    @Override
    public TileEntityBrazier createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityBrazier();
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
        return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT;
    }
}
