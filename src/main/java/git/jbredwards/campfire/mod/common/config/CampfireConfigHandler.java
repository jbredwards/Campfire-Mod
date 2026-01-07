/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.config;

import git.jbredwards.campfire.api.block.campfire.ICampfireSettings;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = "campfire")
public final class CampfireConfigHandler
{
    @Config(modid = "campfire", name = "campfire/brazier")
    public static final class Brazier
    {
        @Config.LangKey("config.campfire.brazierEmitsSmoke")
        public static boolean brazierEmitsSmoke = true;

        @Config.LangKey("config.campfire.isBrazierBurningBlock")
        public static boolean isBrazierBurningBlock = true;

        @Config.RangeInt(min = 0)
        @Config.LangKey("config.campfire.brazierBurnOut")
        public static int brazierBurnOut = 0;

        @Config.LangKey("config.campfire.doesSmokeFollowDye")
        public static boolean doesSmokeFollowDye = true;

        @Config.LangKey("config.campfire.resetDyeOnExtinguish")
        public static boolean resetDyeOnExtinguish = true;

        @Config.LangKey("config.campfire.unlitOnCraft")
        public static boolean unlitOnCraft = false;

        @Nonnull
        @Config.LangKey("config.campfire.poweredAction")
        public static ICampfireSettings.PoweredAction poweredAction = ICampfireSettings.PoweredAction.COLOR;
    }

    @Config(modid = "campfire", name = "campfire/campfire")
    public static final class Campfire
    {
        @Config.LangKey("config.campfire.campfireEmitsSmoke")
        public static boolean campfireEmitsSmoke = true;

        @Config.LangKey("config.campfire.campfireAshEmitsParticles")
        public static boolean campfireAshEmitsParticles = true;

        @Config.LangKey("config.campfire.campfireAutomation")
        public static boolean campfireAutomation = true;

        @Config.LangKey("config.campfire.hasExtraSlots")
        public static boolean hasExtraSlots = true;

        @Config.LangKey("config.campfire.isCampfireBurningBlock")
        public static boolean isCampfireBurningBlock = true;

        @Config.LangKey("config.campfire.recipeItemsOnCampfireOnly")
        public static boolean recipeItemsOnCampfireOnly = true;

        @Config.LangKey("config.campfire.unlitOnCraft")
        public static boolean unlitOnCraft = false;

        @Config.RequiresMcRestart
        @Config.LangKey(("config.campfire.useFurnaceFoodRecipes"))
        public static boolean useFurnaceFoodRecipes = true;

        @Config.RangeInt(min = 0)
        @Config.LangKey("config.campfire.campfireBurnOut")
        public static int campfireBurnOut = 0;

        @Config.LangKey("config.campfire.campfireBurnOutAsh")
        public static boolean campfireBurnOutAsh = true;

        @Config.LangKey("config.campfire.doesSmokeFollowDye")
        public static boolean doesSmokeFollowDye = true;

        @Config.LangKey("config.campfire.resetDyeOnExtinguish")
        public static boolean resetDyeOnExtinguish = true;

        @Nonnull
        @Config.LangKey("config.campfire.poweredAction")
        public static ICampfireSettings.PoweredAction poweredAction = ICampfireSettings.PoweredAction.COLOR;
    }

    @Config(modid = "campfire", name = "campfire/chain")
    public static final class Chain
    {
        @Config.LangKey("config.campfire.chainstone")
        public static Chainstone chainstone = Chainstone.PHYSICALLY_CONNECTED;
        public enum Chainstone
        {
            DISABLED("configgui.campfire.enums.chainstone.disabled"),
            PHYSICALLY_CONNECTED("configgui.campfire.enums.chainstone.physicallyConnected"),
            STICKY_AXIS("configgui.campfire.enums.chainstone.stickyAxis");

            @Nonnull final String langKey;
            Chainstone(@Nonnull final String langKeyIn) { langKey = langKeyIn; }

            @Nonnull
            @Override
            public String toString() { return langKey; }
        }
    }

    @SubscribeEvent
    static void sync(@Nonnull ConfigChangedEvent.OnConfigChangedEvent event) {
        if("campfire".equals(event.getModID())) ConfigManager.sync("campfire", Config.Type.INSTANCE);
    }
}
