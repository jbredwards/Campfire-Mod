package git.jbredwards.campfire.client;

import git.jbredwards.campfire.client.renderer.model.ModelCampfireFire;
import git.jbredwards.campfire.client.renderer.model.ModelCampfireInvWrapper;
import git.jbredwards.campfire.client.renderer.model.ModelCampfireLogs;
import git.jbredwards.campfire.common.init.CampfireItems;
import git.jbredwards.campfire.common.item.ItemBlockColored;
import git.jbredwards.campfire.common.tileentity.AbstractCampfireTE;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = "campfire", value = Side.CLIENT)
final class ClientRegistryHandler
{
    @SubscribeEvent
    static void registerColors(@Nonnull ColorHandlerEvent.Item event) {
        OreDictionary.getOres("campfire", false).forEach(campfire -> {
            event.getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> {
                if(tintIndex >= 5 && world != null && pos != null) {
                    final TileEntity tile = world.getTileEntity(pos);
                    if(tile instanceof AbstractCampfireTE)
                        return new Color(((AbstractCampfireTE)tile).color).brighter().getRGB();
                }

                return -1;
            }, Block.getBlockFromItem(campfire.getItem()));

            event.getItemColors().registerItemColorHandler((stack, tintIndex) -> tintIndex >= 5
                    ? new Color(ItemBlockColored.getColor(stack)).brighter().getRGB() : -1, campfire.getItem());
        });
    }

    @SubscribeEvent
    static void registerModels(@Nonnull ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(ModelCampfireInvWrapper.Loader.INSTANCE);
        ModelLoaderRegistry.registerLoader(ModelCampfireFire.Loader.INSTANCE);
        ModelLoaderRegistry.registerLoader(ModelCampfireLogs.Loader.INSTANCE);

        ModelLoader.setCustomModelResourceLocation(CampfireItems.BRAZIER, 0, new ModelResourceLocation("campfire:brazier", "inventory"));
        ModelLoader.setCustomModelResourceLocation(CampfireItems.BRAZIER, 1, new ModelResourceLocation("campfire:brazier", "inventory_unlit"));

        ModelLoader.setCustomModelResourceLocation(CampfireItems.CAMPFIRE, 0, new ModelResourceLocation("campfire:campfire", "inventory"));
        ModelLoader.setCustomModelResourceLocation(CampfireItems.CAMPFIRE, 1, new ModelResourceLocation("campfire:campfire", "inventory_unlit"));
    }

    @SubscribeEvent
    static void registerTextures(@Nonnull TextureStitchEvent.Pre event) {
        if(event.getMap() == Minecraft.getMinecraft().getTextureMapBlocks()) {
            event.getMap().registerSprite(new ResourceLocation("campfire", "particles/colored_lava"));
            //register each frame of the campfire smoke particle
            for(int i = 0; i < 12; i++)
                event.getMap().registerSprite(new ResourceLocation("campfire",
                        String.format("particles/big_smoke_%d", i)));
        }
    }
}
