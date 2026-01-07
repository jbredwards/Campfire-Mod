/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.groovyscript.builder;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import git.jbredwards.campfire.api.recipe.IGenericRecipe;
import git.jbredwards.campfire.mod.common.compat.groovyscript.GenericRecipeRegistry;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public abstract class GenericRecipeBuilder<V extends IGenericRecipe<V>> extends AbstractRecipeBuilder<V>
{
    @Nonnull
    @GroovyBlacklist
    protected final GenericRecipeRegistry<V> registry;
    protected GenericRecipeBuilder(@Nonnull final GenericRecipeRegistry<V> registryIn) { registry = registryIn; }
}
