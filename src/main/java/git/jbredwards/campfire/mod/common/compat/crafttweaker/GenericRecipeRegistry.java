/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import git.jbredwards.campfire.api.recipe.IGenericRecipe;
import net.minecraftforge.registries.ForgeRegistry;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provides most of the same functionality found in {@link crafttweaker.api.recipes.IRecipeManager}.
 * @author jbred
 *
 */
public abstract class GenericRecipeRegistry<V extends IGenericRecipe<V>>
{
    @Nonnull
    protected final ForgeRegistry<V> registry;
    public GenericRecipeRegistry(@Nonnull final ForgeRegistry<V> registryIn) { registry = registryIn; }

    @ZenGetter("all")
    public List<V> getAll() { return registry.getValues(); }

    @ZenMethod
    public List<V> getRecipesFor(@Nullable final IIngredient output, @Optional final boolean nbtMatch) {
        if(output != null) return registry.getValuesCollection().stream().filter(r -> outputsMatch(r, output, nbtMatch)).collect(Collectors.toList());
        else {
            CraftTweakerAPI.logError("Cannot get recipes for a null item!");
            return Collections.emptyList();
        }
    }

    /**
     * Removes all recipes of this type.
     */
    @ZenMethod
    public void removeAll() {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() { removeAll(r -> true); }

            @Nonnull
            @Override
            public String describe() { return "Removing all recipes"; }
        });
    }

    /**
     * Removes all recipes of this type, that produce the specified item.
     *
     * @param output recipe output pattern
     *
     */
    @ZenMethod
    public void remove(@Nullable final IIngredient output, @Optional final boolean nbtMatch) {
        if(output == null) CraftTweakerAPI.logError("Cannot remove recipes for a null item!");
        else CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() { removeAll(r -> outputsMatch(r, output, nbtMatch)); }

            @Nonnull
            @Override
            public String describe() { return "Removing recipes for " + output + " output"; }
        });
    }

    /**
     * Removes all recipes of this type, with the given registry name.
     *
     * @param recipeName RegistryName of the recipe
     *
     */
    @ZenMethod
    public void removeByRecipeName(@Nullable final String recipeName, @Optional @Nullable final IItemStack output, @Optional final boolean nbtMatch) {
        if(recipeName == null) CraftTweakerAPI.logError("Cannot remove recipes for a null recipe name!");
        else CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                removeFirst(r -> {
                    if(!String.valueOf(r.getRegistryName()).equals(recipeName)) return false;
                    else return output == null || outputsMatch(r, output, nbtMatch);
                });
            }

            @Nonnull
            @Override
            public String describe() {
                return output != null
                        ? "Removing recipe with name \"" + recipeName + "\", Matching filter: " + output.getDisplayName()
                        : "Removing recipe with name \"" + recipeName + "\"";
            }
        });
    }

    /**
     * Removes all recipes of this type, which match the given regex String.
     *
     * @param regexString Regex String to match to
     */
    @ZenMethod
    public void removeByRegex(@Nullable final String regexString, @Optional @Nullable final IItemStack output, @Optional final boolean nbtMatch) {
        if(regexString == null) CraftTweakerAPI.logError("No regex String for the recipe to remove was given.");
        else CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                @Nonnull final Pattern p = Pattern.compile(regexString);
                removeAll(r -> {
                    if(!p.matcher(String.valueOf(r.getRegistryName())).matches()) return false;
                    else return output == null || outputsMatch(r, output, nbtMatch);
                });
            }

            @Nonnull
            @Override
            public String describe() {
                return output != null
                    ? "Removing all recipes matching this regex: \"" + regexString + "\", Matching filter: " + output.getDisplayName()
                    : "Removing all recipes matching this regex: \"" + regexString + "\"";
            }
        });
    }

    /**
     * Removes all recipes of this type, which match the given mod id.
     *
     * @param modid mod id of recipes to remove
     */
    @ZenMethod
    public void removeByMod(@Nullable final String modid) {
        if(modid == null) CraftTweakerAPI.logError("Cannot remove recipes for a null mod id!");
        else CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                registry.getEntries().stream()
                        .filter(e -> e.getKey().getNamespace().equals(modid))
                        .collect(Collectors.toList()).forEach(e -> remove(e.getValue()));
            }

            @Nonnull
            @Override
            public String describe() { return "Removing recipes matching: " + modid; }
        });
    }

    /**
     * Removes all recipes of this type, that contain specified ingredient as input.
     *
     * @param input recipe input pattern
     */
    @ZenMethod
    public void removeByInput(@Nullable final IIngredient input) {
        if(input == null) CraftTweakerAPI.logError("Cannot remove recipes for a null item!");
        else CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                @Nullable final List<IItemStack> stacks = input.getItems();
                if(stacks == null) removeAll(r -> true); // null stacks list means wildcard selector (all ingredients)
                else removeAll(r -> r.getInputs().stream().anyMatch(i -> stacks.stream().map(CraftTweakerMC::getItemStack).anyMatch(i)));
            }

            @Nonnull
            @Override
            public String describe() { return "Removing recipes for " + input + " input"; }
        });
    }

    /**
     * Removes all recipes of this type, that produce the specified item.
     *
     * @param output recipe output pattern
     *
     */
    @ZenMethod
    public void removeByOutput(@Nullable final IIngredient output, @Optional final boolean nbtMatch) { remove(output, nbtMatch); }

    // ================
    // INTERNAL METHODS
    // ================

    protected boolean outputsMatch(@Nonnull final V recipe, @Nonnull final IIngredient filter, final boolean nbtMatch) {
        return recipe.getOutputs().stream().map(CraftTweakerMC::getIItemStackForMatching).filter(Objects::nonNull).anyMatch(nbtMatch ? filter::matchesExact : filter::matches);
    }

    protected void removeFirst(@Nonnull final Predicate<V> filter) {
        registry.getValuesCollection().stream().filter(filter).findFirst().ifPresent(this::remove);
    }

    protected void removeAll(@Nonnull final Predicate<V> filter) {
        registry.getValuesCollection().stream().filter(filter).collect(Collectors.toList()).forEach(this::remove);
    }

    // here in case I eventually want to add ZenRecipeReload compat
    protected void add(@Nonnull final V recipe) { registry.register(recipe); }
    protected void remove(@Nonnull final V recipe) { registry.remove(recipe.getRegistryName()); }
}
