/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.recipe.crafting;

import git.jbredwards.campfire.mod.common.block.AbstractCampfire;
import git.jbredwards.campfire.mod.common.item.ItemBlockColored;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class BrazierCraftingRecipe extends ShapedOreRecipe
{
    @Nonnull
    public final Item campfire;
    public BrazierCraftingRecipe(@Nonnull Item campfireIn, @Nonnull Item brazier) {
        super(null, brazier, "III", "ICI", "III", 'I', "nuggetIron", 'C', campfireIn);
        campfire = campfireIn;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        if(((AbstractCampfire)Block.getBlockFromItem(output.getItem())).campfireSettings.unlitOnCraft()) {
            final ItemStack unlit = super.getRecipeOutput().copy();
            unlit.setItemDamage(1);
            return unlit;
        }

        return super.getRecipeOutput();
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        ItemStack input = ItemStack.EMPTY;
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            final ItemStack stack = inv.getStackInSlot(i);
            if(stack.getItem() == campfire) {
                input = stack;
                break;
            }
        }

        final ItemStack result = super.getCraftingResult(inv);
        result.setItemDamage(input.getMetadata());
        return ItemBlockColored.applyColor(result, ItemBlockColored.getColor(input));
    }
}
