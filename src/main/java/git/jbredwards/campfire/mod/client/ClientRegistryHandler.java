/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.client;

import git.jbredwards.campfire.mod.client.renderer.model.ModelCampfireFire;
import git.jbredwards.campfire.mod.client.renderer.model.ModelCampfireInvWrapper;
import git.jbredwards.campfire.mod.client.renderer.model.ModelCampfireLogs;
import git.jbredwards.campfire.mod.common.block.BlockColorEmitting;
import git.jbredwards.campfire.mod.common.init.CampfireItems;
import git.jbredwards.campfire.mod.common.item.ItemBlockColored;
import git.jbredwards.campfire.mod.common.tileentity.TileEntityColorEmitting;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    static void registerColors(@Nonnull final ColorHandlerEvent.Item event) {
        ForgeRegistries.ITEMS.getValuesCollection().stream().filter(item -> Block.getBlockFromItem(item) instanceof BlockColorEmitting).forEach(item -> {
            // color item
            event.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
                if(tintIndex < 5 || stack.getSubCompound("display") == null) return -1;
                else if(stack.getSubCompound("display").getBoolean("campfire:rainbow")) {
                    @Nonnull final Minecraft mc = Minecraft.getMinecraft();

                    final int speed = 25;
                    final int index = (int)(Minecraft.getSystemTime() / 50 / speed) + stack.hashCode();
                    final float p = (Minecraft.getSystemTime() / 50 % speed + mc.getRenderPartialTicks()) / speed;

                    @Nonnull final float[] prev = EnumDyeColor.byMetadata(index % EnumDyeColor.values().length).getColorComponentValues();
                    @Nonnull final float[] next = EnumDyeColor.byMetadata((index + 1) % EnumDyeColor.values().length).getColorComponentValues();
                    return new Color(prev[0] * (1 - p) + next[0] * p, prev[1] * (1 - p) + next[1] * p, prev[2] * (1 - p) + next[2] * p).brighter().getRGB();
                }

                else return new Color(ItemBlockColored.getColor(stack)).brighter().getRGB();
            }, item);

            // color block
            event.getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> {
                if(tintIndex >= 5 && world != null && pos != null) {
                    @Nullable final TileEntity tile = world.getTileEntity(pos);
                    if(tile instanceof TileEntityColorEmitting) return new Color(((TileEntityColorEmitting)tile).color).brighter().getRGB();
                }

                return -1;
            }, Block.getBlockFromItem(item));
        });
    }

    @SubscribeEvent
    static void registerModels(@Nonnull final ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(ModelCampfireInvWrapper.Loader.INSTANCE);
        ModelLoaderRegistry.registerLoader(ModelCampfireFire.Loader.INSTANCE);
        ModelLoaderRegistry.registerLoader(ModelCampfireLogs.Loader.INSTANCE);

        ModelLoader.setCustomModelResourceLocation(CampfireItems.BRAZIER, 0, new ModelResourceLocation("campfire:brazier", "inventory"));
        ModelLoader.setCustomModelResourceLocation(CampfireItems.BRAZIER, 1, new ModelResourceLocation("campfire:brazier", "inventory_unlit"));

        ModelLoader.setCustomModelResourceLocation(CampfireItems.CAMPFIRE, 0, new ModelResourceLocation("campfire:campfire", "inventory"));
        ModelLoader.setCustomModelResourceLocation(CampfireItems.CAMPFIRE, 1, new ModelResourceLocation("campfire:campfire", "inventory_unlit"));

        ModelLoader.setCustomModelResourceLocation(CampfireItems.CAMPFIRE_ASH, 0, new ModelResourceLocation("campfire:campfire_ash", "inventory"));
        ModelLoader.setCustomModelResourceLocation(CampfireItems.CHAIN, 0, new ModelResourceLocation("campfire:chain", "inventory"));
        ModelLoader.setCustomModelResourceLocation(CampfireItems.LANTERN, 0, new ModelResourceLocation("campfire:lantern", "inventory"));
    }

    @SubscribeEvent
    static void registerTextures(@Nonnull final TextureStitchEvent.Pre event) {
        if(event.getMap() == Minecraft.getMinecraft().getTextureMapBlocks()) {
            event.getMap().registerSprite(new ResourceLocation("campfire", "particles/colored_lava"));
            // register each frame of the campfire smoke particle
            for(int i = 0; i < 12; i++) event.getMap().registerSprite(new ResourceLocation("campfire", String.format("particles/big_smoke_%d", i)));
        }
    }
}
