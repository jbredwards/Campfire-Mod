package git.jbredwards.campfire.common.config;

import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipeHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jbred
 *
 */
@Config(modid = "campfire")
public final class CampfireConfigHandler
{
    @Nonnull
    static final List<ItemStack> TYPES = new ArrayList<>();

    @Nonnull
    public static List<ItemStack> getAllTypes() { return Collections.unmodifiableList(TYPES); }

    public static void buildTypes() {
        //autogenerate campfire logs based on oredict
        final NonNullList<ItemStack> types = NonNullList.create();
        OreDictionary.getOres("logWood").forEach(stack -> {
            if(stack.getMetadata() != OreDictionary.WILDCARD_VALUE) types.add(stack);
            else stack.getItem().getSubItems(CreativeTabs.SEARCH, types);
        });

        TYPES.clear();
        TYPES.addAll(types);
    }

    public static void buildRecipes() {
        //autogenerate campfire recipes based on furnace food recipes
        final Map<ItemStack, ItemStack> furnaceRecipes = FurnaceRecipes.instance().getSmeltingList();
        furnaceRecipes.forEach((in, out) -> {
            if(in.getItem() instanceof ItemFood && out.getItem() instanceof ItemFood) {
                final NonNullList<ItemStack> inputs = NonNullList.create();
                if(in.getMetadata() != OreDictionary.WILDCARD_VALUE) inputs.add(in);
                else in.getItem().getSubItems(CreativeTabs.SEARCH, inputs);

                CampfireRecipeHandler.createRecipe(inputs, out, 400, FurnaceRecipes.instance().getSmeltingExperience(out));
            }
        });
    }
}
