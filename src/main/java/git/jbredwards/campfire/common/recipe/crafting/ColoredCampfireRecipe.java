package git.jbredwards.campfire.common.recipe.crafting;

import git.jbredwards.campfire.common.item.ItemBlockColored;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.DyeUtils;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public class ColoredCampfireRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
    public int getResultDyeColor(int baseColor, @Nonnull List<EnumDyeColor> dyes) {
        float r = baseColor != -1 ? (baseColor >> 16 & 0xFF) / 255f : 0;
        float g = baseColor != -1 ? (baseColor >> 8 & 0xFF) / 255f : 0;
        float b = baseColor != -1 ? (baseColor & 0xFF) / 255f : 0;

        for(EnumDyeColor dye : dyes) {
            final float[] rgb = dye.getColorComponentValues();
            r += rgb[0];
            g += rgb[1];
            b += rgb[2];
        }

        final int size = dyes.size() + (baseColor != -1 ? 1 : 0);
        return new Color(r / size, g / size, b / size).getRGB();
    }

    public boolean isDyeableTarget(@Nonnull ItemStack stack) {
        return ArrayUtils.contains(OreDictionary.getOreIDs(stack), OreDictionary.getOreID("campfire"));
    }

    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
        boolean foundDyeable = false, foundDye = false;

        for(int i = 0; i < inv.getSizeInventory(); i++) {
            final ItemStack stack = inv.getStackInSlot(i);
            if(!stack.isEmpty()) {
                if(!foundDyeable && isDyeableTarget(stack)) {
                    foundDyeable = true;
                    continue;
                }

                if(DyeUtils.isDye(stack)) {
                    foundDye = true;
                    continue;
                }

                return false;
            }
        }

        return foundDyeable && foundDye;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        final List<EnumDyeColor> dyes = new ArrayList<>();
        ItemStack dyeable = ItemStack.EMPTY;

        for(int i = 0; i < inv.getSizeInventory(); i++) {
            final ItemStack stack = inv.getStackInSlot(i);
            if(!stack.isEmpty()) {
                if(dyeable.isEmpty() && isDyeableTarget(stack)) {
                    dyeable = ItemHandlerHelper.copyStackWithSize(stack, 1);
                    continue;
                }

                else if(DyeUtils.isDye(stack)) {
                    final Optional<EnumDyeColor> color = DyeUtils.colorFromStack(stack);
                    if(color.isPresent()) {
                        dyes.add(color.get());
                        continue;
                    }
                }

                //should never pass
                return getRecipeOutput();
            }
        }

        return ItemBlockColored.applyColor(dyeable, getResultDyeColor(ItemBlockColored.getColor(dyeable), dyes));
    }

    @Override
    public boolean isDynamic() { return true; }

    @Override
    public boolean canFit(int width, int height) { return width * height >= 2; }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() { return ItemStack.EMPTY; }
}
