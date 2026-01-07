/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.init;

import git.jbredwards.campfire.api.CampfireAPI;
import git.jbredwards.campfire.api.recipe.campfire.CampfireRecipe;
import git.jbredwards.campfire.api.registry.RegistryContainer;
import git.jbredwards.campfire.mod.common.config.CampfireConfigHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 *
 * @author jbred
 *
 */
@RegistryContainer
public final class CampfireRecipesCampfire
{
    @RegistryContainer.Action
    static void buildCampfireRecipes(@Nonnull final FMLPostInitializationEvent event) {
        if(CampfireConfigHandler.Campfire.useFurnaceFoodRecipes) {
            CampfireAPI.CAMPFIRE_RECIPES.unfreeze();
            FurnaceRecipes.instance().getSmeltingList().entrySet().stream()
                    .flatMap(e -> {
                        if(e.getKey().getMetadata() != OreDictionary.WILDCARD_VALUE) return Stream.of(e);
                        @Nonnull final NonNullList<ItemStack> stacks = NonNullList.create();

                        e.getKey().getItem().getSubItems(CreativeTabs.SEARCH, stacks);
                        return stacks.stream().map(s -> Pair.of(s, e.getValue()));
                    })
                    .forEach(e -> {
                        @Nonnull final ItemStack in = e.getKey(), out = e.getValue();
                        if(in.getItem() instanceof ItemFood && out.getItem() instanceof ItemFood
                        && CampfireAPI.CAMPFIRE_RECIPES.getValuesCollection().stream().noneMatch(r -> r.getInputs().stream().anyMatch(i -> i.test(in)))) {
                            CampfireAPI.CAMPFIRE_RECIPES.register(new CampfireRecipe.Normal(out, woodType -> true, in,
                            600, FurnaceRecipes.instance().getSmeltingExperience(out)).setRegistryName(generateName(in, out)));
                        }
                    });

            CampfireAPI.CAMPFIRE_RECIPES.freeze();
        }
    }

    @Nonnull
    static ResourceLocation generateName(@Nonnull final ItemStack in, @Nonnull final ItemStack out) {
        @Nonnull final StringBuilder path = new StringBuilder("builtin/");
        path.append(String.valueOf(in.getItem().getRegistryName()).replace(':', '.'));
        if(in.getMetadata() != 0) path.append('.').append(in.getMetadata());

        path.append("/to/");
        path.append(String.valueOf(out.getItem().getRegistryName()).replace(':', '.'));
        if(out.getMetadata() != 0) path.append('.').append(out.getMetadata());

        return new ResourceLocation("campfire", path.toString());
    }
}
