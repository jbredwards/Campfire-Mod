/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.creativetab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Serves as a base for any fully textured creative tab, with a search bar and a changing icon.
 *
 * @since 2.0.0
 * @author jbred
 *
 */
public abstract class TexturedCreativeTab extends CreativeTabs
{
    @Nonnull public final NonNullList<ItemStack> icons = NonNullList.create();
    @Nonnull public final String modid;

    protected TexturedCreativeTab(@Nonnull final String modidIn) {
        super(modidIn + ".tab");
        modid = modidIn;
    }

    @Override
    public abstract int getLabelColor();

    @Nonnull
    @SideOnly(Side.CLIENT)
    public ResourceLocation getTabsImage() {
        return new ResourceLocation(modid, "textures/gui/container/creative_inventory/tabs.png");
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getBackgroundImage() {
        return new ResourceLocation(modid, "textures/gui/container/creative_inventory/items.png");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public final boolean hasScrollbar() {
        applyTabsTexture();
        return super.hasScrollbar();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public final int getColumn() {
        applyTabsTexture();
        return super.getColumn();
    }

    // this is a very hacky way to apply the custom tabs texture, will probably be improved in the future
    @SideOnly(Side.CLIENT)
    protected final void applyTabsTexture() {
        @Nonnull final TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        @Nullable final ITextureObject texture = manager.getTexture(GuiContainerCreative.CREATIVE_INVENTORY_TABS);
        // ensure the currently applied texture is the one that needs to be replaced
        // noinspection ConstantValue
        if(texture != null && texture.getGlTextureId() == GlStateManager.textureState[GlStateManager.activeTextureUnit].textureName) manager.bindTexture(getTabsImage());
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public final ItemStack getIcon() {
        if(icons.isEmpty()) {
            buildIcons();
            if(icons.isEmpty()) icons.add(ItemStack.EMPTY);
        }

        return icons.get(((Minecraft.getMinecraft().ingameGUI.getUpdateCounter() + (getIndex() << 7)) >> 5) % icons.size());
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public final ItemStack createIcon() { return getIcon(); }

    @Override
    public final boolean hasSearchBar() { return true; }

    @SideOnly(Side.CLIENT)
    protected void buildIcons() { super.displayAllRelevantItems(icons); }
}
