package git.jbredwards.campfire.common.init;

import git.jbredwards.campfire.client.renderer.model.ModelCampfireInvWrapper;
import git.jbredwards.campfire.client.renderer.model.ModelCampfireLogs;
import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        event.getRegistry().register(ModBlocks.CAMPFIRE.setCreativeTab(CreativeTabs.DECORATIONS).setRegistryName("campfire").setTranslationKey("campfire:campfire"));
        TileEntity.register("campfire:campfire", TileEntityCampfire.class);
    }

    @SubscribeEvent
    static void registerItem(@Nonnull RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ModItems.CAMPFIRE.setRegistryName("campfire"));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    static void registerModels(@Nonnull ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(ModelCampfireInvWrapper.Loader.INSTANCE);
        ModelLoaderRegistry.registerLoader(ModelCampfireLogs.Loader.INSTANCE);
        ModelLoader.setCustomModelResourceLocation(ModItems.CAMPFIRE, 0,
                new ModelResourceLocation(String.valueOf(ModItems.CAMPFIRE.getRegistryName()), "inventory"));
    }
}
