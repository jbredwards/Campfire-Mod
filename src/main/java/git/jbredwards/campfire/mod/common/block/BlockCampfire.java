/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.block;

import com.google.common.collect.ImmutableList;
import git.jbredwards.campfire.api.block.campfire.ICampfireSettings;
import git.jbredwards.campfire.api.block.campfire.ICampfireType;
import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.mod.Campfire;
import git.jbredwards.campfire.mod.common.block.state.CampfireWoodTypeProperty;
import git.jbredwards.campfire.mod.common.capability.ICampfireWoodType;
import git.jbredwards.campfire.mod.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.mod.common.init.CampfireBlocks;
import git.jbredwards.campfire.mod.common.item.ItemCampfire;
import git.jbredwards.campfire.mod.common.item.tab.CampfireCreativeTab;
import git.jbredwards.campfire.mod.common.message.MessageExtinguishEffects;
import git.jbredwards.campfire.api.recipe.campfire.CampfireRecipe;
import git.jbredwards.campfire.mod.common.message.MessageFallParticles;
import git.jbredwards.campfire.mod.common.tileentity.AbstractCampfireTE;
import git.jbredwards.campfire.mod.common.tileentity.TileEntityCampfire;
import git.jbredwards.campfire.mod.common.tileentity.slot.CampfireSlotInfo;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("deprecation")
public class BlockCampfire<V extends CampfireWoodType<V>> extends AbstractCampfire<V>
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

    public BlockCampfire(@Nonnull final Material materialIn, @Nonnull final ICampfireType<V> campfireTypeIn, @Nonnull final ICampfireSettings campfireSettingsIn) {
        this(materialIn, materialIn.getMaterialMapColor(), campfireTypeIn, campfireSettingsIn);
    }

    public BlockCampfire(@Nonnull final Material materialIn, @Nonnull final MapColor mapColorIn, @Nonnull final ICampfireType<V> campfireTypeIn, @Nonnull final ICampfireSettings campfireSettingsIn) {
        super(materialIn, mapColorIn, campfireTypeIn, campfireSettingsIn);
        setSoundType(SoundType.WOOD).setCreativeTab(CampfireCreativeTab.INSTANCE)
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
    protected BlockStateContainer.Builder createStateBuilder() {
        return super.createStateBuilder().add(X_AXIS).add(CampfireWoodTypeProperty.INSTANCE);
    }

    @Nonnull
    @Override
    public TileEntityCampfire createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityCampfire(campfireType);
    }

    @Nonnull
    @Override
    public EnumPushReaction getPushReaction(@Nonnull IBlockState state) { return EnumPushReaction.DESTROY; }

    //===========
    //HANDLE FIRE
    //===========

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(isNonFluidlogged(worldIn, pos)) {
            final ItemStack stack = playerIn.getHeldItem(hand);
            if(!playerIn.canPlayerEdit(pos, facing, stack)) return false;

            if(handleFireIgnite(worldIn, pos, state, playerIn, stack)) return true;
            else if(handleFireExtinguish(worldIn, pos, state, playerIn, stack)) return true;
            else return handleItems(worldIn, pos, playerIn, stack, hitX, hitY, hitZ);
        }

        return false;
    }

    @Override
    public void burnOut(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        extinguishFire(world, pos, state, CampfireConfigHandler.Campfire.campfireBurnOutAsh ? CampfireBlocks.CAMPFIRE_ASH.getDefaultState() : state.withProperty(LIT, false), 0.125, true);
    }

    @Nonnull
    @Override
    protected MessageExtinguishEffects getBurnOutMessage(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull AbstractCampfireTE tile) {
        return new MessageExtinguishEffects(pos, tile.fireStrength >= campfireSettings.getBurnOut() - 1 ? 0.125 : 0.4);
    }

    //============
    //ITEM HELPERS
    //============

    @Nonnull
    @Override
    public Item getItemDropped(@Nonnull IBlockState state, @Nonnull Random rand, int fortune) {
        return campfireType.getJunkItem(campfireSettings, state);
    }

    @Override
    public int quantityDropped(@Nonnull IBlockState state, int fortune, @Nonnull Random rand) {
        return campfireType.getJunkQuantity(campfireSettings, state, rand);
    }

    @Override
    public int damageDropped(@Nonnull IBlockState state) {
        return campfireType.getJunkMetadata(campfireSettings, state);
    }

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
                .withProperty(X_AXIS, placer.getHorizontalFacing().getAxis() == EnumFacing.Axis.X);
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
        campfireType.getWoodTypes().forEach(type -> items.add(ItemCampfire.applyType(new ItemStack(this), type)));
    }

    @Nonnull
    @Override
    public ItemStack getItem(@Nonnull IBlockState state, @Nullable TileEntity tile) {
        final ItemStack stack = super.getItem(state, tile);
        final ICampfireWoodType type = ICampfireWoodType.get(tile);

        if(type == null) return stack;
        final CampfireWoodType<?> woodType = type.get();
        return woodType != null ? ItemCampfire.applyType(stack, woodType) : stack;
    }

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        final TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileEntityCampfire)
            ((TileEntityCampfire)tile).dropAllItems();

        super.breakBlock(worldIn, pos, state);
    }

    //===================
    //HANDLE ITEM COOKING
    //===================

    public boolean handleItems(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, @Nonnull ItemStack stack, float hitX, float hitY, float hitZ) {
        final TileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof TileEntityCampfire)) return false;

        final ICampfireWoodType type = ICampfireWoodType.get(tile);
        if(type == null) return false;

        for(CampfireSlotInfo slot : ((TileEntityCampfire)tile).slotInfo) {
            if(slot.isWithin(hitX, hitY, hitZ)) {
                //pop off item in slot
                if(!slot.stack.isEmpty()) {
                    if(!world.isRemote) {
                        //calculate initial exp amount
                        if(slot.output.isEmpty()) {
                            int exp = slot.output.getCount();
                            if(slot.experience == 0) exp = 0;
                            else if(slot.experience < 1) {
                                int j = MathHelper.floor(exp * slot.experience);
                                if(j < MathHelper.ceil(exp * slot.experience) && Math.random() < (exp * slot.experience - j)) ++j;
                                exp = j;
                            }

                            //create exp orbs
                            while(exp > 0) {
                                final int expAmount = EntityXPOrb.getXPSplit(exp);
                                exp -= expAmount;
                                world.spawnEntity(new EntityXPOrb(world, player.posX, player.posY, player.posZ, expAmount));
                            }
                        }

                        ItemHandlerHelper.giveItemToPlayer(player, slot.stack);
                        slot.resetAndSendToTracking();
                    }

                    return true;
                }

                //put item onto campfire
                if(!slot.isActive || stack.isEmpty()) return false;
                final Optional<? extends CampfireRecipe<?>> recipe = campfireType.getRecipe(stack, type);
                if(!recipe.isPresent() && CampfireConfigHandler.Campfire.recipeItemsOnCampfireOnly) return false;
                if(!world.isRemote) {
                    recipe.ifPresent(campfireRecipe -> {
                        slot.output = campfireRecipe.output.copy();
                        slot.maxCookTime = campfireRecipe.cookTime;
                        slot.experience = campfireRecipe.experience;
                    });

                    slot.stack = ItemHandlerHelper.copyStackWithSize(stack, 1);
                    slot.sendToTracking();

                    if(!player.isCreative()) stack.shrink(1);
                }

                return true;
            }
        }

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

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        @Nullable final RayTraceResult trace = super.collisionRayTrace(blockState, worldIn, pos, start, end);
        if(trace == null) return null;

        @Nullable final RayTraceResult traceSide = rayTrace(pos, start, end, blockState.getBoundingBox(worldIn, pos));
        if(traceSide != null) trace.sideHit = traceSide.sideHit;
        return trace;
    }

    //=========
    //RENDERING
    //=========

    @SideOnly(Side.CLIENT)
    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || layer == campfireType.getFireLayer() && state.getValue(LIT);
    }

    //===============
    //BLOCK PARTICLES
    //===============

    @Override
    public boolean addLandingEffects(@Nonnull IBlockState state, @Nonnull WorldServer worldObj, @Nonnull BlockPos blockPosition, @Nonnull IBlockState iblockstate, @Nonnull EntityLivingBase entity, int amount) {
        if(ICampfireWoodType.get(worldObj.getTileEntity(blockPosition)) != null) {
            Campfire.WRAPPER.sendToAllAround(
                    new MessageFallParticles(blockPosition, entity.posX, entity.posY, entity.posZ, amount),
                    new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 32));

            return true;
        }

        return super.addLandingEffects(state, worldObj, blockPosition, iblockstate, entity, amount);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addRunningEffects(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        if(world.isRemote) {
            final ICampfireWoodType type = ICampfireWoodType.get(world.getTileEntity(pos));
            if(type != null && type.get() != null) {
                final double x = entity.posX + entity.width * (world.rand.nextFloat() - 0.5);
                final double y = entity.getEntityBoundingBox().minY + 0.1;
                final double z = entity.posZ + entity.width * (world.rand.nextFloat() - 0.5);

                final ParticleManager manager = Minecraft.getMinecraft().effectRenderer;
                final Particle particle = manager.particleTypes.get(EnumParticleTypes.BLOCK_CRACK.getParticleID())
                        .createParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), world, x, y, z, -entity.motionX * 4, 1.5, -entity.motionZ * 4, 0);

                if(particle != null) {
                    particle.setParticleTexture(type.get().getModel().getParticleTexture());
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
        final ICampfireWoodType type = ICampfireWoodType.get(worldObj.getTileEntity(pos));
        if(type != null && type.get() != null) {
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

            final Particle particle = new ParticleDigging(worldObj, x, y, z, 0, 0, 0, Blocks.AIR.getDefaultState()).setBlockPos(pos).multiplyVelocity(0.2f).multipleParticleScaleBy(0.6f);
            particle.setParticleTexture(type.get().getModel().getParticleTexture());
            manager.addEffect(particle);
            return true;
        }

        return super.addHitEffects(state, worldObj, target, manager);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addDestroyEffects(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ParticleManager manager) {
        final TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof AbstractCampfireTE && ((AbstractCampfireTE)tile).isLit()) {
            playExtinguishEffects((AbstractCampfireTE)tile, 0.4);
            world.playEvent(Constants.WorldEvents.FIRE_EXTINGUISH_SOUND, pos, 0);
        }

        final ICampfireWoodType type = ICampfireWoodType.get(tile);
        if(type != null && type.get() != null) {
            final TextureAtlasSprite tex = type.get().getModel().getParticleTexture();

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

    @net.minecraftforge.fml.common.Optional.Method(modid = "fluidlogged_api")
    @Nonnull
    @Override
    public EnumActionResult onFluidFill(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState here, @Nonnull FluidState newFluid, int blockFlags) {
        final TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityCampfire)
            ((TileEntityCampfire)tile).dropAllItems();

        return super.onFluidFill(world, pos, here, newFluid, blockFlags);
    }
}
