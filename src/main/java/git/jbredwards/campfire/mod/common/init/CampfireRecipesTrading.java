/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.init;

import git.jbredwards.campfire.api.registry.RegistryContainer;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 *
 * @author jbred
 *
 */
@RegistryContainer(VillagerRegistry.VillagerProfession.class)
final class CampfireRecipesTrading
{
    @RegistryContainer.Action(priority = EventPriority.LOW)
    static void registerTrades(@Nonnull final IForgeRegistry<VillagerRegistry.VillagerProfession> registry) {
        IntStream.of(0, 1).mapToObj(Objects.requireNonNull(registry.getValue(new ResourceLocation("librarian")))::getCareer).forEach(career -> career
                .addTrade(2, new EntityVillager.ListItemForEmeralds(CampfireItems.LANTERN, new EntityVillager.PriceInfo(2, 2))));
        IntStream.of(0, 1, 2).mapToObj(Objects.requireNonNull(registry.getValue(new ResourceLocation("smith")))::getCareer).forEach(career -> career
                .addTrade(2, new EntityVillager.EmeraldForItems(CampfireItems.CHAIN, new EntityVillager.PriceInfo(5, 7))));
    }
}
