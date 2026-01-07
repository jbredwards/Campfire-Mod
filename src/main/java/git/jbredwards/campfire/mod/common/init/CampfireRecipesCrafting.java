/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.init;

import git.jbredwards.campfire.api.CampfireAPI;
import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.api.recipe.ingredient.CompoundIngredient;
import git.jbredwards.campfire.api.recipe.ingredient.WrappedIngredient;
import git.jbredwards.campfire.api.registry.RegistryContainer;
import git.jbredwards.campfire.mod.common.recipe.crafting.BrazierCraftingRecipe;
import git.jbredwards.campfire.mod.common.recipe.crafting.CampfireCraftingRecipe;
import git.jbredwards.campfire.mod.common.recipe.crafting.ColoredCampfireRecipe;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@RegistryContainer(IRecipe.class)
final class CampfireRecipesCrafting
{
    @RegistryContainer.Action
    static void registerRecipes(@Nonnull final IForgeRegistry<IRecipe> registry) {
        // recipes with some sort of special functionality
        @Nonnull final WrappedIngredient woodTypes = new WrappedIngredient.Registry<>(CampfireAPI.WOOD_TYPES, CampfireWoodType::asItemStack);
        registry.register(new CampfireCraftingRecipe(CampfireItems.CAMPFIRE, woodTypes, " S ", "SCS", "LLL", 'S', "stickWood", 'C', CompoundIngredient.from("coal", "charcoal", Items.FIRE_CHARGE), 'L')
                .setRegistryName("builtin/campfires"));
        registry.register(new ColoredCampfireRecipe()
                .setRegistryName("builtin/dyeables"));

        // normally behaving recipes
        registry.register(new BrazierCraftingRecipe(CampfireItems.CAMPFIRE, CampfireItems.BRAZIER)
                .setRegistryName("brazier"));
        registry.register(new ShapedOreRecipe(null, CampfireItems.CHAIN, "N", "I", "N", 'N', "nuggetIron", 'I', "ingotIron")
                .setRegistryName("chain"));
        registry.register(new ShapedOreRecipe(null, CampfireItems.LANTERN, "NNN", "NTN", "NNN", 'N', "nuggetIron", 'T', Blocks.TORCH)
                .setRegistryName("lantern"));
    }
}
