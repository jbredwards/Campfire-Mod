package git.jbredwards.campfire.common.block;

import git.jbredwards.campfire.Campfire;
import git.jbredwards.campfire.client.particle.ParticleCampfireSmoke;
import git.jbredwards.campfire.client.particle.ParticleColoredLava;
import git.jbredwards.campfire.common.block.state.ColorProperty;
import git.jbredwards.campfire.common.block.state.ItemStackProperty;
import git.jbredwards.campfire.common.capability.ICampfireType;
import git.jbredwards.campfire.common.compat.fluidlogged_api.FluidloggedAPI;
import git.jbredwards.campfire.common.compat.futuremc.IBeeCalmer;
import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.init.CampfireSounds;
import git.jbredwards.campfire.common.item.ItemBlockColored;
import git.jbredwards.campfire.common.message.MessageExtinguishEffects;
import git.jbredwards.campfire.common.message.MessageFallParticles;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.server.management.PlayerChunkMapEntry;
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
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
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
public abstract class AbstractCampfire extends Block implements ITileEntityProvider, IFluidloggable, IBeeCalmer
{
    @Nonnull
    public static final PropertyBool
            LIT = PropertyBool.create("lit"),
            POWERED = PropertyBool.create("powered"),
            SIGNAL = PropertyBool.create("signal");

    protected final boolean isSmokey;
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
    public final AbstractCampfireTE createNewTileEntity(@Nonnull World world, int meta) {
        return createTileEntity(world, getStateFromMeta(meta));
    }

    @Nonnull
    @Override
    public abstract AbstractCampfireTE createTileEntity(@Nonnull World world, @Nonnull IBlockState state);

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
    protected boolean canSilkHarvest() { return true; }

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
        return meta == 1 ? getDefaultState().withProperty(LIT, false) : getDefaultState();
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        final @Nullable TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof AbstractCampfireTE) ((AbstractCampfireTE)tile).color = ItemBlockColored.getColor(stack);

        final ICampfireType stackCap = ICampfireType.get(stack);
        if(stackCap != null) {
            final ICampfireType tileCap = ICampfireType.get(tile);
            if(tileCap != null) tileCap.set(stackCap.get());
        }
    }

    @Nonnull
    @Override
    public ItemStack getItem(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        final @Nullable TileEntity tile = worldIn.getTileEntity(pos);
        final ItemStack stack = new ItemStack(this, 1, state.getValue(LIT) ? 0 : 1);

        return tile instanceof AbstractCampfireTE ? ItemBlockColored.applyColor(stack, AbstractCampfireTE.getColor(tile)) : stack;
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
            drops.add(ItemBlockColored.applyColor(new ItemStack(this, 1, state.getValue(LIT) ? 0 : 1), AbstractCampfireTE.getColor(te)));
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

    //==================================
    //HANDLE POWERED & SIGNAL PROPERTIES
    //==================================

    @Override
    public void onBlockAdded(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        if(worldIn.getBlockState(pos.down()).getBlock() == Blocks.HAY_BLOCK) updateSignal(worldIn, pos, state, true);
        if(worldIn.isBlockPowered(pos)) updatePower(worldIn, pos, state, true);
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        if(pos.down().equals(fromPos)) {
            final boolean isSignal = state.getValue(SIGNAL);
            if(isSignal != (worldIn.getBlockState(fromPos).getBlock() == Blocks.HAY_BLOCK))
                updateSignal(worldIn, pos, state, !isSignal);
        }

        final boolean isPowered = state.getValue(POWERED);
        if(isPowered != worldIn.isBlockPowered(pos)) updatePower(worldIn, pos, state, !isPowered);
    }

    public void updateSignal(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, boolean newSignal) {
        final int blockFlags = Constants.BlockFlags.SEND_TO_CLIENTS | Constants.BlockFlags.NO_OBSERVERS;
        worldIn.setBlockState(pos, state.withProperty(SIGNAL, newSignal), blockFlags);
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
        if(event.getRayTraceResult().typeOfHit == RayTraceResult.Type.BLOCK) {
            final RayTraceResult result = event.getRayTraceResult();
            final Entity entity = event.getEntity();
            //water extinguishes fire
            if(entity instanceof EntityPotion) {
                final ItemStack potion = ((EntityPotion)entity).getPotion();
                final PotionType potionType = PotionUtils.getPotionFromItem(potion);
                if(potionType == PotionTypes.WATER && PotionUtils.getEffectsFromStack(potion).isEmpty()) {
                    final BlockPos pos = result.getBlockPos();
                    final IBlockState state = entity.world.getBlockState(pos);
                    if(state.getBlock() instanceof AbstractCampfire && state.getValue(LIT))
                        ((AbstractCampfire)state.getBlock()).extinguishFire(entity.world, pos, state, true);
                }
            }
            //snowballs extinguish fire
            else if(entity instanceof EntitySnowball) {
                final BlockPos pos = result.getBlockPos();
                final IBlockState state = entity.world.getBlockState(pos);
                if(state.getBlock() instanceof AbstractCampfire && state.getValue(LIT))
                    ((AbstractCampfire)state.getBlock()).extinguishFire(entity.world, pos, state, true);
            }
            //entities on fire ignite it
            else if(entity.isBurning()) {
                final BlockPos pos = result.getBlockPos();
                final IBlockState state = entity.world.getBlockState(pos);
                if(state.getBlock() instanceof AbstractCampfire && !state.getValue(LIT))
                    ((AbstractCampfire)state.getBlock()).igniteFire(entity.world, pos, state);
            }
        }
    }

    @Override
    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
        if(entityIn instanceof EntityPlayer && !((EntityPlayer)entityIn).isAllowEdit()) return;
        if(!worldIn.isRemote && entityIn.posY < pos.getY() + 0.5 && entityIn.isBurning() && !state.getValue(LIT))
            igniteFire(worldIn, pos, state);
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
            if(world.isRemote) playExtinguishEffects(world, pos, (AbstractCampfireTE)tile, extraSmokeOffset);
            if(CampfireConfigHandler.resetDyeOnExtinguish) ((AbstractCampfireTE)tile).color = -1;
        }

        world.setBlockState(pos, extinguishedState);
        if(playSound && !world.isRemote) world.playEvent(Constants.WorldEvents.FIRE_EXTINGUISH_SOUND, pos, 0);
    }

    public boolean igniteFire(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        if(isNonFluidlogged(world, pos)) {
            world.setBlockState(pos, state.withProperty(LIT, true));
            if(canBurnOut()) { //reset fire strength
                final TileEntity tile = world.getTileEntity(pos);
                if(tile instanceof AbstractCampfireTE) ((AbstractCampfireTE)tile).resetFireStrength();
            }

            return true;
        }

        return false;
    }

    public abstract boolean canBurnOut();
    public boolean canRainExtinguish(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        return world.isRainingAt(pos.up());
    }

    public void burnOut(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        extinguishFire(world, pos, state, true);
    }

    @Override
    public boolean getTickRandomly() { return canBurnOut(); }

    @Override
    public void randomTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
        if(getTickRandomly() && state.getValue(LIT) && worldIn.getGameRules().getBoolean("doFireTick")) {
            if(canRainExtinguish(worldIn, pos, state) && random.nextFloat() < 0.25) extinguishFire(worldIn, pos, state, false);
            else if(random.nextFloat() < 0.25) {
                final TileEntity tile = worldIn.getTileEntity(pos);
                if(tile instanceof AbstractCampfireTE) {
                    final AbstractCampfireTE campfire = (AbstractCampfireTE)tile;
                    if(campfire.fireStrength > 0) {
                        //send client-side burn out particles
                        if(worldIn instanceof WorldServer) {
                            final PlayerChunkMapEntry entry = ((WorldServer)worldIn).getPlayerChunkMap()
                                    .getEntry(pos.getX() >> 4, pos.getZ() >> 4);

                            if(entry != null) {
                                final MessageExtinguishEffects message = getBurnOutMessage(worldIn, pos, campfire);
                                entry.getWatchingPlayers().forEach(player -> Campfire.WRAPPER.sendTo(message, player));
                            }
                        }

                        //handle campfire burn out
                        worldIn.updateObservingBlocksAt(pos, this);
                        if(--campfire.fireStrength == 0) burnOut(worldIn, pos, state);
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

    public boolean isSmokey() { return isSmokey; }

    @SideOnly(Side.CLIENT)
    public void addParticles(@Nonnull World world, @Nonnull BlockPos pos, int smokeColor, int fallbackColor, boolean forceCampfireParticles, boolean isSignal, boolean isPowered, double extraSmokeOffset) {
        if(isSmokey() || forceCampfireParticles) {
            //campfire smoke
            if(!isPowered || CampfireConfigHandler.poweredAction != CampfireConfigHandler.PoweredAction.DISABLE) {
                final double x = pos.getX() + 0.5 + world.rand.nextDouble() / 3 * (world.rand.nextBoolean() ? 1 : -1);
                final double y = pos.getY() + world.rand.nextDouble() + world.rand.nextDouble();
                final double z = pos.getZ() + 0.5 + world.rand.nextDouble() / 3 * (world.rand.nextBoolean() ? 1 : -1);

                final int color = isPowered ? smokeColor : fallbackColor;
                ParticleCampfireSmoke.spawnParticle(world, x, y, z, 0, 0.07, 0, isSignal, !isPowered && color != -1, color);
            }

            //extinguish smoke
            if(extraSmokeOffset >= 0 && isSmokey()) {
                final double x = pos.getX() + 0.25 + world.rand.nextDouble() / 2;
                final double y = pos.getY() + extraSmokeOffset;
                final double z = pos.getZ() + 0.25 + world.rand.nextDouble() / 2;
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0, 0.005, 0);
            }
        }
    }

    @Nonnull
    protected MessageExtinguishEffects getBurnOutMessage(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull AbstractCampfireTE tile) {
        return new MessageExtinguishEffects(pos, 0.4);
    }

    @SideOnly(Side.CLIENT)
    public void playExtinguishEffects(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull AbstractCampfireTE tile, double extraSmokeOffset) {
        final int forcedSmokeColor = tile.forcedSmokeColor;
        if(isSmokey() || forcedSmokeColor != -1) {
            final int smokeColor = tile.getSmokeColor();
            final int fallbackColor = tile.getFallbackColor();

            final boolean isSignal = tile.isSignal();
            final boolean isPowered = tile.isPowered();

            for(int i = 0; i < 20; i++)
                addParticles(world, pos, smokeColor, fallbackColor, forcedSmokeColor != -1, isSignal, isPowered, extraSmokeOffset);
        }
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
            if(rand.nextInt(5) == 0) {
                final int color = AbstractCampfireTE.getColor(worldIn.getTileEntity(pos));
                for(int i = 0; i < rand.nextInt(1) + 1; ++i) {
                    if(color == -1) worldIn.spawnParticle(EnumParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
                    else ParticleColoredLava.spawnParticle(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, color);
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

            final @Nullable ICampfireType type = ICampfireType.get(tile);
            if(type != null) return ((IExtendedBlockState)state).withProperty(ItemStackProperty.INSTANCE, type.get());
        }

        return state;
    }

    //fix specifically for better foliage, WHY DOES IT RENDER CUTOUT_MIPPED FOR EVERY BLOCK IN THE GAME LMAO
    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT_MIPPED
                ? EnumBlockRenderType.INVISIBLE : EnumBlockRenderType.MODEL;
    }

    //===============
    //BLOCK PARTICLES
    //===============

    @Override
    public boolean addLandingEffects(@Nonnull IBlockState state, @Nonnull WorldServer worldObj, @Nonnull BlockPos blockPosition, @Nonnull IBlockState iblockstate, @Nonnull EntityLivingBase entity, int amount) {
        if(ICampfireType.get(worldObj.getTileEntity(blockPosition)) != null) {
            Campfire.WRAPPER.sendToAllAround(
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
        final TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof AbstractCampfireTE && ((AbstractCampfireTE)tile).isLit())
            world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,
                    0.5f, 2.6f + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8f, false);

        final ICampfireType type = ICampfireType.get(tile);
        if(type != null) {
            final IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(type.get());
            final TextureAtlasSprite tex = model.getOverrides().handleItemState(model, type.get(), null, null).getParticleTexture();

            final int particleCount = 0b1000000; //64 (value must have only one true bit)
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
        if(here.getValue(LIT)) extinguishFire(world, pos, here, true);
        return EnumActionResult.PASS;
    }

    protected boolean isNonFluidlogged(@Nonnull World world, @Nonnull BlockPos pos) {
        return !Campfire.isFluidloggedAPI || !FluidloggedAPI.isFluidlogged(world, pos);
    }
}
