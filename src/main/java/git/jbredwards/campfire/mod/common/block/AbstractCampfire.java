/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.block;

import git.jbredwards.campfire.api.block.IHasWorldState;
import git.jbredwards.campfire.api.block.campfire.ICampfireSettings;
import git.jbredwards.campfire.api.block.campfire.ICampfireType;
import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.mod.Campfire;
import git.jbredwards.campfire.mod.common.block.state.ColorProperty;
import git.jbredwards.campfire.mod.common.block.state.CampfireWoodTypeProperty;
import git.jbredwards.campfire.mod.common.capability.ICampfireWoodType;
import git.jbredwards.campfire.mod.common.compat.fluidlogged_api.FluidloggedAPI;
import git.jbredwards.campfire.api.block.IBeeCalmer;
import git.jbredwards.campfire.mod.common.item.ItemBlockColored;
import git.jbredwards.campfire.mod.common.message.MessageExtinguishEffects;
import git.jbredwards.campfire.mod.common.message.MessageFallParticles;
import git.jbredwards.campfire.mod.common.tileentity.AbstractCampfireTE;
import git.jbredwards.campfire.mod.common.block.util.LocationImpl;
import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSourceImpl;
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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public abstract class AbstractCampfire<V extends CampfireWoodType<V>> extends BlockColorEmitting implements IFluidloggable, IBeeCalmer, IHasWorldState
{
    @Nonnull public static final PropertyBool LIT = PropertyBool.create("lit"), POWERED = PropertyBool.create("powered"), SIGNAL = PropertyBool.create("signal");

    @Nonnull public final ICampfireType<V> campfireType;
    @Nonnull public final ICampfireSettings campfireSettings;

    public AbstractCampfire(@Nonnull final Material materialIn, @Nonnull final ICampfireType<V> campfireTypeIn, @Nonnull final ICampfireSettings campfireSettingsIn) {
        this(materialIn, materialIn.getMaterialMapColor(), campfireTypeIn, campfireSettingsIn);
    }

    public AbstractCampfire(@Nonnull final Material materialIn, @Nonnull final MapColor mapColorIn, @Nonnull final ICampfireType<V> campfireTypeIn, @Nonnull final ICampfireSettings campfireSettingsIn) {
        super(materialIn, mapColorIn);
        campfireType = campfireTypeIn;
        campfireSettings = campfireSettingsIn;

        setTickRandomly(true);
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
    protected BlockStateContainer.Builder createStateBuilder() {
        return super.createStateBuilder().add(SIGNAL, LIT, POWERED);
    }

    @Nonnull
    @Override
    public abstract AbstractCampfireTE createTileEntity(@Nonnull World world, @Nonnull IBlockState state);

    @Override
    public int getLightValue(@Nonnull IBlockState state) { return state.getValue(LIT) ? lightValue : 0; }

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

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
        return meta == 1 ? getDefaultState().withProperty(LIT, false) : getDefaultState();
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        final @Nullable TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof AbstractCampfireTE) ((AbstractCampfireTE)tile).color = ItemBlockColored.getColor(stack);

        final ICampfireWoodType stackCap = ICampfireWoodType.get(stack);
        if(stackCap != null) {
            final ICampfireWoodType tileCap = ICampfireWoodType.get(tile);
            if(tileCap != null) tileCap.set(stackCap.get());
        }
    }

    @Override
    protected boolean canSilkHarvest() { return true; }

    @Nonnull
    @Override
    protected ItemStack getSilkTouchDrop(@Nonnull IBlockState state) {
        return new ItemStack(this, 1, !campfireSettings.unlitOnCraft() && state.getValue(LIT) ? 0 : 1); // apply color and type through AbstractCampfire#getItem
    }

    //==================================
    //HANDLE POWERED & SIGNAL PROPERTIES
    //==================================

    @Nonnull
    @Override
    public IBlockState getStateForWorld(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        return (state = state.withProperty(POWERED, world.isBlockPowered(pos))).withProperty(SIGNAL, campfireType.isFireSignal(campfireSettings, state, new BlockSourceImpl(world, pos.down())));
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        // handled by IHasWorldState, the world::setBlockState call will proceed if the stateForWorld is different from the state currently in the world
        worldIn.setBlockState(pos, state, Constants.BlockFlags.SEND_TO_CLIENTS | Constants.BlockFlags.NO_OBSERVERS);
    }

    //===========
    //HANDLE FIRE
    //===========

    protected static boolean isIllegiblePlayer(@Nullable final Entity entity) {
        return !(entity instanceof EntityPlayer) || ((EntityPlayer)entity).isAllowEdit();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void handleProjectileCollision(@Nonnull final ProjectileImpactEvent event) {
        if(event.getRayTraceResult().typeOfHit == RayTraceResult.Type.BLOCK) {
            @Nonnull final RayTraceResult result = event.getRayTraceResult();
            @Nonnull final Entity entity = event.getEntity();

            @Nonnull final BlockPos pos = result.getBlockPos();
            @Nonnull final IBlockState state = entity.world.getBlockState(pos);

            if(state.getBlock() instanceof AbstractCampfire) {
                @Nonnull final AbstractCampfire block = (AbstractCampfire)state.getBlock();

                if(block.campfireType.canWaterExtinguish(block.campfireSettings, state)) {
                    // water extinguishes fire
                    if(entity instanceof EntityPotion) {
                        @Nonnull final ItemStack potion = ((EntityPotion)entity).getPotion();
                        @Nonnull final PotionType potionType = PotionUtils.getPotionFromItem(potion);

                        if(potionType == PotionTypes.WATER && PotionUtils.getEffectsFromStack(potion).isEmpty()
                        && state.getValue(LIT) && isIllegiblePlayer(((EntityThrowable)entity).getThrower())) {
                            block.extinguishFire(entity.world, pos, state, true);
                        }
                    }

                    // snowballs extinguish fire
                    else if(entity instanceof EntitySnowball && state.getValue(LIT) && isIllegiblePlayer(((EntityThrowable)entity).getThrower())) {
                        block.extinguishFire(entity.world, pos, state, true);
                    }
                }

                // entities on fire ignite it
                if(block.campfireType.isFireEntity(block.campfireSettings, state, entity) && !state.getValue(LIT)) {
                    if(entity instanceof EntityThrowable && !isIllegiblePlayer(((EntityThrowable)entity).getThrower())) return;
                    else if(entity instanceof EntityArrow && !isIllegiblePlayer(((EntityArrow)entity).shootingEntity)) return;

                    block.igniteFire(entity.world, pos, state);
                }
            }
        }
    }

    @Override
    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
        if(!worldIn.isRemote) {
            if(state.getValue(LIT)) campfireType.onEntityCollision(campfireSettings, state, entityIn);
            else if(entityIn.posY < pos.getY() + 0.5 && isIllegiblePlayer(entityIn) && campfireType.isFireEntity(campfireSettings, state, entityIn)) igniteFire(worldIn, pos, state);
        }
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
            extinguishFire(worldIn, pos, state, true);
            stack.damageItem(1, playerIn);
            return true;
        }

        return false;
    }

    public void extinguishFire(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, boolean playSound) {
        extinguishFire(world, pos, state, state.withProperty(LIT, false), 0.4, playSound);
    }

    public void extinguishFire(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull IBlockState extinguishedState, double extraSmokeOffset, boolean playSound) {
        final @Nullable TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof AbstractCampfireTE) {
            if(world.isRemote) playExtinguishEffects((AbstractCampfireTE)tile, extraSmokeOffset);
            if(campfireSettings.resetDyeOnExtinguish()) ((AbstractCampfireTE)tile).color = -1;
        }

        world.setBlockState(pos, extinguishedState);
        if(playSound && !world.isRemote) world.playEvent(Constants.WorldEvents.FIRE_EXTINGUISH_SOUND, pos, 0);
    }

    public boolean igniteFire(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        if(isNonFluidlogged(world, pos)) {
            world.setBlockState(pos, state.withProperty(LIT, true));
            if(campfireSettings.getBurnOut() > 0) { // reset fire strength
                final TileEntity tile = world.getTileEntity(pos);
                if(tile instanceof AbstractCampfireTE) ((AbstractCampfireTE)tile).fireStrength = 0;
            }

            return true;
        }

        return false;
    }

    public boolean isFireSourceBelow(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState campfire) {
        return campfireType.isFireSource(campfireSettings, campfire, new BlockSourceImpl(world, pos.down()));
    }

    public void burnOut(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        extinguishFire(world, pos, state, true);
    }

    @Override
    public boolean isBurning(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        return campfireSettings.getBurnsEntities() && world.getBlockState(pos).getValue(LIT);
    }

    @Override
    public void randomTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
        if(campfireSettings.getBurnOut() > 0 && state.getValue(LIT) && worldIn.getGameRules().getBoolean("doFireTick") && !isFireSourceBelow(worldIn, pos, state)) {
            if(campfireType.canWaterExtinguish(campfireSettings, state) && worldIn.isRainingAt(pos.up()) && random.nextFloat() < 0.25) extinguishFire(worldIn, pos, state, false);
            else {
                final TileEntity tile = worldIn.getTileEntity(pos);
                if(tile instanceof AbstractCampfireTE) {
                    final AbstractCampfireTE campfire = (AbstractCampfireTE)tile;
                    // compat with old v1 worlds
                    if(campfire.isLegacyFireStrength) {
                        campfire.fireStrength = campfireSettings.getBurnOut() - campfire.fireStrength;
                        campfire.isLegacyFireStrength = false;
                    }

                    if(campfireSettings.getBurnOut() > 0 && campfire.fireStrength > -1) {
                        // send client-side burn out particles
                        if(worldIn instanceof WorldServer) {
                            final PlayerChunkMapEntry entry = ((WorldServer)worldIn).getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
                            if(entry != null) {
                                final MessageExtinguishEffects message = getBurnOutMessage(worldIn, pos, campfire);
                                entry.getWatchingPlayers().forEach(player -> Campfire.WRAPPER.sendTo(message, player));
                            }
                        }

                        // handle campfire burn out
                        worldIn.updateObservingBlocksAt(pos, this);
                        if(campfire.fireStrength ++>= campfireSettings.getBurnOut()) burnOut(worldIn, pos, state);
                    }
                }
            }
        }
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

    public boolean isSmokey() { return campfireSettings.getEmitsSmoke(); }

    @SideOnly(Side.CLIENT)
    public void addParticles(@Nonnull final AbstractCampfireTE tile, int smokeColor, int fallbackColor, boolean forceCampfireParticles, boolean isSignal, boolean isPowered, double extraSmokeOffset) {
        if((isSmokey() || forceCampfireParticles)) {
            // fix issue#7 (smoke particles can leak into other dimensions)
            final EntityPlayer player = Minecraft.getMinecraft().player;
            if(player != null && player.world == tile.getWorld()) {
                @Nonnull final World world = tile.getWorld();
                @Nonnull final BlockPos pos = tile.getPos();

                // campfire smoke
                if((!isPowered || campfireSettings.getPoweredAction() != ICampfireSettings.PoweredAction.DISABLE)
                && !(this instanceof BlockBrazier && (tile.getBlockMetadata() & 8) != 0)) { // prevent hanging braziers from emitting big ugly particles
                    final double x = pos.getX() + 0.5 + world.rand.nextDouble() / 3 * (world.rand.nextBoolean() ? 1 : -1);
                    final double y = pos.getY() + world.rand.nextDouble() + world.rand.nextDouble();
                    final double z = pos.getZ() + 0.5 + world.rand.nextDouble() / 3 * (world.rand.nextBoolean() ? 1 : -1);

                    final int color = isPowered ? smokeColor : fallbackColor;
                    campfireType.spawnSmokeParticle(campfireSettings, new LocationImpl(world, x, y, z), fallbackColor, color, isSignal, isPowered);
                }

                // extinguish smoke
                if(extraSmokeOffset >= 0 && isSmokey()) {
                    final double x = pos.getX() + 0.25 + world.rand.nextDouble() / 2;
                    final double y = pos.getY() + extraSmokeOffset;
                    final double z = pos.getZ() + 0.25 + world.rand.nextDouble() / 2;

                    final int color = isPowered ? smokeColor : fallbackColor;
                    campfireType.spawnExtinguishParticle(campfireSettings, new LocationImpl(world, x, y, z), fallbackColor, color, isSignal, isPowered);
                }
            }
        }
    }

    @Nonnull
    protected MessageExtinguishEffects getBurnOutMessage(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull AbstractCampfireTE tile) {
        return new MessageExtinguishEffects(pos, 0.4);
    }

    @SideOnly(Side.CLIENT)
    public void playExtinguishEffects(@Nonnull AbstractCampfireTE tile, double extraSmokeOffset) {
        final int forcedSmokeColor = tile.forcedSmokeColor;
        if(isSmokey() || forcedSmokeColor != -1) {
            final int smokeColor = tile.getSmokeColor();
            final int fallbackColor = tile.getFallbackColor();

            final boolean isSignal = tile.isSignal();
            final boolean isPowered = tile.isPowered();

            for(int i = 0; i < 20; i++) addParticles(tile, smokeColor, fallbackColor, forcedSmokeColor != -1, isSignal, isPowered, extraSmokeOffset);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(@Nonnull IBlockState stateIn, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Random rand) {
        if(stateIn.getValue(LIT)) {
            @Nullable final TileEntity te = worldIn.getTileEntity(pos);
            if(te instanceof AbstractCampfireTE) {
                @Nonnull final AbstractCampfireTE tile = (AbstractCampfireTE)te;

                // ambient sounds
                if(rand.nextInt(10) == 0) {
                    final int fireColor = tile.getFallbackColor();
                    final int smokeColor = tile.isPowered() ? tile.getSmokeColor() : fireColor;

                    campfireType.playCrackleSound(campfireSettings, new BlockSourceImpl(worldIn, pos), fireColor, smokeColor, tile.isSignal(), tile.isPowered());
                }

                // lava particles
                if(rand.nextInt(5) == 0) {
                    final int fireColor = tile.getFallbackColor();
                    final int smokeColor = tile.isPowered() ? tile.getSmokeColor() : fireColor;

                    for(int i = 0; i < rand.nextInt(1) + 1; i++) campfireType.spawnLavaParticle(campfireSettings, new BlockSourceImpl(worldIn, pos), fireColor, smokeColor, tile.isSignal(), tile.isPowered());
                }
            }
        }
    }

    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        if(state instanceof IExtendedBlockState) {
            final @Nullable TileEntity tile = world.getTileEntity(pos);
            state = ((IExtendedBlockState)state).withProperty(ColorProperty.INSTANCE, AbstractCampfireTE.getColor(tile));

            final @Nullable ICampfireWoodType type = ICampfireWoodType.get(tile);
            if(type != null) return ((IExtendedBlockState)state).withProperty(CampfireWoodTypeProperty.INSTANCE, type.get());
        }

        return state;
    }

    // fix specifically for better foliage, WHY DOES IT RENDER CUTOUT_MIPPED FOR EVERY BLOCK IN THE GAME?
    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT_MIPPED ? EnumBlockRenderType.INVISIBLE : EnumBlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public float[] getBeaconColorMultiplier(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockPos beaconPos) {
        return state.getValue(LIT) ? super.getBeaconColorMultiplier(state, world, pos, beaconPos) : null;
    }

    //===========================
    //FLUIDLOGGED API INTEGRATION
    //===========================

    @Optional.Method(modid = "fluidlogged_api")
    @Nonnull
    @Override
    public EnumActionResult onFluidFill(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState here, @Nonnull FluidState newFluid, int blockFlags) {
        if(here.getValue(LIT)) extinguishFire(world, pos, here, true);
        return EnumActionResult.PASS;
    }

    protected static boolean isNonFluidlogged(@Nonnull World world, @Nonnull BlockPos pos) {
        return !Campfire.isFluidloggedAPI || !FluidloggedAPI.isFluidlogged(world, pos);
    }

    //================
    //FUTURE MC COMPAT
    //================

    @Override
    public boolean canCalmBeeHive(@Nonnull IBlockState state) {
        return isSmokey() && state.getValue(LIT) && !(state.getValue(POWERED) && campfireSettings.getPoweredAction() == ICampfireSettings.PoweredAction.DISABLE);
    }
}
