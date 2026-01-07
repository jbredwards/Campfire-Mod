/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * A generic item recipe interface for easy CraftTweaker and GroovyScript mod integration.
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public interface IGenericRecipe<V extends IGenericRecipe<V>> extends IForgeRegistryEntry<V>
{
    /**
     * @return The conditions that all inputs must satisfy (i.e. a list of valid ingredients).
     * @since 2.0.0
     * @author jbred
     */
    @Nonnull
    Collection<? extends Predicate<ItemStack>> getInputs();

    /**
     * @return The output ItemStacks for this recipe.
     * @since 2.0.0
     * @author jbred
     */
    @Nonnull
    Collection<ItemStack> getOutputs();
}
