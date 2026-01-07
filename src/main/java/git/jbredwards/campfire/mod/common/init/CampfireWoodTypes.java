/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.init;

import git.jbredwards.campfire.api.CampfireAPI;
import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.api.registry.RegistryContainer;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@RegistryContainer(CampfireWoodType.Normal.class)
final class CampfireWoodTypes
{
    @RegistryContainer.Action(priority = EventPriority.HIGH)
    static void registerBuiltinTypes(@Nonnull final FMLPostInitializationEvent event) {
        CampfireWoodType.postInit(CampfireAPI.WOOD_TYPES, CampfireWoodType.Normal::new);
    }
}
