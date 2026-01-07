/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.block.campfire;

import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public interface ICampfireSettings
{
    @Nonnull
    SoundEvent getCrackleSound();

    // ---------------
    // config settings
    // ---------------

    boolean getBurnsEntities();
    boolean getEmitsSmoke();
    int getBurnOut();

    boolean doesSmokeFollowDye();
    boolean resetDyeOnExtinguish();
    boolean unlitOnCraft();

    @Nonnull
    PoweredAction getPoweredAction();
    enum PoweredAction
    {
        IGNORE("configgui.campfire.enums.poweredAction.ignore"),
        COLOR("configgui.campfire.enums.poweredAction.color"),
        DISABLE("configgui.campfire.enums.poweredAction.disable");

        @Nonnull final String langKey;
        PoweredAction(@Nonnull final String langKeyIn) { langKey = langKeyIn; }

        @Nonnull
        @Override
        public String toString() { return langKey; }
    }
}
