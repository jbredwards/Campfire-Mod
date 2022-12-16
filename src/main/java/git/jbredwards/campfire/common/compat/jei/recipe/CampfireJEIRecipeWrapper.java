package git.jbredwards.campfire.common.compat.jei.recipe;

import git.jbredwards.campfire.common.compat.jei.category.CampfireJEICategory;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipe;
import mezz.jei.plugins.vanilla.furnace.SmeltingRecipe;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public class CampfireJEIRecipeWrapper extends SmeltingRecipe
{
    public final int cookTime;
    public final float experience;
    public final List<ItemStack> campfireTypes;

    public CampfireJEIRecipeWrapper(@Nonnull CampfireRecipe recipe) {
        super(recipe.inputs, recipe.output);
        cookTime = recipe.cookTime;
        experience = recipe.experience;
        campfireTypes = recipe.campfireTypes;
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        CampfireJEICategory.instance.cachedArrows.getUnchecked(cookTime).draw(minecraft, 24, 18);

        if(experience > 0) {
            final String experienceString = Translator.translateToLocalFormatted("gui.campfire.jei.category.campfire.experience", experience);
            minecraft.fontRenderer.drawString(experienceString, recipeWidth - minecraft.fontRenderer.getStringWidth(experienceString), 0, 0xFF808080);
        }

        final String cookTimeString = Translator.translateToLocalFormatted("gui.campfire.jei.category.campfire.cookTime", cookTime / 20f);
        minecraft.fontRenderer.drawString(cookTimeString, recipeWidth - minecraft.fontRenderer.getStringWidth(cookTimeString), 45, 0xFF808080);
    }
}
