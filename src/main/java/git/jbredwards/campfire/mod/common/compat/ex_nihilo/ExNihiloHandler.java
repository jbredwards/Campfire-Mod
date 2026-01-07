/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.ex_nihilo;

import exnihilocreatio.registries.manager.ExNihiloRegistryManager;
import exnihilocreatio.util.BlockInfo;
import git.jbredwards.campfire.api.registry.RegistryContainer;
import git.jbredwards.campfire.mod.common.init.CampfireBlocks;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@RegistryContainer(dependencies = "exnihilocreatio")
final class ExNihiloHandler
{
    @RegistryContainer.Action(priority = EventPriority.LOWEST)
    static void registerHeatSources(@Nonnull final FMLPostInitializationEvent event) {
        ExNihiloRegistryManager.HEAT_REGISTRY.register(new CampfireBlockInfo(CampfireBlocks.BRAZIER.getDefaultState()), 4);
        ExNihiloRegistryManager.HEAT_REGISTRY.register(new CampfireBlockInfo(CampfireBlocks.CAMPFIRE.getDefaultState()), 4);
        ExNihiloRegistryManager.HEAT_REGISTRY.register(new BlockInfo(CampfireBlocks.LANTERN.getDefaultState()), 1);
        // replace ex nihilo heat registry with one that has a special case for campfires
        // (without this, entries for campfires are needed for every lit blockState, thus leading to duplicate jei recipes)
        ReflectionHelper.setPrivateValue(ExNihiloRegistryManager.class, null, new CampfireHeatRegistry(ExNihiloRegistryManager.HEAT_REGISTRY), "HEAT_REGISTRY");
    }
}
