/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.client.gui;

import git.jbredwards.campfire.api.config.ISearchableGuiFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public final class CampfireGuiFactory implements ISearchableGuiFactory
{
    GuiTextField searchBar;

    @Override
    public void initialize(@Nonnull final Minecraft mc) {
        searchBar = new GuiTextField(0, mc.fontRenderer, 0, 0, 256, 14);
        searchBar.setCanLoseFocus(true);
        // searchBar.setFocused(true);
    }

    @Nonnull
    @Override
    public String getModID() { return "campfire"; }

    @Nonnull
    @Override
    public GuiTextField getSearchBar() { return searchBar; }
}
