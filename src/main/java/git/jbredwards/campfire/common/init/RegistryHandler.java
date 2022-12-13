package git.jbredwards.campfire.common.init;

import git.jbredwards.campfire.common.tileentity.TileEntityBrazier;
import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = "campfire")
final class RegistryHandler
{
    @SubscribeEvent
    static void registerBlock(@Nonnull RegistryEvent.Register<Block> event) {
        event.getRegistry().register(CampfireBlocks.BRAZIER.setRegistryName("brazier").setTranslationKey("campfire:brazier"));
        TileEntity.register("campfire:brazier", TileEntityBrazier.class);

        event.getRegistry().register(CampfireBlocks.CAMPFIRE.setRegistryName("campfire").setTranslationKey("campfire:campfire"));
        TileEntity.register("campfire:campfire", TileEntityCampfire.class);
    }

    @SubscribeEvent
    static void registerItem(@Nonnull RegistryEvent.Register<Item> event) {
        event.getRegistry().register(CampfireItems.BRAZIER.setRegistryName("brazier"));
        OreDictionary.registerOre("campfire", CampfireItems.BRAZIER);

        event.getRegistry().register(CampfireItems.CAMPFIRE.setRegistryName("campfire"));
        OreDictionary.registerOre("campfire", CampfireItems.CAMPFIRE);
    }

    @SubscribeEvent
    static void registerSounds(@Nonnull RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(CampfireSounds.CRACKLE.setRegistryName("blocks.campfire.crackle"));
    }
}
