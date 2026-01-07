/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.recipe.ingredient;

import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Public version of forge's {@link net.minecraftforge.common.crafting.CompoundIngredient} class.
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public class CompoundIngredient extends net.minecraftforge.common.crafting.CompoundIngredient
{
    public CompoundIngredient(@Nonnull final Collection<Ingredient> children) { super(children); }

    /**
     * @param ingredients The ingredients to be compounded. Objects can be a: Block, Item, ItemStack, String (for oredict), or Ingredient.
     * @return A new {@link CompoundIngredient}, using {@link CraftingHelper#getIngredient(Object) CraftingHelper::getIngredient}
     * to generate {@link Ingredient Ingredients} from the ingredients array.
     * @throws NullPointerException If ingredients is null.
     *
     * @since 2.0.0
     * @author jbred
     */
    @Nonnull
    public static CompoundIngredient from(@Nonnull final Object... ingredients) {
        return new CompoundIngredient(Arrays.stream(ingredients).map(CraftingHelper::getIngredient).collect(Collectors.toList()));
    }
}
