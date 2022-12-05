package git.jbredwards.campfire;

import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipeHandler;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 *
 * @author jbred
 *
 */
@Mod(modid = "campfire", name = "Campfire", version = "1.0.0")
public final class Campfire
{
    //dummy recipes for testing
    @Mod.EventHandler
    static void init(@Nonnull FMLInitializationEvent event) {
        CampfireRecipeHandler.createRecipe(
                new ResourceLocation("campfire", "test"),
                Collections.singletonList(new ItemStack(Items.PORKCHOP)),
                new ItemStack(Items.COOKED_PORKCHOP), 400, 0);
    }
}
