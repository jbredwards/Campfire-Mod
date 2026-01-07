/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.recipe.ingredient;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An {@link Ingredient} that wraps around an {@link ItemStack} collection, which allows this ingredient to account for
 * any changes made to the collection post-ingredient-creation. But some methods may have worse performance than using a
 * normal ingredient, as they now have to be calculated dynamically during each call.
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public abstract class WrappedIngredient extends Ingredient
{
    public static class Collection extends WrappedIngredient
    {
        @Nonnull
        public final Iterable<ItemStack> mutableStacks;
        public Collection(@Nonnull final Iterable<ItemStack> stacksIn) { mutableStacks = stacksIn; }

        @Nonnull
        @Override
        public Stream<ItemStack> stream() { return StreamSupport.stream(mutableStacks.spliterator(), false); }
    }

    public static class Registry<V extends IForgeRegistryEntry<V>> extends WrappedIngredient
    {
        @Nonnull public final IForgeRegistry<V> registry;
        @Nonnull public final Function<V, ItemStack> stackFunction;

        public Registry(@Nonnull final IForgeRegistry<V> registryIn, @Nonnull final Function<V, ItemStack> stackFunctionIn) {
            registry = registryIn;
            stackFunction = stackFunctionIn;
        }

        @Nonnull
        @Override
        public Stream<ItemStack> stream() { return registry.getValuesCollection().stream().map(stackFunction); }
    }

    @Nonnull
    @Override
    public ItemStack[] getMatchingStacks() {
        return stream().flatMap(s -> {
            if(s.getMetadata() != OreDictionary.WILDCARD_VALUE) return Stream.of(s);
            @Nonnull final NonNullList<ItemStack> tabStacks = NonNullList.create();

            s.getItem().getSubItems(CreativeTabs.SEARCH, tabStacks);
            return tabStacks.stream();
        }).toArray(ItemStack[]::new);
    }

    @Nonnull
    @Override
    public IntList getValidItemStacksPacked() {
        return new IntArrayList(stream().flatMapToInt(s -> {
            if(s.getMetadata() != OreDictionary.WILDCARD_VALUE) return IntStream.of(RecipeItemHelper.pack(s));
            @Nonnull final NonNullList<ItemStack> tabStacks = NonNullList.create();

            s.getItem().getSubItems(CreativeTabs.SEARCH, tabStacks);
            return tabStacks.stream().mapToInt(RecipeItemHelper::pack);
        }).toArray());
    }

    @Override
    public boolean apply(@Nullable final ItemStack input) {
        return input != null && stream().anyMatch(s -> OreDictionary.itemMatches(s, input, false));
    }

    @Override
    public boolean isSimple() { return stream().noneMatch(s -> s.getItem().isDamageable()); }

    @Nonnull
    public abstract Stream<ItemStack> stream();
}
