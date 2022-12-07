package git.jbredwards.campfire.common.compat.jei.category;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import git.jbredwards.campfire.common.capability.ICampfireType;
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
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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

    public static final List<Object> catalysts = new ArrayList<>();
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
        background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 114, 82, 54);
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
    public void drawExtras(@Nonnull Minecraft minecraft) {
        staticFlame.draw(minecraft, 1, 20);
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull CampfireRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        final IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        guiItemStacks.init(inputSlot, true, 0, 0);
        guiItemStacks.init(outputSlot, false, 60, 18);
        guiItemStacks.set(ingredients);

        final List<ItemStack> campfires = new ArrayList<>();
        for(Object catalyst : catalysts) {
            if(catalyst instanceof ItemStack) {
                final ICampfireType type = ICampfireType.get((ItemStack)catalyst);
                if(type != null) for(ItemStack campfireType : recipeWrapper.campfireTypes) {
                    if(ItemHandlerHelper.canItemStacksStack(type.get(), campfireType)) {
                        campfires.add((ItemStack)catalyst);
                        break;
                    }
                }
            }
        }

        guiItemStacks.init(fuelSlot, false, 0, 36);
        guiItemStacks.set(fuelSlot, campfires);
    }
}
