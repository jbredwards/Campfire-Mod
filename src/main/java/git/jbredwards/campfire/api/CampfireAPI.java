/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api;

import git.jbredwards.campfire.api.block.campfire.ICampfireSettings;
import git.jbredwards.campfire.api.block.campfire.ICampfireType;
import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.api.recipe.campfire.CampfireRecipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * This class can only be loaded during FMLPreInit, or later.
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public final class CampfireAPI
{
    static { if(!Loader.instance().hasReachedState(LoaderState.PREINITIALIZATION)) throw new IllegalStateException("Campfire registries were loaded early."); }

    /**
     * Registry for campfire recipes pertaining to this mod's campfire.
     * Other mods adding their own campfires should create their own registries, to allow for unique recipes!
     */
    @Nonnull
    public static final ForgeRegistry<CampfireRecipe.Normal> CAMPFIRE_RECIPES = (ForgeRegistry<CampfireRecipe.Normal>)GameRegistry.findRegistry(CampfireRecipe.Normal.class);

    /**
     * Holds all log ItemStacks that can be used to make a campfire.
     */
    @Nonnull
    public static final ForgeRegistry<CampfireWoodType.Normal> WOOD_TYPES = (ForgeRegistry<CampfireWoodType.Normal>)GameRegistry.findRegistry(CampfireWoodType.Normal.class);

    @Nonnull
    public static final ICampfireType<CampfireWoodType.Normal> NORMAL_TYPE = new ICampfireType<CampfireWoodType.Normal>() {
        @Nonnull
        @Override
        public IForgeRegistry<CampfireWoodType.Normal> getWoodTypes() { return WOOD_TYPES; }

        @Nonnull
        @Override
        public IForgeRegistry<CampfireRecipe.Normal> getRecipes() { return CAMPFIRE_RECIPES; }

        // ----------------------------------
        // always drop 2 charcoal when broken
        // ----------------------------------

        @Nonnull
        @Override
        public Item getJunkItem(@Nonnull final ICampfireSettings settings, @Nonnull final IBlockState campfire) { return Items.COAL; }

        @Override
        public int getJunkQuantity(@Nonnull final ICampfireSettings settings, @Nonnull final IBlockState campfire, @Nonnull final Random rand) { return 2; }

        @Override
        public int getJunkMetadata(@Nonnull final ICampfireSettings settings, @Nonnull final IBlockState campfire) { return 1; }
    };
}
