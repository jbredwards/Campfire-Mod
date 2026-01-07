/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.groovyscript;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.api.*;
import com.cleanroommc.groovyscript.api.documentation.annotations.Example;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RecipeBuilderDescription;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.helper.ingredient.IngredientHelper;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import git.jbredwards.campfire.api.CampfireAPI;
import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.api.recipe.campfire.CampfireRecipe;
import git.jbredwards.campfire.api.recipe.campfire.IgniteRecipe;
import git.jbredwards.campfire.mod.common.compat.groovyscript.builder.CampfireRecipeBuilder;
import git.jbredwards.campfire.mod.common.init.CampfireRecipesIgniting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

/**
 *
 * @author jbred
 *
 */
public final class CampfireGroovyScriptPlugin implements GroovyPlugin
{
    @Nonnull
    public static final INamed IGNITING = new VirtualizedRegistry<IgniteRecipe>(Collections.singleton("igniting")) {
        @Override
        @GroovyBlacklist
        public void onReload() {
            CampfireRecipesIgniting.RECIPES.removeAll(removeScripted());
            CampfireRecipesIgniting.RECIPES.addAll(restoreFromBackup());
        }

        public void add(@Nullable final IgniteRecipe recipe) {
            if(recipe == null) return;
            addScripted(recipe);
            CampfireRecipesIgniting.RECIPES.add(0, recipe);
        }

        public void add(@Nonnull final IIngredient condition, final boolean consume) {
            add(new IgniteRecipe(condition, consume));
        }

        public void add(@Nonnull final IIngredient condition, @Nonnull final SoundEvent sound, final boolean consume) {
            add(new IgniteRecipe(condition, sound, consume));
        }

        @MethodDescription(priority = 2000, example = @Example(commented = true))
        public void removeAll() {
            CampfireRecipesIgniting.RECIPES.forEach(this::addBackup);
            CampfireRecipesIgniting.RECIPES.clear();
        }
    };

    @Nonnull
    public static final INamed RECIPES = new GenericRecipeRegistry<CampfireRecipe.Normal>(CampfireAPI.CAMPFIRE_RECIPES, Collections.singleton("recipes")) {
        public void add(@Nonnull final IIngredient output, @Nonnull final IIngredient input, @Nonnull final IIngredient woodTypes, final int cookTime, final float experience) {
            add(new CampfireRecipe.Normal(IngredientHelper.toItemStack(output), woodTypes, input.toMcIngredient(), cookTime, experience));
        }

        @MethodDescription(example = @Example("item('minecraft:log:3')"))
        public boolean removeByWoodType(@Nullable final IIngredient woodType) {
            return removeByIngredient(woodType, recipe -> Collections.singleton(recipe.woodTypes));
        }

        @Nonnull
        @RecipeBuilderDescription(example = {
                @Example(".input(item('minecraft:clay')).output(item('minecraft:diamond')).experience(1)"),
                @Example(".input(oredict('planks')).output(item('minecraft:emerald')).woodTypes(item('minecraft:log:0')).cookTime(500)")
        })
        public CampfireRecipeBuilder<CampfireRecipe.Normal> recipeBuilder() { return new CampfireRecipeBuilder<>(this, CampfireRecipe.Normal::new); }
    };

    @Nonnull
    public static final INamed TYPES = new AbstractRecipeRegistry<CampfireWoodType.Normal>(CampfireAPI.WOOD_TYPES, Collections.singleton("types")) {
        public void add(@Nullable final IIngredient woodType) {
            if(woodType != null && getRegistry().getValuesCollection().stream().noneMatch(wt -> woodType.test(wt.asItemStack()))) {
                for(@Nonnull final ItemStack s : woodType.getMatchingStacks())
                    add(new CampfireWoodType.Normal(s).setRegistryName(GroovyScript.getRunConfig().getPackId(),
                    "gs/" + String.valueOf(s.getItem().getRegistryName()).replace(':', '/') + '/' + s.getMetadata()
                ));
            }
        }

        public boolean remove(@Nullable final IIngredient woodType) {
            return woodType != null && streamRecipes().removeIf(wt -> woodType.test(wt.asItemStack()));
        }
    };

    @Override
    public void onCompatLoaded(@Nonnull final GroovyContainer<?> container) {
        container.addProperty(IGNITING);
        container.addProperty(RECIPES);
        container.addProperty(TYPES);
    }

    @Nonnull
    @Override
    public String getModId() { return "campfire"; }

    @Nonnull
    @Override
    public String getContainerName() { return "Campfire"; }
}
