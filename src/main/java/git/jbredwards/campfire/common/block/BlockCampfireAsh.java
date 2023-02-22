package git.jbredwards.campfire.common.block;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
@Optional.Interface(modid = "fluidlogged_api", iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggable")
public class BlockCampfireAsh extends BlockFalling implements IFluidloggable
{
    @Nonnull public static final PropertyInteger LAYERS = PropertyInteger.create("layers", 1, 16);
    @Nonnull public static final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 1, 0.0625, 1);

    public BlockCampfireAsh(@Nonnull Material materialIn) {
        super(materialIn);
        setDefaultState(getDefaultState().withProperty(LAYERS, 1));
        setSoundType(SoundType.SAND).setCreativeTab(CreativeTabs.DECORATIONS)
                .setHardness(0.15f).setHarvestLevel("shovel", 0);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LAYERS, MathHelper.clamp(meta + 1, 1, 16));
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return state.getValue(LAYERS) - 1;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LAYERS);
    }

    @Nonnull
    @Override
    public MapColor getMapColor(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        return MapColor.OBSIDIAN;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        return AABB.setMaxY(state.getValue(LAYERS) * 0.0625);
    }

    @Nonnull
    @Override
    public ItemStack getItem(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        return new ItemStack(this);
    }

    @Nonnull
    @Override
    public Item getItemDropped(@Nonnull IBlockState state, @Nonnull Random rand, int fortune) { return Items.COAL; }

    @Override
    public int quantityDropped(@Nonnull IBlockState state, int fortune, @Nonnull Random random) {
        return (int)((state.getValue(LAYERS) << 1) * Math.min((fortune + 1) * random.nextFloat(), 1));
    }

    @Override
    public int damageDropped(@Nonnull IBlockState state) { return 1; }

    @Nonnull
    @Override
    protected ItemStack getSilkTouchDrop(@Nonnull IBlockState state) {
        return new ItemStack(this, state.getValue(LAYERS));
    }

    @Override
    protected boolean canSilkHarvest() { return true; }

    @Override
    public float getBlockHardness(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        return super.getBlockHardness(state, worldIn, pos) * state.getValue(LAYERS);
    }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) { return state.getValue(LAYERS) == 16; }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) { return state.isFullCube(); }

    @Override
    public boolean causesSuffocation(@Nonnull IBlockState state) { return state.getValue(LAYERS) >= 15; }

    @Override
    public boolean isSideSolid(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return state.isFullCube() || side == EnumFacing.DOWN;
    }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return state.isSideSolid(worldIn, pos, face) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        final ItemStack held = playerIn.getHeldItem(hand);
        if(held.getItem() == Item.getItemFromBlock(this)) {
            final int layers = state.getValue(LAYERS);
            if(layers < 16) {
                final IBlockState newState = state.withProperty(LAYERS, layers + 1);
                final AxisAlignedBB collision = newState.getCollisionBoundingBox(worldIn, pos);
                if((collision == null || worldIn.checkNoEntityCollision(collision.offset(pos))) && worldIn.setBlockState(pos, newState, 11)) {
                    final SoundType soundtype = state.getBlock().getSoundType(newState, worldIn, pos, playerIn);
                    worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1) / 2, soundtype.getPitch() * 0.8f);

                    if(!playerIn.isCreative()) held.shrink(1);
                    return true;
                }
            }
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        final int layers = state.getValue(LAYERS);
        if(layers < 16 && side == EnumFacing.UP) return true;

        final BlockPos neighborPos = pos.offset(side);
        final IBlockState neighbor = world.getBlockState(neighborPos);

        return !neighbor.doesSideBlockRendering(world, neighborPos, side.getOpposite()) &&
                (side == EnumFacing.DOWN || !(neighbor.getBlock() instanceof BlockCampfireAsh) || neighbor.getValue(LAYERS) < layers);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(@Nonnull IBlockState stateIn, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);
        if(rand.nextFloat() < 0.25) {
            final double yOffset = stateIn.getValue(LAYERS) * 0.0625 + 0.0625;
            for(EnumFacing side : EnumFacing.values()) {
                if(stateIn.shouldSideBeRendered(worldIn, pos, side) && (side.getAxis().isVertical() || rand.nextFloat() < yOffset)) {
                    final double x = pos.getX() + (side.getXOffset() == 0 ? rand.nextFloat() : side.getXOffset() == 1 ? 1.0625 : -0.0625);
                    final double y = pos.getY() + (side.getYOffset() == 0 ? rand.nextFloat() * yOffset : side.getYOffset() == 1 ? yOffset : -0.0625);
                    final double z = pos.getZ() + (side.getZOffset() == 0 ? rand.nextFloat() : side.getZOffset() == 1 ? 1.0625 : -0.0625);
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0, 0, 0);
                }
            }
        }
    }

    @Optional.Method(modid = "fluidlogged_api")
    @Override
    public boolean isFluidloggable(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
        return state.getValue(LAYERS) <= 14;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getDustColor(@Nonnull IBlockState state) { return 0x3a485f; }

    @Override
    protected void onStartFalling(@Nonnull EntityFallingBlock fallingEntity) { fallingEntity.shouldDropItem = false; }
}
