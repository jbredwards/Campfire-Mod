/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public interface ISearchableGuiFactory extends IModGuiFactory
{
    @Nonnull
    String getModID();

    @Nonnull
    GuiTextField getSearchBar();

    @Override
    default boolean hasConfigGui() { return true; }

    @Nonnull
    @Override
    default GuiScreen createConfigGui(@Nonnull final GuiScreen parentScreen) {
        return new SearchableGuiConfig(parentScreen, this, I18n.format("configgui." + getModID() + ".configTitle"));
    }

    // NO-OP
    @Nullable
    @Override
    default Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }
}
