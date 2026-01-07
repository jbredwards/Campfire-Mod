/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.Example;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import git.jbredwards.campfire.api.recipe.IGenericRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author jbred
 *
 */
@RegistryDescription
public abstract class GenericRecipeRegistry<T extends IGenericRecipe<T>> extends AbstractRecipeRegistry<T>
{
    public GenericRecipeRegistry(@Nonnull final IForgeRegistry<T> registry) { this(registry, null); }
    public GenericRecipeRegistry(@Nonnull final IForgeRegistry<T> registry, @Nullable final Collection<String> aliases) {
        super(registry, aliases);
    }

    @MethodDescription(example = @Example("item('minecraft:potato')"))
    public boolean removeByInput(@Nullable final IIngredient input) {
        return removeByIngredient(input, IGenericRecipe::getInputs);
    }

    @MethodDescription(example = @Example("item('minecraft:baked_potato')"))
    public boolean removeByOutput(@Nullable final IIngredient output) {
        return removeByStack(output, IGenericRecipe::getOutputs);
    }

    // ========
    // INTERNAL
    // ========

    @GroovyBlacklist
    protected final boolean removeByStack(@Nullable final IIngredient toRemove, @Nonnull final Function<T, Collection<ItemStack>> recipeToStack) {
        return toRemove != null && streamRecipes().removeIf(recipe -> recipeToStack.apply(recipe).stream().anyMatch(toRemove));
    }

    @GroovyBlacklist
    protected final boolean removeByIngredient(@Nullable final IIngredient toRemove, @Nonnull final Function<T, Collection<? extends Predicate<ItemStack>>> recipeToIngredient) {
        if(toRemove == null) return false;

        @Nonnull final ItemStack[] matching = toRemove.getMatchingStacks();
        return streamRecipes().removeIf(recipe -> Arrays.stream(matching).anyMatch(stack -> recipeToIngredient.apply(recipe).stream().anyMatch(ingredient -> ingredient.test(stack))));
    }
}
