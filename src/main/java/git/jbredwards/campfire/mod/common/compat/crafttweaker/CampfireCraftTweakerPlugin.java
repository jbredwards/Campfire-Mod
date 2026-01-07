/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.crafttweaker;

import com.teamacronymcoders.contenttweaker.modules.vanilla.resources.sounds.ISoundEventDefinition;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import git.jbredwards.campfire.api.CampfireAPI;
import git.jbredwards.campfire.api.recipe.campfire.CampfireRecipe;
import git.jbredwards.campfire.api.recipe.campfire.IgniteRecipe;
import git.jbredwards.campfire.mod.common.init.CampfireRecipesIgniting;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
@ZenRegister
@ZenClass("campfire")
public final class CampfireCraftTweakerPlugin
{
    @Nonnull
    public static final GenericRecipeRegistry<CampfireRecipe.Normal> recipes = new GenericRecipeRegistry<CampfireRecipe.Normal>(CampfireAPI.CAMPFIRE_RECIPES) {

    };

    @ZenRegister
    @ZenClass("campfire.igniting")
    public static final class Igniting
    {
        @ZenMethod
        public static void add(@Nonnull final IIngredient condition, final boolean consume) {
            CraftTweakerAPI.apply(new IAction() {
                @Override
                public void apply() { CampfireRecipesIgniting.RECIPES.add(0, new IgniteRecipe(CraftTweakerMC.getIngredient(condition), consume)); }

                @Nonnull
                @Override
                public String describe() { return "Add new campfire igniting items."; }
            });
        }

        @ZenMethod
        @Optional.Method(modid = "contenttweaker")
        public static void add(@Nonnull final IIngredient condition, @Nonnull final ISoundEventDefinition igniteSound, final boolean consume) {
            CraftTweakerAPI.apply(new IAction() {
                @Override
                public void apply() { CampfireRecipesIgniting.RECIPES.add(0, new IgniteRecipe(CraftTweakerMC.getIngredient(condition), igniteSound.getInternal(), consume)); }

                @Nonnull
                @Override
                public String describe() { return "Add new campfire igniting items."; }
            });
        }

        @ZenMethod
        public static void removeAll() {
            CraftTweakerAPI.apply(new IAction() {
                @Override
                public void apply() { CampfireRecipesIgniting.RECIPES.clear(); }

                @Nonnull
                @Override
                public String describe() { return "Remove all campfire igniting items."; }
            });
        }
    }

    @ZenRegister
    @ZenClass("campfire.recipes")
    public static final class Recipes
    {

    }

    @ZenRegister
    @ZenClass("campfire.types")
    public static final class Types
    {

    }
}
