package git.jbredwards.campfire.common.compat.jei.category;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import git.jbredwards.campfire.common.compat.jei.recipe.CampfireRecipeWrapper;
import git.jbredwards.campfire.common.init.ModItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.config.Constants;
import mezz.jei.plugins.vanilla.furnace.FurnaceRecipeCategory;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class CampfireCategory extends FurnaceRecipeCategory<CampfireRecipeWrapper>
{
    @Nonnull public static final String NAME = "campfire:campfire";

    @Nonnull public final LoadingCache<Integer, IDrawableAnimated> cachedArrows;
    @Nonnull public final IDrawable background, icon;

    public static CampfireCategory instance;
    protected CampfireCategory(@Nonnull IGuiHelper guiHelper) {
        super(guiHelper);
        cachedArrows = CacheBuilder.newBuilder()
                .maximumSize(25)
                .build(new CacheLoader<Integer, IDrawableAnimated>() {
                    @Override
                    public IDrawableAnimated load(@Nonnull Integer cookTime) {
                        return guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 82, 128, 24, 17)
                                .buildAnimated(cookTime, IDrawableAnimated.StartDirection.LEFT, false);
                    }
                });

        icon = guiHelper.createDrawableIngredient(new ItemStack(ModItems.CAMPFIRE));
        background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 186, 82, 34)
                .addPadding(0, 10, 0, 0)
                .build();
    }

    @Nonnull
    public static CampfireCategory getOrBuildInstance(@Nonnull IGuiHelper guiHelper) {
        return instance == null ? (instance = new CampfireCategory(guiHelper)) : instance;
    }

    @Nonnull
    @Override
    public String getUid() { return NAME; }

    @Nonnull
    @Override
    public String getModName() { return "Campfire"; }

    @Nonnull
    @Override
    public String getTitle() { return Translator.translateToLocal("gui.campfire.jei.category.campfire"); }

    @Nonnull
    @Override
    public IDrawable getBackground() { return background; }

    @Nonnull
    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) { animatedFlame.draw(minecraft, 1, 20); }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull CampfireRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        final IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        guiItemStacks.init(inputSlot, true, 0, 0);
        guiItemStacks.init(outputSlot, false, 60, 8);
        guiItemStacks.set(ingredients);
    }
}
