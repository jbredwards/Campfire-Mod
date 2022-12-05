package git.jbredwards.campfire.common.init;

import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = "campfire")
public final class RegistryHandler
{
    @SubscribeEvent
    static void registerBlock(@Nonnull RegistryEvent.Register<Block> event) {
        event.getRegistry().register(ModBlocks.CAMPFIRE.setRegistryName("campfire").setTranslationKey("campfire:campfire"));
        TileEntity.register("campfire:campfire", TileEntityCampfire.class);
    }

    @SubscribeEvent
    static void registerItem(@Nonnull RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ModItems.CAMPFIRE.setRegistryName("campfire"));
    }


}
