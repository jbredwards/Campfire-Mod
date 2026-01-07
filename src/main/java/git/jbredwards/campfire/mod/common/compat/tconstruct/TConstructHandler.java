/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.tconstruct;

import git.jbredwards.campfire.api.registry.RegistryContainer;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.shared.TinkerFluids;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@RegistryContainer(dependencies = "tconstruct")
final class TConstructHandler
{
    @RegistryContainer.Action
    static void registerMeltingRecipes(@Nonnull final FMLPostInitializationEvent event) {
        TinkerRegistry.registerMelting("brazierIron", TinkerFluids.iron, 8 * Material.VALUE_Nugget);
        TinkerRegistry.registerMelting("chainIron", TinkerFluids.iron, 11 * Material.VALUE_Nugget);
        TinkerRegistry.registerMelting("lanternIron", TinkerFluids.iron, 8 * Material.VALUE_Nugget);
    }
}
