package git.jbredwards.campfire.common.compat.futuremc;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryModifiable;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = "campfire")
final class FutureMCHandler
{
    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    static void removeFutureMCCampfireRecipe(@Nonnull RegistryEvent.Register<IRecipe> event) {
        Optional.ofNullable(Block.getBlockFromName("futuremc:campfire")).ifPresent(block -> block.setCreativeTab(null));
        if(event.getRegistry() instanceof IForgeRegistryModifiable)
            ((IForgeRegistryModifiable<IRecipe>)event.getRegistry()).remove(new ResourceLocation("futuremc", "else/campfire"));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    static void warnFutureMCCampfireDeprecation(@Nonnull ItemTooltipEvent event) {
        if(String.valueOf(event.getItemStack().getItem().getRegistryName()).equals("futuremc:campfire"))
            event.getToolTip().add(I18n.format("tooltip.campfire.futuremc.deprecated"));
    }
}
