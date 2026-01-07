/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.block.campfire;

import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.api.recipe.campfire.CampfireRecipe;
import git.jbredwards.campfire.mod.client.particle.ParticleCampfireSmoke;
import git.jbredwards.campfire.mod.client.particle.ParticleColoredLava;
import git.jbredwards.campfire.mod.common.capability.ICampfireWoodType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.ILocation;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Random;

/**
 *
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public interface ICampfireType<V extends CampfireWoodType<V>>
{
    @Nonnull
    IForgeRegistry<V> getWoodTypes();

    /**
     * @return All the recipes that can be performed by this campfire block .
     * @since 2.0.0
     */
    @Nonnull
    IForgeRegistry<? extends CampfireRecipe<?>> getRecipes();

    @Nonnull
    default Optional<? extends CampfireRecipe<?>> getRecipe(@Nonnull final ItemStack stack, @Nonnull final ICampfireWoodType woodType) {
        return getRecipes().getValuesCollection().stream().filter(recipe -> recipe.accepts(stack, woodType)).findFirst();
    }

    @Nonnull
    Item getJunkItem(@Nonnull final ICampfireSettings settings, @Nonnull final IBlockState campfire);

    int getJunkQuantity(@Nonnull final ICampfireSettings settings, @Nonnull final IBlockState campfire, @Nonnull final Random rand);

    int getJunkMetadata(@Nonnull final ICampfireSettings settings, @Nonnull final IBlockState campfire);

    default boolean canWaterExtinguish(@Nonnull final ICampfireSettings settings, @Nonnull final IBlockState campfire) { return true; }

    default void onEntityCollision(@Nonnull final ICampfireSettings settings, @Nonnull final IBlockState campfire, @Nonnull final Entity entity) {}

    default boolean isFireEntity(@Nonnull final ICampfireSettings settings, @Nonnull final IBlockState campfire, @Nonnull final Entity entity) {
        return entity.isBurning();
    }

    default boolean isFireSignal(@Nonnull final ICampfireSettings settings, @Nonnull final IBlockState campfire, @Nonnull final IBlockSource sourceToCheck) {
        return sourceToCheck.getBlockState().getBlock() == Blocks.HAY_BLOCK;
    }

    default boolean isFireSource(@Nonnull final ICampfireSettings settings, @Nonnull final IBlockState campfire, @Nonnull final IBlockSource sourceToCheck) {
        return sourceToCheck.getBlockState().getBlock().isFireSource(sourceToCheck.getWorld(), sourceToCheck.getBlockPos(), EnumFacing.UP);
    }

    default void playCrackleSound(@Nonnull final ICampfireSettings settings, @Nonnull final ILocation location, final int fireColor, final int smokeColor, final boolean isSmokeSignal, final boolean isBlockPowered) {
        location.getWorld().playSound(location.getX(), location.getY(), location.getZ(), settings.getCrackleSound(), SoundCategory.BLOCKS, 0.5f + location.getWorld().rand.nextFloat(), location.getWorld().rand.nextFloat() * 0.7f + 0.6f, false);
    }

    @SideOnly(Side.CLIENT)
    default void spawnCookingParticle(@Nonnull final ICampfireSettings settings, @Nonnull final ILocation location, final int fireColor, final int smokeColor, final boolean isSmokeSignal, final boolean isBlockPowered) {
        location.getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, location.getX(), location.getY(), location.getZ(), 0, 5.0E-4D, 0);
    }

    @SideOnly(Side.CLIENT)
    default void spawnExtinguishParticle(@Nonnull final ICampfireSettings settings, @Nonnull final ILocation location, final int fireColor, final int smokeColor, final boolean isSmokeSignal, final boolean isBlockPowered) {
        location.getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, location.getX(), location.getY(), location.getZ(), 0, 0.005, 0);
    }

    @SideOnly(Side.CLIENT)
    default void spawnLavaParticle(@Nonnull final ICampfireSettings settings, @Nonnull final ILocation location, final int fireColor, final int smokeColor, final boolean isSmokeSignal, final boolean isBlockPowered) {
        if(fireColor == -1) location.getWorld().spawnParticle(EnumParticleTypes.LAVA, location.getX(), location.getY(), location.getZ(), 0, 0, 0);
        else ParticleColoredLava.spawnParticle(location.getWorld(), location.getX(), location.getY(), location.getZ(), fireColor);
    }

    @SideOnly(Side.CLIENT)
    default void spawnSmokeParticle(@Nonnull final ICampfireSettings settings, @Nonnull final ILocation location, final int fireColor, final int smokeColor, final boolean isSmokeSignal, final boolean isBlockPowered) {
        ParticleCampfireSmoke.spawnParticle(location.getWorld(), location.getX(), location.getY(), location.getZ(), 0, 0.07, 0, isSmokeSignal, !isBlockPowered && smokeColor != -1, smokeColor);
    }

    @Nonnull
    default BlockRenderLayer getFireLayer() { return BlockRenderLayer.CUTOUT; }
}
