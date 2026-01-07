/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod;

import com.cleanroommc.assetmover.AssetMoverAPI;
import com.google.common.collect.ImmutableMap;
import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.api.capability.CapabilityStorage;
import git.jbredwards.campfire.api.recipe.campfire.CampfireRecipe;
import git.jbredwards.campfire.api.registry.RegistryContainer;
import git.jbredwards.campfire.mod.client.renderer.tileentity.CampfireTESR;
import git.jbredwards.campfire.mod.common.capability.ICampfireWoodType;
import git.jbredwards.campfire.mod.common.dispenser.BehaviorCampfireIgnite;
import git.jbredwards.campfire.mod.common.message.MessageExtinguishEffects;
import git.jbredwards.campfire.mod.common.message.MessageFallParticles;
import git.jbredwards.campfire.mod.common.message.MessageSyncCampfireSlot;
import git.jbredwards.campfire.mod.common.tileentity.TileEntityCampfire;
import net.minecraft.block.BlockDispenser;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber
@Mod(modid = "campfire", useMetadata = true,
        dependencies = "required-client:assetmover@[2.5,);after:piston_api@[1.0.2,)",
        guiFactory = "git.jbredwards.campfire.mod.client.gui.CampfireGuiFactory"
)
public final class Campfire
{
    @Nonnull
    public static final SimpleNetworkWrapper WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel("campfire");
    public static final boolean isFluidloggedAPI = Loader.isModLoaded("fluidlogged_api");

    @SubscribeEvent
    static void createRegistries(@Nonnull final RegistryEvent.NewRegistry event) {
        new RegistryBuilder<CampfireRecipe.Normal>().setName(new ResourceLocation("campfire", "campfire_recipes"))
                .setType(CampfireRecipe.Normal.class).disableSaving().allowModification().create();
        new RegistryBuilder<CampfireWoodType.Normal>().setName(new ResourceLocation("campfire", "wood_types"))
                .setType(CampfireWoodType.Normal.class).allowModification().create();
    }

    @Mod.EventHandler
    static void preInit(@Nonnull final FMLPreInitializationEvent event) throws Exception {
        WRAPPER.registerMessage(MessageFallParticles.Handler.INSTANCE, MessageFallParticles.class, 0, Side.CLIENT);
        WRAPPER.registerMessage(MessageSyncCampfireSlot.Handler.INSTANCE, MessageSyncCampfireSlot.class, 1, Side.CLIENT);
        WRAPPER.registerMessage(MessageExtinguishEffects.Handler.INSTANCE, MessageExtinguishEffects.class, 2, Side.CLIENT);

        CapabilityManager.INSTANCE.register(ICampfireWoodType.class, new CapabilityStorage<>(), () -> { throw new UnsupportedOperationException(); });
        ModuleHandler.initializeRegistryModules(event.getAsmData().getAll(RegistryContainer.class.getCanonicalName()));
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    static void preInitClient(@Nonnull final FMLPreInitializationEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCampfire.class, new CampfireTESR());
    }

    @Mod.EventHandler
    static void postInit(@Nonnull final FMLPostInitializationEvent event) {
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FIRE_CHARGE, new BehaviorCampfireIgnite(Items.FIRE_CHARGE));
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FLINT_AND_STEEL, new BehaviorCampfireIgnite(Items.FLINT_AND_STEEL));
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    static void constructClient(@Nonnull final FMLConstructionEvent event) {
        AssetMoverAPI.fromMinecraft("1.18.2", ImmutableMap.<String, String>builder()
                .put("assets/minecraft/models/block/chain.json", "assets/campfire/models/block/chain.json")
                .put("assets/minecraft/sounds/block/campfire/crackle1.ogg", "assets/campfire/sounds/crackle1.ogg")
                .put("assets/minecraft/sounds/block/campfire/crackle2.ogg", "assets/campfire/sounds/crackle2.ogg")
                .put("assets/minecraft/sounds/block/campfire/crackle3.ogg", "assets/campfire/sounds/crackle3.ogg")
                .put("assets/minecraft/sounds/block/campfire/crackle4.ogg", "assets/campfire/sounds/crackle4.ogg")
                .put("assets/minecraft/sounds/block/campfire/crackle5.ogg", "assets/campfire/sounds/crackle5.ogg")
                .put("assets/minecraft/sounds/block/campfire/crackle6.ogg", "assets/campfire/sounds/crackle6.ogg")
                .put("assets/minecraft/sounds/block/chain/break1.ogg", "assets/campfire/sounds/chain/break1.ogg")
                .put("assets/minecraft/sounds/block/chain/break2.ogg", "assets/campfire/sounds/chain/break2.ogg")
                .put("assets/minecraft/sounds/block/chain/break3.ogg", "assets/campfire/sounds/chain/break3.ogg")
                .put("assets/minecraft/sounds/block/chain/break4.ogg", "assets/campfire/sounds/chain/break4.ogg")
                .put("assets/minecraft/sounds/block/chain/step1.ogg", "assets/campfire/sounds/chain/step1.ogg")
                .put("assets/minecraft/sounds/block/chain/step2.ogg", "assets/campfire/sounds/chain/step2.ogg")
                .put("assets/minecraft/sounds/block/chain/step3.ogg", "assets/campfire/sounds/chain/step3.ogg")
                .put("assets/minecraft/sounds/block/chain/step4.ogg", "assets/campfire/sounds/chain/step4.ogg")
                .put("assets/minecraft/sounds/block/chain/step5.ogg", "assets/campfire/sounds/chain/step5.ogg")
                .put("assets/minecraft/sounds/block/chain/step6.ogg", "assets/campfire/sounds/chain/step6.ogg")
                .put("assets/minecraft/sounds/block/lantern/break1.ogg", "assets/campfire/sounds/lantern/break1.ogg")
                .put("assets/minecraft/sounds/block/lantern/break2.ogg", "assets/campfire/sounds/lantern/break2.ogg")
                .put("assets/minecraft/sounds/block/lantern/break3.ogg", "assets/campfire/sounds/lantern/break3.ogg")
                .put("assets/minecraft/sounds/block/lantern/break4.ogg", "assets/campfire/sounds/lantern/break4.ogg")
                .put("assets/minecraft/sounds/block/lantern/break5.ogg", "assets/campfire/sounds/lantern/break5.ogg")
                .put("assets/minecraft/sounds/block/lantern/break6.ogg", "assets/campfire/sounds/lantern/break6.ogg")
                .put("assets/minecraft/sounds/block/lantern/place1.ogg", "assets/campfire/sounds/lantern/place1.ogg")
                .put("assets/minecraft/sounds/block/lantern/place2.ogg", "assets/campfire/sounds/lantern/place2.ogg")
                .put("assets/minecraft/sounds/block/lantern/place3.ogg", "assets/campfire/sounds/lantern/place3.ogg")
                .put("assets/minecraft/sounds/block/lantern/place4.ogg", "assets/campfire/sounds/lantern/place4.ogg")
                .put("assets/minecraft/sounds/block/lantern/place5.ogg", "assets/campfire/sounds/lantern/place5.ogg")
                .put("assets/minecraft/sounds/block/lantern/place6.ogg", "assets/campfire/sounds/lantern/place6.ogg")
                .put("assets/minecraft/textures/block/campfire_fire.png", "assets/campfire/textures/blocks/campfire_fire.png")
                .put("assets/minecraft/textures/block/campfire_fire.png.mcmeta", "assets/campfire/textures/blocks/campfire_fire.png.mcmeta")
                .put("assets/minecraft/textures/block/chain.png", "assets/minecraft/textures/block/chain.png") // fix missing texture error in logger
                .build());
    }

    // ==============
    // MODULE HANDLER
    // ==============

    @Mod.EventHandler
    static void fireModuleEvents(@Nonnull final FMLPreInitializationEvent event) { ModuleHandler.fireFMLEvents(event); }

    @Mod.EventHandler
    static void fireModuleEvents(@Nonnull final FMLInitializationEvent event) { ModuleHandler.fireFMLEvents(event); }

    @Mod.EventHandler
    static void fireModuleEvents(@Nonnull final FMLPostInitializationEvent event) { ModuleHandler.fireFMLEvents(event); }

    @Mod.EventHandler
    static void fireModuleEvents(@Nonnull final FMLLoadCompleteEvent event) { ModuleHandler.fireFMLEvents(event); }

    @Mod.EventHandler
    static void fireModuleEvents(@Nonnull final FMLServerAboutToStartEvent event) { ModuleHandler.fireFMLEvents(event); }

    @Mod.EventHandler
    static void fireModuleEvents(@Nonnull final FMLServerStartingEvent event) { ModuleHandler.fireFMLEvents(event); }

    @Mod.EventHandler
    static void fireModuleEvents(@Nonnull final FMLServerStartedEvent event) { ModuleHandler.fireFMLEvents(event); }

    @Mod.EventHandler
    static void fireModuleEvents(@Nonnull final FMLServerStoppingEvent event) { ModuleHandler.fireFMLEvents(event); }

    @Mod.EventHandler
    static void fireModuleEvents(@Nonnull final FMLServerStoppedEvent event) { ModuleHandler.fireFMLEvents(event); }
}
