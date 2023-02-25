package git.jbredwards.campfire.common.compat.futuremc;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryModifiable;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = "campfire")
final class FutureMCHandler
{
    @GameRegistry.ObjectHolder("futuremc:campfire") public static Block FUTURE_MC_CAMPFIRE = null;
    @GameRegistry.ObjectHolder("futuremc:campfire") public static Item FUTURE_MC_CAMPFIRE_ITEM = null;

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent(priority = EventPriority.LOW)
    static void removeFutureMCCampfireRecipe(@Nonnull RegistryEvent.Register<IRecipe> event) {
        if(FUTURE_MC_CAMPFIRE != null) {
            FUTURE_MC_CAMPFIRE.setCreativeTab(null);
            if(event.getRegistry() instanceof IForgeRegistryModifiable)
                ((IForgeRegistryModifiable<IRecipe>)event.getRegistry()).remove(new ResourceLocation("futuremc", "else/campfire"));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void warnFutureMCCampfireDeprecation(@Nonnull ItemTooltipEvent event) {
        if(FUTURE_MC_CAMPFIRE_ITEM != null && event.getItemStack().getItem() == FUTURE_MC_CAMPFIRE_ITEM)
            event.getToolTip().add(I18n.format("tooltip.campfire.futuremc.deprecated"));
    }
}
