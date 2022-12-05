package git.jbredwards.campfire.common.compat.jei.recipe;

import git.jbredwards.campfire.common.compat.jei.category.CampfireCategory;
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
public class CampfireRecipeWrapper extends SmeltingRecipe
{
    public final int cookTime;
    public final float experience;

    public CampfireRecipeWrapper(@Nonnull List<ItemStack> inputs, @Nonnull ItemStack output, int cookTime, float experience) {
        super(inputs, output);
        this.cookTime = cookTime;
        this.experience = experience;
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        CampfireCategory.instance.cachedArrows.getUnchecked(cookTime > 0 ? cookTime : 400).draw(minecraft, 24, 18);

        if(experience > 0) {
            final String experienceString = Translator.translateToLocalFormatted("gui.campfire.jei.category.campfire.experience", experience);
            minecraft.fontRenderer.drawString(experienceString, recipeWidth - minecraft.fontRenderer.getStringWidth(experienceString), 0, 0xFF808080);
        }

        if(cookTime > 0) {
            final String cookTimeString = Translator.translateToLocalFormatted("gui.campfire.jei.category.campfire.cookTime", cookTime / 20f);
            minecraft.fontRenderer.drawString(cookTimeString, recipeWidth - minecraft.fontRenderer.getStringWidth(cookTimeString), 45, 0xFF808080);
        }
    }
}
