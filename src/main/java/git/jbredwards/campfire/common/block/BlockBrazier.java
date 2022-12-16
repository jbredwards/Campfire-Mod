package git.jbredwards.campfire.common.block;

import com.google.common.collect.ImmutableList;
import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.tileentity.TileEntityBrazier;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public class BlockBrazier extends AbstractCampfire<TileEntityBrazier>
{
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

    public BlockBrazier(@Nonnull Material materialIn, boolean isSmokeyIn) {
        this(materialIn, materialIn.getMaterialMapColor(), isSmokeyIn);
    }

    public BlockBrazier(@Nonnull Material materialIn, @Nonnull MapColor mapColorIn, boolean isSmokeyIn) {
        super(materialIn, mapColorIn, isSmokeyIn);
        setSoundType(SoundType.METAL).setCreativeTab(CreativeTabs.DECORATIONS)
                .setHardness(2).setLightOpacity(2).setHarvestLevel("pickaxe", 0);
    }

    @Nonnull
    @Override
    public TileEntityBrazier createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityBrazier();
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
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
        return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isSmokey() { return isSmokey && CampfireConfigHandler.brazierEmitsSmoke; }
}
