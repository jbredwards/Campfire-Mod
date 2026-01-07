/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.recipe.crafting;

import git.jbredwards.campfire.mod.common.block.AbstractCampfire;
import git.jbredwards.campfire.mod.common.item.ItemCampfire;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class CampfireCraftingRecipe extends ShapedOreRecipe
{
    @Nonnull public Ingredient woodTypes;
    @Nonnull public ItemCampfire<?> campfire;
    @Nonnull public Object[] in;

    public CampfireCraftingRecipe(@Nonnull final ItemCampfire<?> campfireIn, @Nonnull final Ingredient woodTypesIn, @Nonnull final Object... recipeIn) {
        super(null, ItemStack.EMPTY, ArrayUtils.add(recipeIn, woodTypesIn));
        woodTypes = woodTypesIn;
        campfire = campfireIn;
        in = recipeIn;
    }

    @Override
    public boolean matches(@Nonnull final InventoryCrafting inv, @Nonnull final World world) {
        return super.matches(inv, world) && !getType(inv).isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull final InventoryCrafting inv) {
        @Nonnull final ItemStack type = getType(inv);
        if(type.isEmpty()) return ItemStack.EMPTY;

        else if(((AbstractCampfire<?>)campfire.getBlock()).campfireSettings.unlitOnCraft()) {
            @Nonnull final ItemStack unlit = campfire.applyType(type);
            unlit.setItemDamage(1);
            return unlit;
        }

        else return campfire.applyType(type);
    }

    @Nonnull
    public ItemStack getType(@Nonnull final IInventory inv) {
        @Nonnull ItemStack type = ItemStack.EMPTY;
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            @Nonnull final ItemStack invStack = inv.getStackInSlot(i);
            if(woodTypes.test(invStack)) {
                if(type.isEmpty()) type = invStack;
                else if(!type.isItemEqual(invStack)) return ItemStack.EMPTY;
            }
        }

        return type;
    }

    @Nonnull
    public ResourceLocation getGroupAsLocation() { return group; }

    @Override
    public boolean isDynamic() { return true; } // no recipe book for this recipe
}
