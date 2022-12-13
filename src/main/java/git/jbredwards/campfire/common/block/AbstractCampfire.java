package git.jbredwards.campfire.common.block;

import git.jbredwards.campfire.Campfire;
import git.jbredwards.campfire.common.block.state.ColorProperty;
import git.jbredwards.campfire.common.compat.fluidlogged_api.FluidloggedAPI;
import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.init.CampfireSounds;
import git.jbredwards.campfire.common.item.ItemBlockColored;
import git.jbredwards.campfire.common.tileentity.AbstractCampfireTE;
import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Enchantments;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(modid = "campfire")
@Optional.Interface(modid = "fluidlogged_api", iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggable")
public abstract class AbstractCampfire<T extends AbstractCampfireTE> extends Block implements ITileEntityProvider, IFluidloggable
{
    @Nonnull
    public static final PropertyBool
            LIT = PropertyBool.create("lit"),
            POWERED = PropertyBool.create("powered"),
            SIGNAL = PropertyBool.create("signal");

    public final boolean isSmokey;

    public AbstractCampfire(@Nonnull Material materialIn, boolean isSmokeyIn) {
        this(materialIn, materialIn.getMaterialMapColor(), isSmokeyIn);
    }

    public AbstractCampfire(@Nonnull Material materialIn, @Nonnull MapColor mapColorIn, boolean isSmokeyIn) {
        super(materialIn, mapColorIn);
        isSmokey = isSmokeyIn;
        setDefaultState(getDefaultState().withProperty(POWERED, false).withProperty(SIGNAL, false));
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(SIGNAL, (meta & 4) != 0)
                .withProperty(LIT, (meta & 2) != 0)
                .withProperty(POWERED, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return (state.getValue(SIGNAL) ? 4 : 0)
                | (state.getValue(LIT) ? 2 : 0)
                | (state.getValue(POWERED) ? 1 : 0);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(SIGNAL, LIT, POWERED).add(ColorProperty.INSTANCE).build();
    }

    @Nonnull
    @Override
    public final T createNewTileEntity(@Nonnull World world, int meta) {
        return createTileEntity(world, getStateFromMeta(meta));
    }

    @Nonnull
    @Override
    public abstract T createTileEntity(@Nonnull World world, @Nonnull IBlockState state);

    @Override
    public int getLightValue(@Nonnull IBlockState state) { return state.getValue(LIT) ? 15 : 0; }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(isNonFluidlogged(worldIn, pos)) {
            final ItemStack stack = playerIn.getHeldItem(hand);
            if(!playerIn.canPlayerEdit(pos, facing, stack)) return false;

            if(handleFireIgnite(worldIn, pos, state, playerIn, stack)) return true;
            else return handleFireExtinguish(worldIn, pos, state, playerIn, stack);
        }

        return false;
    }

    //============
    //HANDLE ITEMS
    //============

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        final @Nullable TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof AbstractCampfireTE && ((AbstractCampfireTE)tile).color == -1)
            ((AbstractCampfireTE)tile).color = ItemBlockColored.getColor(stack);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void harvestBlock(@Nonnull World worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te, @Nonnull ItemStack stack) {
        if(!(te instanceof AbstractCampfireTE)) {
            super.harvestBlock(worldIn, player, pos, state, te, stack);
            return;
        }

        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005f);

        //ensure silk touch drop captures type stored in tile entity
        if(canSilkHarvest(worldIn, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
            final List<ItemStack> drops = new ArrayList<>();
            drops.add(ItemBlockColored.applyColor(new ItemStack(this), ((AbstractCampfireTE)te).color));

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

    //=======================
    //HANDLE POWERED PROPERTY
    //=======================

    @Override
    public void onBlockAdded(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        if(worldIn.isBlockPowered(pos)) updatePower(worldIn, pos, state, true);
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        final boolean isPowered = state.getValue(POWERED);
        if(isPowered != worldIn.isBlockPowered(pos)) updatePower(worldIn, pos, state, !isPowered);
    }

    public void updatePower(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, boolean newPower) {
        final int blockFlags = Constants.BlockFlags.SEND_TO_CLIENTS | Constants.BlockFlags.NO_OBSERVERS;
        worldIn.setBlockState(pos, state.withProperty(POWERED, newPower), blockFlags);
    }

    //===========
    //HANDLE FIRE
    //===========

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void handleProjectileCollision(@Nonnull ProjectileImpactEvent event) {
        if(!event.getEntity().world.isRemote && event.getRayTraceResult().typeOfHit == RayTraceResult.Type.BLOCK) {
            final RayTraceResult result = event.getRayTraceResult();
            final Entity entity = event.getEntity();
            //water extinguishes fire
            if(entity instanceof EntityPotion) {
                final ItemStack potion = ((EntityPotion)entity).getPotion();
                final PotionType potionType = PotionUtils.getPotionFromItem(potion);
                if(potionType == PotionTypes.WATER && PotionUtils.getEffectsFromStack(potion).isEmpty()) {
                    final BlockPos pos = result.getBlockPos();
                    final IBlockState state = entity.world.getBlockState(pos);
                    if(state.getBlock() instanceof BlockCampfire && state.getValue(LIT))
                        ((BlockCampfire)state.getBlock()).extinguishFire(entity.world, pos, state);
                }
            }
            //snowballs extinguish fire
            else if(entity instanceof EntitySnowball) {
                final AxisAlignedBB fireBB = new AxisAlignedBB(0.25, 0.0625, 0.25, 0.75, 1, 0.75);
                final BlockPos pos = result.getBlockPos();
                if(fireBB.offset(pos).contains(result.hitVec.add(new Vec3d(result.sideHit.getDirectionVec()).scale(0.1)))) {
                    final IBlockState state = entity.world.getBlockState(pos);
                    if(state.getBlock() instanceof BlockCampfire && state.getValue(LIT))
                        ((BlockCampfire)state.getBlock()).extinguishFire(entity.world, pos, state);
                }
            }
            //entities on fire ignite it
            else if(entity.isBurning()) {
                final BlockPos pos = result.getBlockPos();
                final IBlockState state = entity.world.getBlockState(pos);
                if(state.getBlock() instanceof BlockCampfire && !state.getValue(LIT))
                    if(((BlockCampfire)state.getBlock()).igniteFire(entity.world, pos, state))
                        event.setCanceled(true);
            }
        }
    }

    @Override
    public boolean isBurning(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        return CampfireConfigHandler.isBurningBlock && world.getBlockState(pos).getValue(LIT);
    }

    @Override
    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
        if(entityIn instanceof EntityPlayer && !((EntityPlayer)entityIn).isAllowEdit()) return;
        if(!worldIn.isRemote && entityIn.isBurning() && !state.getValue(LIT)) igniteFire(worldIn, pos, state);
    }

    public boolean handleFireIgnite(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull ItemStack stack) {
        if(!state.getValue(LIT)) {
            if(stack.getItem() instanceof ItemFireball) {
                if(igniteFire(worldIn, pos, state)) {
                    worldIn.playSound(playerIn, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1, (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.2f + 1);
                    if(!playerIn.isCreative()) stack.shrink(1);
                    return true;
                }
            }

            else if(stack.getItem() instanceof ItemFlintAndSteel) {
                if(igniteFire(worldIn, pos, state)) {
                    worldIn.playSound(playerIn, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, worldIn.rand.nextFloat() * 0.4f + 0.8f);
                    stack.damageItem(1, playerIn);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean handleFireExtinguish(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull ItemStack stack) {
        if(state.getValue(LIT) && stack.getItem().getToolClasses(stack).contains("shovel")) {
            extinguishFire(worldIn, pos, state);
            stack.damageItem(1, playerIn);
            return true;
        }

        return false;
    }

    public void extinguishFire(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        if(!world.isRemote) {
            world.playEvent(Constants.WorldEvents.FIRE_EXTINGUISH_SOUND, pos, 0);
            world.setBlockState(pos, state.withProperty(LIT, false));
        }
    }

    public boolean igniteFire(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        if(isNonFluidlogged(world, pos)) {
            world.setBlockState(pos, state.withProperty(LIT, true));
            return true;
        }

        return false;
    }

    //=========
    //COLLISION
    //=========

    @Override
    public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        getCollisionBoxList(state).forEach(aabb -> addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb));
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        final List<RayTraceResult> list = getCollisionBoxList(blockState).stream()
                .map(aabb -> rayTrace(pos, start, end, aabb))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if(list.isEmpty()) return null;
        RayTraceResult furthest = null;
        double dist = -1;

        for(RayTraceResult trace : list) {
            final double newDist = trace.hitVec.squareDistanceTo(end);
            if(newDist > dist) {
                furthest = trace;
                dist = newDist;
            }
        }

        return furthest;
    }

    @Nonnull
    public abstract List<AxisAlignedBB> getCollisionBoxList(@Nonnull IBlockState state);

    @Nonnull
    protected static AxisAlignedBB box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new AxisAlignedBB(minX / 16, minY / 16, minZ / 16, maxX / 16, maxY / 16, maxZ / 16);
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
        return state.getBlockFaceShape(world, pos, side) == BlockFaceShape.SOLID;
    }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    //===============
    //BLOCK RENDERING
    //===============

    @SideOnly(Side.CLIENT)
    public void addParticles(@Nonnull World world, @Nonnull BlockPos pos, int color, boolean isSignal, boolean isPowered) {

    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(@Nonnull IBlockState stateIn, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Random rand) {
        if(stateIn.getValue(LIT)) {
            //ambient sounds
            if(rand.nextInt(10) == 0) {
                worldIn.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, CampfireSounds.CRACKLE, SoundCategory.BLOCKS, 0.5f + rand.nextFloat(), rand.nextFloat() * 0.7f + 0.6f, false);
            }

            //lava particles
            if(isSmokey && rand.nextInt(5) == 0) {
                for(int i = 0; i < rand.nextInt(1) + 1; ++i) {
                    worldIn.spawnParticle(EnumParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, rand.nextFloat() / 2, 5.0E-5D, rand.nextFloat() / 2);
                }
            }
        }
    }

    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        if(state instanceof IExtendedBlockState) {
            final @Nullable TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof AbstractCampfireTE)
                return ((IExtendedBlockState)state).withProperty(ColorProperty.INSTANCE, ((AbstractCampfireTE)tile).color);
        }

        return state;
    }

    //===========================
    //FLUIDLOGGED API INTEGRATION
    //===========================

    @Optional.Method(modid = "fluidlogged_api")
    @Nonnull
    @Override
    public EnumActionResult onFluidFill(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState here, @Nonnull FluidState newFluid, int blockFlags) {
        if(here.getValue(LIT)) extinguishFire(world, pos, here);
        return EnumActionResult.PASS;
    }

    protected boolean isNonFluidlogged(@Nonnull World world, @Nonnull BlockPos pos) {
        return !Campfire.isFluidloggedAPI || !FluidloggedAPI.isFluidlogged(world, pos);
    }
}
