package git.jbredwards.campfire.common.recipe.campfire;

import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.*;

/**
 *
 * @author jbred
 *
 */
public final class CampfireRecipeHandler
{
    @Nonnull
    static final List<CampfireRecipe> RECIPES = new ArrayList<>();

    @Nonnull
    public static Optional<CampfireRecipe> getFromInput(@Nonnull ItemStack in, @Nonnull ItemStack campfireTypeIn) {
        if(in.isEmpty()) return Optional.empty();
        for(CampfireRecipe recipe : RECIPES)
            if(recipe.canAccept(in, campfireTypeIn))
                return Optional.of(recipe);

        return Optional.empty();
    }

    public static void removeInput(@Nonnull ItemStack in) {
        getFromInput(in, ItemStack.EMPTY).ifPresent(RECIPES::remove);
    }

    public static void removeOutput(@Nonnull ItemStack out) {
        RECIPES.removeIf(recipe -> ItemHandlerHelper.canItemStacksStack(out, recipe.output));
    }

    public static void createRecipe(@Nonnull List<ItemStack> inputs, @Nonnull ItemStack output, int cookTime, float experience) {
        createRecipe(CampfireConfigHandler.getAllTypes(), inputs, output, cookTime, experience);
    }

    public static void createRecipe(@Nonnull List<ItemStack> campfireTypes, @Nonnull List<ItemStack> inputsIn, @Nonnull ItemStack output, int cookTime, float experience) {
        if(!output.isEmpty()) {
            final List<ItemStack> inputs = new ArrayList<>();
            for(Iterator<ItemStack> it = inputsIn.iterator(); it.hasNext();) {
                final ItemStack input = it.next();
                if(input.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                    it.remove();

                    final NonNullList<ItemStack> tabItems = NonNullList.create();
                    input.getItem().getSubItems(CreativeTabs.SEARCH, tabItems);
                    inputs.addAll(tabItems);
                }
            }

            inputs.addAll(inputsIn);
            inputs.forEach(CampfireRecipeHandler::removeInput);
            RECIPES.add(new CampfireRecipe(campfireTypes, inputs, output, cookTime, experience));
        }
    }

    @Nonnull
    public static List<CampfireRecipe> getAll() { return Collections.unmodifiableList(RECIPES); }
}
