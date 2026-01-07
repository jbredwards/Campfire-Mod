/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.init;

import git.jbredwards.campfire.api.registry.RegistryContainer;
import net.minecraft.block.SoundType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@RegistryContainer(SoundEvent.class)
public final class CampfireSoundEvents
{
    // ============
    // SOUND EVENTS
    // ============

    @RegistryContainer.Entry
    public static final SoundEvent BRAZIER_CRACKLE = new SoundEvent(new ResourceLocation("campfire", "blocks.brazier.crackle"));
    @RegistryContainer.Entry
    public static final SoundEvent CAMPFIRE_CRACKLE = new SoundEvent(new ResourceLocation("campfire", "blocks.campfire.crackle"));
    @RegistryContainer.Entry
    public static final SoundEvent CHAIN_BREAK = new SoundEvent(new ResourceLocation("campfire", "blocks.chain.break"));
    @RegistryContainer.Entry
    public static final SoundEvent CHAIN_FALL = new SoundEvent(new ResourceLocation("campfire", "blocks.chain.fall"));
    @RegistryContainer.Entry
    public static final SoundEvent CHAIN_HIT = new SoundEvent(new ResourceLocation("campfire", "blocks.chain.hit"));
    @RegistryContainer.Entry
    public static final SoundEvent CHAIN_PLACE = new SoundEvent(new ResourceLocation("campfire", "blocks.chain.place"));
    @RegistryContainer.Entry
    public static final SoundEvent CHAIN_STEP = new SoundEvent(new ResourceLocation("campfire", "blocks.chain.step"));
    @RegistryContainer.Entry
    public static final SoundEvent LANTERN_BREAK = new SoundEvent(new ResourceLocation("campfire", "blocks.lantern.break"));
    @RegistryContainer.Entry
    public static final SoundEvent LANTERN_FALL = new SoundEvent(new ResourceLocation("campfire", "blocks.lantern.fall"));
    @RegistryContainer.Entry
    public static final SoundEvent LANTERN_HIT = new SoundEvent(new ResourceLocation("campfire", "blocks.lantern.hit"));
    @RegistryContainer.Entry
    public static final SoundEvent LANTERN_PLACE = new SoundEvent(new ResourceLocation("campfire", "blocks.lantern.place"));
    @RegistryContainer.Entry
    public static final SoundEvent LANTERN_STEP = new SoundEvent(new ResourceLocation("campfire", "blocks.lantern.step"));

    // ===========
    // SOUND TYPES
    // ===========

    @Nonnull
    public static final SoundType CHAIN_TYPE = new SoundType(1, 1, CHAIN_BREAK, CHAIN_STEP, CHAIN_PLACE, CHAIN_HIT, CHAIN_FALL);
    @Nonnull
    public static final SoundType LANTERN_TYPE = new SoundType(1, 1, LANTERN_BREAK, LANTERN_STEP, LANTERN_PLACE, LANTERN_HIT, LANTERN_FALL);
}
