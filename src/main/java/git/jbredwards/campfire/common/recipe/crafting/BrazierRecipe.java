package git.jbredwards.campfire.common.recipe.crafting;

import git.jbredwards.campfire.common.item.ItemBlockColored;
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
public class BrazierRecipe extends ShapedOreRecipe
{
    @Nonnull
    protected final Item campfire;
    public BrazierRecipe(@Nonnull Item campfire, @Nonnull Item brazier) {
        super(null, brazier, "III", "ICI", "III", 'I', "nuggetIron", 'C', campfire);
        this.campfire = campfire;
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

        return ItemBlockColored.applyColor(super.getCraftingResult(inv), ItemBlockColored.getColor(input));
    }
}
