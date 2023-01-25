package git.jbredwards.campfire.common.recipe.crafting;

import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.item.ItemCampfire;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class CampfireCraftingRecipe extends ShapedOreRecipe
{
    public CampfireCraftingRecipe(@Nonnull ItemStack type, @Nonnull Ingredient fuel, @Nonnull Item campfire) {
        super(null, ItemCampfire.applyType(campfire, type), " S ", "SCS", "LLL", 'S', "stickWood", 'C', fuel, 'L', type);
        setRegistryName(String.format("%s.%s.%s",
                campfire.delegate.name().getPath(),
                type.getItem().getCreatorModId(type),
                type.getTranslationKey()));
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        final ItemStack result = super.getCraftingResult(inv);
        if(CampfireConfigHandler.unlitOnCraft) result.setItemDamage(1);
        return result;
    }
}
