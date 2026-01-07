/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public class SearchableGuiConfig extends GuiConfig
{
    // ------------
    // constructors
    // ------------

    public SearchableGuiConfig(@Nonnull final GuiScreen parentScreen,
                               @Nonnull final ISearchableGuiFactory modid,
                               @Nonnull final String title) {
        this(parentScreen, modid, false, false, title, ConfigManager.getModConfigClasses(modid.getModID()));
    }

    public SearchableGuiConfig(@Nonnull final GuiScreen parentScreen,
                               @Nonnull final ISearchableGuiFactory modID,
                               final boolean allRequireWorldRestart, final boolean allRequireMcRestart,
                               @Nonnull final String title,
                               @Nonnull final Class<?>... configClasses) {
        this(parentScreen, configClasses.length == 1
                ? ConfigElement.from(configClasses[0]).getChildElements().stream().sorted(Comparator.comparing(e -> I18n.format(e.getLanguageKey()))).collect(Collectors.toList())
                : Arrays.stream(configClasses).map(ConfigElement::from).sorted(Comparator.comparing(e -> I18n.format(e.getLanguageKey()))).collect(Collectors.toList()),
                modID, null, allRequireWorldRestart, allRequireMcRestart, title, null);
    }

    public SearchableGuiConfig(@Nonnull final GuiScreen parentScreen, @Nonnull final List<IConfigElement> configElements,
                               @Nonnull final ISearchableGuiFactory modID, @Nullable final String configID,
                               final boolean allRequireWorldRestart, final boolean allRequireMcRestart,
                               @Nonnull final String title) {
        this(parentScreen, configElements, modID, configID, allRequireWorldRestart, allRequireMcRestart, title, null);
    }

    public SearchableGuiConfig(@Nonnull final GuiScreen parentScreen, @Nonnull final List<IConfigElement> configElements,
                               @Nonnull final ISearchableGuiFactory modID,
                               final boolean allRequireWorldRestart, final boolean allRequireMcRestart,
                               @Nonnull final String title) {
        this(parentScreen, configElements, modID, null, allRequireWorldRestart, allRequireMcRestart, title, null);
    }

    public SearchableGuiConfig(@Nonnull final GuiScreen parentScreen, @Nonnull final List<IConfigElement> configElements,
                               @Nonnull final ISearchableGuiFactory modID,
                               final boolean allRequireWorldRestart, final boolean allRequireMcRestart,
                               @Nonnull final String title, @Nullable final String titleLine2) {
        this(parentScreen, configElements, modID, null, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
    }

    public SearchableGuiConfig(@Nonnull final GuiScreen parentScreen, @Nonnull final List<IConfigElement> configElements,
                               @Nonnull final ISearchableGuiFactory modID, @Nullable final String configID,
                               final boolean allRequireWorldRestart, final boolean allRequireMcRestart,
                               @Nonnull final String title, @Nullable final String titleLine2) {
        super(parentScreen, configElements, modID.getModID(), configID, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
        guiFactory = modID;
        entryList = new SearchableGuiConfigEntries(this, mc);
    }

    // ------------
    // actual stuff
    // ------------

    @Nonnull
    public final ISearchableGuiFactory guiFactory;

    @Override
    public void initGui() {
        @Nonnull final GuiTextField searchBar = guiFactory.getSearchBar();
        searchBar.x = (width >> 1) - (searchBar.width >> 1);
        searchBar.y = height - 50;

        super.initGui();
        if(!(entryList instanceof SearchableGuiConfigEntries)) ((SearchableGuiConfigEntries)(entryList = new SearchableGuiConfigEntries(this, mc))).initGui();
    }
}
