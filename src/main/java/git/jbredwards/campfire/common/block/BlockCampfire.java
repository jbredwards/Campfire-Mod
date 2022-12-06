package git.jbredwards.campfire.common.block;

import git.jbredwards.campfire.common.block.state.ItemStackProperty;
import git.jbredwards.campfire.common.capability.ICampfireType;
import git.jbredwards.campfire.common.item.ItemCampfire;
import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(modid = "campfire")
@Optional.Interface(modid = "fluidlogged_api", iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggable")
public class BlockCampfire extends BlockHorizontal implements ITileEntityProvider, IFluidloggable
{
    @Nonnull
    public static final PropertyBool LIT = PropertyBool.create("lit"), POWERED = PropertyBool.create("powered");

    public BlockCampfire(@Nonnull Material materialIn) { this(materialIn, materialIn.getMaterialMapColor()); }
    public BlockCampfire(@Nonnull Material materialIn, @Nonnull MapColor mapColorIn) {
        super(materialIn, mapColorIn);
        setDefaultState(getDefaultState().withProperty(POWERED, false));
        setSoundType(SoundType.WOOD).setHardness(2).setLightOpacity(2).setHarvestLevel("axe", 0);
    }

    @Override
    public int getLightValue(@Nonnull IBlockState state) { return state.getValue(LIT) ? 15 : 0; }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(meta >> 2))
                .withProperty(LIT, (meta & 2) != 0)
                .withProperty(POWERED, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return (state.getValue(FACING).getHorizontalIndex() << 2)
                | (state.getValue(LIT) ? 2 : 0)
                | (state.getValue(POWERED) ? 1 : 0);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(FACING, LIT, POWERED).add(ItemStackProperty.INSTANCE).build();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) { return new TileEntityCampfire(); }

    //============
    //ITEM HELPERS
    //============

    @Nonnull
    @Override
    public Item getItemDropped(@Nonnull IBlockState state, @Nonnull Random rand, int fortune) { return Items.COAL; }

    @Override
    public int damageDropped(@Nonnull IBlockState state) { return 1; }

    @Override
    public int quantityDropped(@Nonnull Random random) { return 2; }

    @Override
    protected boolean canSilkHarvest() { return true; }

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
        final IBlockState state = placer.isSneaking() ? getDefaultState().withProperty(FACING, placer.getHorizontalFacing()) : getDefaultState();
        if(worldIn.isBlockPowered(pos)) return state.withProperty(POWERED, true);
        return state;
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        final ICampfireType stackCap = ICampfireType.get(stack);
        if(stackCap != null) {
            final ICampfireType tileCap = ICampfireType.get(worldIn.getTileEntity(pos));
            if(tileCap != null) tileCap.set(stackCap.get());
        }
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
        for(int i = 0; i < 4; i++) //testing with vanilla logs
            items.add(ItemCampfire.applyType(new ItemStack(this), new ItemStack(Blocks.LOG, 1, i)));
    }

    @Nonnull
    @Override
    public ItemStack getItem(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        final ICampfireType type = ICampfireType.get(worldIn.getTileEntity(pos));
        return type != null ? ItemCampfire.applyType(new ItemStack(this), type.get()) : new ItemStack(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void handleSilkDrop(@Nonnull BlockEvent.HarvestDropsEvent event) {
        if(event.isSilkTouching() && event.getState().getBlock() instanceof BlockCampfire) {
            final ICampfireType tileCap = ICampfireType.get(event.getWorld().getTileEntity(event.getPos()));
            if(tileCap != null) event.getDrops().forEach(stack -> {
                final ICampfireType stackCap = ICampfireType.get(stack);
                if(stackCap != null) stackCap.set(tileCap.get());
            });
        }
    }

    //=============================
    //GENERIC NON-SOLID BLOCK STUFF
    //=============================

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

    //=========
    //RENDERING
    //=========

    @SideOnly(Side.CLIENT)
    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT && state.getValue(LIT);
    }

    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        if(state instanceof IExtendedBlockState) {
            final @Nullable ICampfireType type = ICampfireType.get(world.getTileEntity(pos));
            if(type != null) return ((IExtendedBlockState)state).withProperty(ItemStackProperty.INSTANCE, type.get());
        }

        return state;
    }

    //===============
    //BLOCK PARTICLES
    //===============



    //=========
    //COLLISION
    //=========



    //===========================
    //FLUIDLOGGED API INTEGRATION
    //===========================

    @Optional.Method(modid = "fluidlogged_api")
    @Nonnull
    @Override
    public EnumActionResult onFluidFill(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState here, @Nonnull FluidState newFluid, int blockFlags) {
        world.playEvent(Constants.WorldEvents.FIRE_EXTINGUISH_SOUND, pos, 0);
        world.setBlockState(pos, here.withProperty(LIT, false), 2);
        return EnumActionResult.PASS;
    }
}
