package git.jbredwards.campfire.common.recipe.campfire;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
@Immutable
public class CampfireRecipe
{
    @Nonnull public final ResourceLocation id;
    @Nonnull public final List<ItemStack> inputs;
    @Nonnull public final ItemStack output;

    public final int cookTime;
    public final float experience;

    protected CampfireRecipe(@Nonnull ResourceLocation id, @Nonnull List<ItemStack> inputs, @Nonnull ItemStack output, int cookTime, float experience) {
        this.id = id;
        this.inputs = ImmutableList.copyOf(inputs);
        this.output = output;
        this.cookTime = cookTime;
        this.experience = experience;
    }

    public boolean canAccept(@Nonnull ItemStack in) {
        for(ItemStack input : inputs)
            if(ItemHandlerHelper.canItemStacksStack(in, input)) return true;

        return false;
    }
}
