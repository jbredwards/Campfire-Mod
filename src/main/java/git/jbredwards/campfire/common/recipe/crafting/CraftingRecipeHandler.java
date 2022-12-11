package git.jbredwards.campfire.common.recipe.crafting;

import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.init.CampfireItems;
import git.jbredwards.campfire.common.item.ItemCampfire;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

/**
 * auto generates the campfire crafting recipes
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = "campfire")
final class CraftingRecipeHandler
{
    @SubscribeEvent
    static void registerRecipes(@Nonnull RegistryEvent.Register<IRecipe> event) {
        CampfireConfigHandler.buildTypes();
        CampfireConfigHandler.getAllTypes().forEach(type -> event.getRegistry().register(
                new ShapedOreRecipe(
                        new ResourceLocation("campfire", "Campfire"),
                        ItemCampfire.applyType(CampfireItems.CAMPFIRE, type),
                        " S ", "SCS", "LLL",
                        'S', "stickWood",
                        'C', new ItemStack(Items.COAL, 1, OreDictionary.WILDCARD_VALUE),
                        'L', type)
                        .setRegistryName("campfire", String.format("campfire.%s.%s",
                                type.getItem().getCreatorModId(type), type.getTranslationKey()))));
    }
}
