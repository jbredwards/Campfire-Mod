package git.jbredwards.campfire.common.recipe.campfire;

import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public class CampfireRecipe
{
    @Nonnull public final List<ItemStack> campfireTypes; //mutable
    @Nonnull public final List<ItemStack> inputs; //immutable
    @Nonnull public final ItemStack output;

    public final int cookTime;
    public final float experience;

    protected CampfireRecipe(@Nonnull List<ItemStack> campfireTypes, @Nonnull List<ItemStack> inputs, @Nonnull ItemStack output, int cookTime, float experience) {
        this.campfireTypes = new ArrayList<>(campfireTypes);
        this.inputs = Collections.unmodifiableList(inputs);
        this.output = output;
        this.cookTime = cookTime;
        this.experience = experience;
    }

    @Nonnull
    public static CampfireRecipe of(@Nonnull List<ItemStack> inputsIn, @Nonnull ItemStack output, int cookTime, float experience) {
        return of(CampfireConfigHandler.getAllTypes(), inputsIn, output, cookTime, experience);
    }

    @Nonnull
    public static CampfireRecipe of(@Nonnull List<ItemStack> campfireTypes, @Nonnull List<ItemStack> inputsIn, @Nonnull ItemStack output, int cookTime, float experience) {
        final List<ItemStack> inputs = new ArrayList<>();
        for(Iterator<ItemStack> it = inputsIn.iterator(); it.hasNext();) {
            final ItemStack input = it.next();
            if(input.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                it.remove();

                final NonNullList<ItemStack> tabItems = NonNullList.create();
                input.getItem().getSubItems(CreativeTabs.SEARCH, tabItems);

                //no duplicate stacks in recipe input
                tabItems.forEach(stack -> {
                    if(!CampfireConfigHandler.isStackInList(inputs, stack)) inputs.add(stack);
                });
            }

            //no duplicate stacks in recipe input
            else if(CampfireConfigHandler.isStackInList(inputs, input)) it.remove();
        }

        inputs.addAll(inputsIn);
        inputs.removeIf(ItemStack::isEmpty);
        return new CampfireRecipe(campfireTypes, inputs, output, cookTime, experience);
    }

    //when campfire type is empty, skip type check
    public boolean canAccept(@Nonnull ItemStack in, @Nonnull ItemStack campfireTypeIn) {
        boolean isValidType = campfireTypeIn.isEmpty();
        if(!isValidType) {
            for(ItemStack campfireType : campfireTypes) {
                if(ItemHandlerHelper.canItemStacksStack(campfireTypeIn, campfireType)) {
                    isValidType = true;
                    break;
                }
            }
        }

        if(isValidType) {
            for(ItemStack input : inputs) {
                if(ItemHandlerHelper.canItemStacksStack(in, input)) {
                    return true;
                }
            }
        }

        return false;
    }
}
