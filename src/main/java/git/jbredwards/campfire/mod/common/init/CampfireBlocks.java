/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.init;

import git.jbredwards.campfire.api.CampfireAPI;
import git.jbredwards.campfire.api.block.campfire.ICampfireSettings;
import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.api.registry.RegistryContainer;
import git.jbredwards.campfire.mod.common.block.*;
import git.jbredwards.campfire.mod.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.mod.common.tileentity.TileEntityBrazier;
import git.jbredwards.campfire.mod.common.tileentity.TileEntityCampfire;
import git.jbredwards.campfire.mod.common.tileentity.TileEntityColorEmitting;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@RegistryContainer(Block.class)
public final class CampfireBlocks
{
    // =================
    // CAMPFIRE SETTINGS
    // =================

    @Nonnull
    public static final ICampfireSettings BRAZIER_SETTINGS = new ICampfireSettings() {
        @Nonnull
        @Override
        public SoundEvent getCrackleSound() { return CampfireSoundEvents.BRAZIER_CRACKLE; }
        @Override
        public boolean getBurnsEntities() { return CampfireConfigHandler.Brazier.isBrazierBurningBlock; }
        @Override
        public boolean getEmitsSmoke() { return CampfireConfigHandler.Brazier.brazierEmitsSmoke; }
        @Override
        public int getBurnOut() { return CampfireConfigHandler.Brazier.brazierBurnOut; }
        @Override
        public boolean doesSmokeFollowDye() { return CampfireConfigHandler.Brazier.doesSmokeFollowDye; }
        @Override
        public boolean resetDyeOnExtinguish() { return CampfireConfigHandler.Brazier.resetDyeOnExtinguish; }
        @Override
        public boolean unlitOnCraft() { return CampfireConfigHandler.Brazier.unlitOnCraft; }
        @Nonnull
        @Override
        public PoweredAction getPoweredAction() { return CampfireConfigHandler.Brazier.poweredAction; }
    };
    @Nonnull
    public static final ICampfireSettings CAMPFIRE_SETTINGS = new ICampfireSettings() {
        @Nonnull
        @Override
        public SoundEvent getCrackleSound() { return CampfireSoundEvents.CAMPFIRE_CRACKLE; }
        @Override
        public boolean getBurnsEntities() { return CampfireConfigHandler.Campfire.isCampfireBurningBlock; }
        @Override
        public boolean getEmitsSmoke() { return CampfireConfigHandler.Campfire.campfireEmitsSmoke; }
        @Override
        public int getBurnOut() { return CampfireConfigHandler.Campfire.campfireBurnOut; }
        @Override
        public boolean doesSmokeFollowDye() { return CampfireConfigHandler.Campfire.doesSmokeFollowDye; }
        @Override
        public boolean resetDyeOnExtinguish() { return CampfireConfigHandler.Campfire.resetDyeOnExtinguish; }
        @Override
        public boolean unlitOnCraft() { return CampfireConfigHandler.Campfire.unlitOnCraft; }
        @Nonnull
        @Override
        public PoweredAction getPoweredAction() { return CampfireConfigHandler.Campfire.poweredAction; }
    };

    // ======
    // BLOCKS
    // ======

    @RegistryContainer.Entry
    public static final BlockBrazier<CampfireWoodType.Normal> BRAZIER = RegistryContainer.Helper.create(new BlockBrazier<>(Material.IRON, MapColor.OBSIDIAN, CampfireAPI.NORMAL_TYPE, BRAZIER_SETTINGS), block -> block.setTranslationKey("campfire.brazier"));
    @RegistryContainer.Entry
    public static final BlockCampfire<CampfireWoodType.Normal> CAMPFIRE = RegistryContainer.Helper.create(new BlockCampfire<>(Material.WOOD, MapColor.OBSIDIAN, CampfireAPI.NORMAL_TYPE, CAMPFIRE_SETTINGS), block -> block.setTranslationKey("campfire.campfire"));
    @RegistryContainer.Entry
    public static final BlockCampfireAsh CAMPFIRE_ASH = RegistryContainer.Helper.create(new BlockCampfireAsh(Material.CIRCUITS, MapColor.OBSIDIAN), block -> block.setTranslationKey("campfire.campfire_ash"));
    @RegistryContainer.Entry
    public static final BlockChain CHAIN = RegistryContainer.Helper.create(new BlockChain(Material.IRON, MapColor.AIR), block -> block.setTranslationKey("campfire.chain"));
    @RegistryContainer.Entry
    public static final BlockLantern LANTERN = RegistryContainer.Helper.create(new BlockLantern(Material.IRON), block -> block.setTranslationKey("campfire.lantern"));

    // =============
    // TILE ENTITIES
    // =============

    @RegistryContainer.Action
    static void registerTiles() {
        TileEntity.register("campfire:brazier", TileEntityBrazier.class);
        TileEntity.register("campfire:campfire", TileEntityCampfire.class);
        TileEntity.register("campfire:color_emitting", TileEntityColorEmitting.class);
    }
}
