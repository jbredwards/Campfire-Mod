package git.jbredwards.campfire.common.block;

import com.google.common.collect.ImmutableList;
import git.jbredwards.campfire.common.block.state.ColorProperty;
import git.jbredwards.campfire.common.block.state.ItemStackProperty;
import git.jbredwards.campfire.common.capability.ICampfireType;
import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.init.CampfireBlocks;
import git.jbredwards.campfire.common.item.ItemCampfire;
import git.jbredwards.campfire.common.message.MessageExtinguishEffects;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipe;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipeHandler;
import git.jbredwards.campfire.common.tileentity.AbstractCampfireTE;
import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import git.jbredwards.campfire.common.tileentity.slot.CampfireSlotInfo;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("deprecation")
public class BlockCampfire extends AbstractCampfire
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
        return new BlockStateContainer.Builder(this).add(X_AXIS, SIGNAL, LIT, POWERED)
                .add(ColorProperty.INSTANCE, ItemStackProperty.INSTANCE).build();
    }

    @Nonnull
    @Override
    public TileEntityCampfire createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityCampfire();
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
    public boolean isBurning(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        return CampfireConfigHandler.isCampfireBurningBlock && world.getBlockState(pos).getValue(LIT);
    }

    @Override
    public boolean canBurnOut() { return isSmokey && CampfireConfigHandler.campfireBurnOut > 0; }

    @Override
    public void burnOut(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        extinguishFire(world, pos, state, CampfireBlocks.CAMPFIRE_ASH.getDefaultState(), 0.125, true);
    }

    @Nonnull
    @Override
    protected MessageExtinguishEffects getBurnOutMessage(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull AbstractCampfireTE tile) {
        return new MessageExtinguishEffects(pos, tile.fireStrength == 1 ? 0.125 : 0.4);
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

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
                .withProperty(X_AXIS, placer.getHorizontalFacing().getAxis() == EnumFacing.Axis.X);
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
        CampfireConfigHandler.getAllTypes().forEach(type -> items.add(ItemCampfire.applyType(this, type)));
    }

    @Nonnull
    @Override
    public ItemStack getItem(@Nonnull IBlockState state, @Nullable TileEntity tile) {
        final ICampfireType type = ICampfireType.get(tile);
        final ItemStack stack = super.getItem(state, tile);

        return type != null ? ItemCampfire.applyType(stack, type.get()) : stack;
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

        final ICampfireType type = ICampfireType.get(tile);
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
                        slot.reset();
                        slot.sendToTracking();
                    }

                    return true;
                }

                //insert item into campfire
                if(!slot.isActive || stack.isEmpty()) return false;
                else if(!world.isRemote) {
                    getRecipeFromInput(stack, type.get()).ifPresent(campfireRecipe -> {
                        slot.output = campfireRecipe.output;
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

    @Nonnull
    protected java.util.Optional<CampfireRecipe> getRecipeFromInput(@Nonnull ItemStack stack, @Nonnull ItemStack type) {
        return CampfireRecipeHandler.getFromInput(stack, type);
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

    @Override
    public boolean isSmokey() { return isSmokey && CampfireConfigHandler.campfireEmitsSmoke; }

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
