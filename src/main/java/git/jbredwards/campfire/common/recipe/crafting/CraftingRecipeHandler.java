package git.jbredwards.campfire.common.recipe.crafting;

import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.init.CampfireItems;
import net.minecraft.init.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 * auto-generates the campfire crafting recipes
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = "campfire")
final class CraftingRecipeHandler
{
    @SubscribeEvent(priority = EventPriority.HIGH)
    static void buildTypes(@Nonnull RegistryEvent.Register<IRecipe> event) { CampfireConfigHandler.buildTypes(); }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    static void registerRecipes(@Nonnull RegistryEvent.Register<IRecipe> event) {
        CampfireConfigHandler.getAllTypes().forEach(type -> event.getRegistry().register(
                new CampfireCraftingRecipe(type, Ingredient.fromItem(Items.COAL), CampfireItems.CAMPFIRE)));

        event.getRegistry().register(new ColoredCampfireRecipe().setRegistryName("dyeCampfires"));
        event.getRegistry().register(new BrazierCraftingRecipe(CampfireItems.CAMPFIRE, CampfireItems.BRAZIER));
    }
}
