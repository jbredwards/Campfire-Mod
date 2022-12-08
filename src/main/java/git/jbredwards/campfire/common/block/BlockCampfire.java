package git.jbredwards.campfire.common.block;

import git.jbredwards.campfire.common.block.state.ItemStackProperty;
import git.jbredwards.campfire.common.capability.ICampfireType;
import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.item.ItemCampfire;
import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
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
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.*;
import net.minecraft.item.Item;
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
import net.minecraft.world.WorldServer;
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
import java.util.*;
import java.util.stream.Collectors;

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
    @Nonnull public static final PropertyBool LIT = PropertyBool.create("lit"), POWERED = PropertyBool.create("powered");
    @Nonnull public static final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 1, 0.4375, 1);

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

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        final ItemStack stack = playerIn.getHeldItem(hand);

        if(handleItems(worldIn, pos, state, playerIn, stack, hitX, hitY, hitZ, true)) return true;
        else if(handleFireIgnite(worldIn, pos, state, playerIn, stack)) return true;
        else if(handleFireExtinguish(worldIn, pos, state, playerIn, stack)) return true;
        else return handleItems(worldIn, pos, state, playerIn, stack, hitX, hitY, hitZ, false);
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
        return placer.isSneaking() ? getDefaultState().withProperty(FACING, placer.getHorizontalFacing()) : getDefaultState();
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

    public boolean handleItems(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull ItemStack stack, float hitX, float hitY, float hitZ, boolean checkRecipe) {
        //TODO
        return false;
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
                    if(state.getBlock() instanceof BlockCampfire && state.getValue(LIT)) {
                        ((BlockCampfire)state.getBlock()).extinguishFire(entity.world, pos, state);
                    }
                }
            }
            //snowballs extinguish fire
            else if(entity instanceof EntitySnowball) {
                final AxisAlignedBB fireBB = new AxisAlignedBB(0.25, 0.0625, 0.25, 0.75, 1, 0.75);
                final BlockPos pos = result.getBlockPos();
                if(fireBB.offset(pos).contains(result.hitVec.add(new Vec3d(result.sideHit.getDirectionVec()).scale(0.1)))) {
                    final IBlockState state = entity.world.getBlockState(pos);
                    if(state.getBlock() instanceof BlockCampfire && state.getValue(LIT)) {
                        ((BlockCampfire)state.getBlock()).extinguishFire(entity.world, pos, state);
                    }
                }
            }
            //entities on fire ignite it
            else if(entity.isBurning()) {
                final BlockPos pos = result.getBlockPos();
                final IBlockState state = entity.world.getBlockState(pos);
                if(state.getBlock() instanceof BlockCampfire && !state.getValue(LIT)) {
                    entity.world.setBlockState(pos, state.withProperty(LIT, true));
                    event.setCanceled(true);
                }
            }
        }
    }

    @Override
    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
        if(!worldIn.isRemote && entityIn.isBurning() && !state.getValue(LIT))
            worldIn.setBlockState(pos, state.withProperty(LIT, true));
    }

    public boolean handleFireIgnite(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull ItemStack stack) {
        if(!state.getValue(LIT)) {
            if(stack.getItem() instanceof ItemFireball) {
                worldIn.playSound(playerIn, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1, (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.2f + 1);
                worldIn.setBlockState(pos, state.withProperty(LIT, true));
                if(!playerIn.isCreative()) stack.shrink(1);
                return true;
            }

            else if(stack.getItem() instanceof ItemFlintAndSteel) {
                worldIn.playSound(playerIn, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, worldIn.rand.nextFloat() * 0.4f + 0.8f);
                worldIn.setBlockState(pos, state.withProperty(LIT, true));
                stack.damageItem(1, playerIn);
                return true;
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

    //=========
    //COLLISION
    //=========

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        return AABB;
    }

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
    public List<AxisAlignedBB> getCollisionBoxList(@Nonnull IBlockState state) {
        return Collections.singletonList(AABB);
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

    @Override
    public boolean addLandingEffects(@Nonnull IBlockState state, @Nonnull WorldServer worldObj, @Nonnull BlockPos blockPosition, @Nonnull IBlockState iblockstate, @Nonnull EntityLivingBase entity, int numberOfParticles) {
        return super.addLandingEffects(state, worldObj, blockPosition, iblockstate, entity, numberOfParticles);
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
        extinguishFire(world, pos, here);
        return EnumActionResult.PASS;
    }
}
