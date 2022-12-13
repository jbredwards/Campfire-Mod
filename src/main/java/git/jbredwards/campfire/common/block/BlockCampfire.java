package git.jbredwards.campfire.common.block;

import com.google.common.collect.ImmutableList;
import git.jbredwards.campfire.Campfire;
import git.jbredwards.campfire.common.block.state.ItemStackProperty;
import git.jbredwards.campfire.common.capability.ICampfireType;
import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.item.ItemCampfire;
import git.jbredwards.campfire.common.message.MessageFallParticles;
import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("deprecation")
public class BlockCampfire extends AbstractCampfire<TileEntityCampfire>
{
    @Nonnull public static final PropertyBool X_AXIS = PropertyBool.create("x_axis");
    @Nonnull public static final AxisAlignedBB AABB = box(0, 0, 0, 16, 7, 16);
    @Nonnull public static final List<AxisAlignedBB>
            X_AABB = ImmutableList.of(
                    //ash
                    box(0, 0, 5, 16, 1, 11),
                    //logs
                    box(0,  0, 1,  16, 4, 5),
                    box(0,  0, 11, 16, 4, 15),
                    box(1,  3, 0,  5,  7, 16),
                    box(11, 3, 0,  15, 7, 16)
            ),
            Z_AABB = ImmutableList.of(
                    //ash
                    box(5, 0, 0, 11, 1, 16),
                    //logs
                    box(1,  0, 0,  5,  4, 16),
                    box(11, 0, 0,  15, 4, 16),
                    box(0,  3, 1,  16, 7, 5),
                    box(0,  3, 11, 16, 7, 15)
            );

    public BlockCampfire(@Nonnull Material materialIn, boolean isSmokeyIn) {
        this(materialIn, materialIn.getMaterialMapColor(), isSmokeyIn);
    }

    public BlockCampfire(@Nonnull Material materialIn, @Nonnull MapColor mapColorIn, boolean isSmokeyIn) {
        super(materialIn, mapColorIn, isSmokeyIn);
        setSoundType(SoundType.WOOD).setCreativeTab(CreativeTabs.DECORATIONS)
                .setHardness(2).setLightOpacity(2).setHarvestLevel("axe", 0);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(X_AXIS, (meta & 8) != 0)
                .withProperty(SIGNAL, (meta & 4) != 0)
                .withProperty(LIT, (meta & 2) != 0)
                .withProperty(POWERED, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return (state.getValue(X_AXIS) ? 8 : 0)
                | (state.getValue(SIGNAL) ? 4 : 0)
                | (state.getValue(LIT) ? 2 : 0)
                | (state.getValue(POWERED) ? 1 : 0);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(X_AXIS, SIGNAL, LIT, POWERED).add(ItemStackProperty.INSTANCE).build();
    }

    @Nonnull
    @Override
    public TileEntityCampfire createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityCampfire();
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(isNonFluidlogged(worldIn, pos)) {
            final ItemStack stack = playerIn.getHeldItem(hand);
            if(!playerIn.canPlayerEdit(pos, facing, stack)) return false;

            if(handleFireIgnite(worldIn, pos, state, playerIn, stack)) return true;
            else if(handleFireExtinguish(worldIn, pos, state, playerIn, stack)) return true;
            else return handleItems(worldIn, pos, state, playerIn, stack, hitX, hitY, hitZ);
        }

        return false;
    }

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
        return getDefaultState().withProperty(X_AXIS, placer.getHorizontalFacing().getAxis() == EnumFacing.Axis.X);
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
        CampfireConfigHandler.getAllTypes().forEach(type -> items.add(ItemCampfire.applyType(this, type)));
    }

    @Nonnull
    @Override
    public ItemStack getItem(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        final ICampfireType type = ICampfireType.get(worldIn.getTileEntity(pos));
        return type != null ? ItemCampfire.applyType(this, type.get()) : new ItemStack(this);
    }

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        final TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileEntityCampfire)
            ((TileEntityCampfire)tile).dropAllItems();

        super.breakBlock(worldIn, pos, state);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void harvestBlock(@Nonnull World worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te, @Nonnull ItemStack stack) {
        final ICampfireType type = ICampfireType.get(te);
        if(type == null) {
            super.harvestBlock(worldIn, player, pos, state, te, stack);
            return;
        }

        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005f);

        //ensure silk touch drop captures type stored in tile entity
        if(canSilkHarvest(worldIn, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
            final List<ItemStack> drops = new ArrayList<>();
            drops.add(ItemCampfire.applyType(this, type.get()));

            ForgeEventFactory.fireBlockHarvesting(drops, worldIn, pos, state, 0, 1, true, player);
            drops.forEach(drop -> spawnAsEntity(worldIn, pos, drop));
        }

        //old code for no silk touch
        else {
            harvesters.set(player);
            final int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
            dropBlockAsItem(worldIn, pos, state, fortune);
            harvesters.set(null);
        }
    }

    //===================
    //HANDLE ITEM COOKING
    //===================

    public boolean handleItems(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull ItemStack stack, float hitX, float hitY, float hitZ) {
        //TODO
        return false;
    }

    //=========
    //COLLISION
    //=========

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        return AABB;
    }

    @Nonnull
    public List<AxisAlignedBB> getCollisionBoxList(@Nonnull IBlockState state) {
        return state.getValue(X_AXIS) ? X_AABB : Z_AABB;
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

    @Override
    public boolean addLandingEffects(@Nonnull IBlockState state, @Nonnull WorldServer worldObj, @Nonnull BlockPos blockPosition, @Nonnull IBlockState iblockstate, @Nonnull EntityLivingBase entity, int amount) {
        final ICampfireType type = ICampfireType.get(worldObj.getTileEntity(blockPosition));
        if(type != null) {
            Campfire.wrapper.sendToAllAround(
                    new MessageFallParticles(blockPosition, entity.posX, entity.posY, entity.posZ, amount),
                    new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 32));

            return true;
        }

        return super.addLandingEffects(state, worldObj, blockPosition, iblockstate, entity, amount);
    }

    @Override
    public boolean addRunningEffects(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        if(world.isRemote) {
            final ICampfireType type = ICampfireType.get(world.getTileEntity(pos));
            if(type != null) {
                final double x = entity.posX + entity.width * (world.rand.nextFloat() - 0.5);
                final double y = entity.getEntityBoundingBox().minY + 0.1;
                final double z = entity.posZ + entity.width * (world.rand.nextFloat() - 0.5);

                final ParticleManager manager = Minecraft.getMinecraft().effectRenderer;
                final Particle particle = manager.particleTypes.get(EnumParticleTypes.BLOCK_CRACK.getParticleID())
                        .createParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), world, x, y, z, -entity.motionX * 4, 1.5, -entity.motionZ * 4, 0);

                if(particle != null) {
                    final IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(type.get());
                    particle.setParticleTexture(model.getOverrides().handleItemState(model, type.get(), null, null).getParticleTexture());
                    manager.addEffect(particle);
                    return true;
                }
            }
        }

        return super.addRunningEffects(state, world, pos, entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addHitEffects(@Nonnull IBlockState state, @Nonnull World worldObj, @Nonnull RayTraceResult target, @Nonnull ParticleManager manager) {
        final BlockPos pos = target.getBlockPos();
        final ICampfireType type = ICampfireType.get(worldObj.getTileEntity(pos));
        if(type != null) {
            final double offset = 0.1;
            double x = pos.getX() + worldObj.rand.nextDouble() * (1 - offset * 2) + offset;
            double y = pos.getY() + worldObj.rand.nextDouble() * (0.4375 - offset * 2) + offset;
            double z = pos.getZ() + worldObj.rand.nextDouble() * (1 - offset * 2) + offset;
            switch(target.sideHit) {
                case UP:    y = pos.getY() + offset + 0.4375;
                    break;
                case DOWN:  y = pos.getY() - offset;
                    break;
                case NORTH: z = pos.getZ() - offset;
                    break;
                case SOUTH: z = pos.getZ() + offset + 1;
                    break;
                case WEST:  x = pos.getX() - offset;
                    break;
                case EAST:  x = pos.getX() + offset + 1;
            }

            final IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(type.get());
            final Particle particle = new ParticleDigging(worldObj, x, y, z, 0, 0, 0, Blocks.AIR.getDefaultState()).setBlockPos(pos).multiplyVelocity(0.2f).multipleParticleScaleBy(0.6f);
            particle.setParticleTexture(model.getOverrides().handleItemState(model, type.get(), null, null).getParticleTexture());
            manager.addEffect(particle);
            return true;
        }

        return super.addHitEffects(state, worldObj, target, manager);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addDestroyEffects(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ParticleManager manager) {
        final ICampfireType type = ICampfireType.get(world.getTileEntity(pos));
        if(type != null) {
            final IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(type.get());
            final TextureAtlasSprite tex = model.getOverrides().handleItemState(model, type.get(), null, null).getParticleTexture();

            final int particleCount = 64; //value must have only one true bit
            final int particlesPer = particleCount >> 4;
            for(int i = 0; i < particleCount; i++) {
                final int ix = i & particlesPer - 1;
                final int iy = i >> 2 & particlesPer - 1;
                final int iz = i >> 4 & particlesPer - 1;

                final double x = (ix + 0.5) / particlesPer;
                final double y = (iy + 0.5) / particlesPer;
                final double z = (iz + 0.5) / particlesPer;

                final Particle particle = new ParticleDigging(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, x - 0.5, y - 0.5, z - 0.5, Blocks.AIR.getDefaultState()).setBlockPos(pos);
                particle.setParticleTexture(tex);
                manager.addEffect(particle);
            }

            return true;
        }

        return super.addDestroyEffects(world, pos, manager);
    }

    //===========================
    //FLUIDLOGGED API INTEGRATION
    //===========================

    @Optional.Method(modid = "fluidlogged_api")
    @Nonnull
    @Override
    public EnumActionResult onFluidFill(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState here, @Nonnull FluidState newFluid, int blockFlags) {
        final TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityCampfire)
            ((TileEntityCampfire)tile).dropAllItems();

        return super.onFluidFill(world, pos, here, newFluid, blockFlags);
    }
}
