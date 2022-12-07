package git.jbredwards.campfire.common.recipe.campfire;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
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

    public boolean canAccept(@Nonnull ItemStack in) {
        for(ItemStack input : inputs) if(ItemHandlerHelper.canItemStacksStack(in, input)) return true;
        return false;
    }
}
