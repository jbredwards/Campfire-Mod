/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.init;

import git.jbredwards.campfire.api.recipe.campfire.IgniteRecipe;
import git.jbredwards.campfire.api.registry.RegistryContainer;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
@RegistryContainer(hasEvents = true)
public final class CampfireRecipesIgniting
{
    @Nonnull
    public static final List<IgniteRecipe> RECIPES = new ArrayList<>();

    /*@SubscribeEvent(priority = EventPriority.HIGH)
    static void buildIgniteRecipes(@Nonnull final GenericRecipeEvent<IgniteRecipe> event) {
        RECIPES.clear();
        RECIPES.add(new IgniteRecipe(stack -> stack.getItem() instanceof ItemFireball, true));
        RECIPES.add(new IgniteRecipe(stack -> stack.getItem() instanceof ItemFlintAndSteel, false));
    }*/
}
