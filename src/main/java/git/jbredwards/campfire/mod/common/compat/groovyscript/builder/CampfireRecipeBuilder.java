/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.groovyscript.builder;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.Comp;
import com.cleanroommc.groovyscript.api.documentation.annotations.Property;
import com.cleanroommc.groovyscript.api.documentation.annotations.RecipeBuilderMethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RecipeBuilderRegistrationMethod;
import git.jbredwards.campfire.api.recipe.campfire.CampfireRecipe;
import git.jbredwards.campfire.mod.common.compat.groovyscript.GenericRecipeRegistry;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 *
 * @author jbred
 *
 */
@Property(property = "input", comp = @Comp(types = Comp.Type.EQ, eq = 1))
@Property(property = "output", comp = @Comp(types = Comp.Type.EQ, eq = 1))
public class CampfireRecipeBuilder<V extends CampfireRecipe<V>> extends GenericRecipeBuilder<V>
{
    @Nonnull
    protected final RecipeGenerator<V> generator;
    public CampfireRecipeBuilder(@Nonnull final GenericRecipeRegistry<V> registryIn, @Nonnull final RecipeGenerator<V> generatorIn) {
        super(registryIn);
        generator = generatorIn;
    }

    @Nullable
    @Property
    protected IIngredient woodTypes;

    @Property(comp = @Comp(types = Comp.Type.GT, gt = 0))
    protected int cookTime = 400;

    @Property(comp = @Comp(types = Comp.Type.GTE, gte = 0))
    protected float experience = 0;

    @Nonnull
    @Override
    public String getErrorMsg() { return "Error adding Campfire cooking recipe"; }

    @Override
    public void validate(@Nonnull final GroovyLog.Msg msg) {
        validateItems(msg, 1, 1, 1, 1);
        msg.add(cookTime <= 0, "cookTime must be an integer greater than 0, but it was {}", cookTime);
        msg.add(experience < 0, "experience must be a non negative float, but it was {}", experience);
    }

    @Nonnull
    @RecipeBuilderMethodDescription
    public CampfireRecipeBuilder<V> woodTypes(@Nullable final IIngredient woodTypes) {
        this.woodTypes = woodTypes;
        return this;
    }

    @Nonnull
    @RecipeBuilderMethodDescription
    public CampfireRecipeBuilder<V> cookTime(final int cookTime) {
        this.cookTime = cookTime;
        return this;
    }

    @Nonnull
    @RecipeBuilderMethodDescription
    public CampfireRecipeBuilder<V> experience(final float experience) {
        this.experience = experience;
        return this;
    }

    @Nullable
    @Override
    @RecipeBuilderRegistrationMethod
    public V register() {
        if(!validate()) return null;
        validateName();

        @Nonnull final V recipe = generator.generate(output.get(0),
                (woodTypes != null ? woodTypes : IIngredient.ANY).toMcIngredient(),
                input.get(0).toMcIngredient(), cookTime, experience);

        registry.add(recipe.setRegistryName(name));
        return recipe;
    }

    @FunctionalInterface
    public interface RecipeGenerator<V extends CampfireRecipe<V>>
    {
        @Nonnull
        V generate(@Nonnull final ItemStack outputIn, @Nonnull final Predicate<ItemStack> woodTypesIn,
                   @Nonnull final Object inputIn, final int cookTimeIn, final float experienceIn);
    }
}
