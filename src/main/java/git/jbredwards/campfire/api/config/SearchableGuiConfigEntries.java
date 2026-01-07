/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.api.config;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public class SearchableGuiConfigEntries extends GuiConfigEntries
{
    @Nonnull public final ISearchableGuiFactory guiFactory;
    @Nonnull protected String lastFilterText = "";

    @Nonnull protected final List<String>[] nameKeys = new List[listEntries.size()];
    @Nonnull protected final List<String>[] tooltipKeys = new List[listEntries.size()];
    @Nonnull protected List<IConfigEntry> filteredEntries = Collections.unmodifiableList(listEntries);

    public SearchableGuiConfigEntries(@Nonnull final SearchableGuiConfig parent, @Nonnull final Minecraft mc) {
        super(parent, mc);
        guiFactory = parent.guiFactory;

        for(int i = 0; i < listEntries.size(); i++) {
            // transform list element
            @Nonnull final IConfigElement element = listEntries.get(i).getConfigElement();
            listEntries.set(i, listEntries.get(i) instanceof GuiConfigEntries.CategoryEntry ? new GuiConfigEntries.CategoryEntry(parent, this, element) {
                @Nonnull
                @Override
                protected GuiScreen buildChildScreen() {
                    return new SearchableGuiConfig(owningScreen, configElement.getChildElements(), ((SearchableGuiConfig)owningScreen).guiFactory,
                            owningScreen.allRequireWorldRestart || configElement.requiresWorldRestart(), owningScreen.allRequireMcRestart || configElement.requiresMcRestart(),
                            owningScreen.title, ((owningScreen.titleLine2 == null ? "" : owningScreen.titleLine2) + " > " + name));
                }

                @Override
                public boolean enabled() { return !configElement.getChildElements().isEmpty(); }
            } : new ComponentConfigEntry(listEntries.get(i), guiFactory.getModID()));

            // append search keys
            nameKeys[i] = new ArrayList<>();
            tooltipKeys[i] = new ArrayList<>();
            generateSearchKeys(Collections.singletonList(element), i);
        }

        bottom -= 36;
        if(!lastFilterText.equals(guiFactory.getSearchBar().getText())) updateFilteredEntries(guiFactory.getSearchBar().getText());
    }

    @Override
    protected void initGui() {
        super.initGui();
        bottom -= 36; // make room for search bar
    }

    protected void generateSearchKeys(@Nullable final List<IConfigElement> children, final int index) {
        if(children != null && !children.isEmpty()) for(@Nonnull final IConfigElement element : children) {
            nameKeys[index].add(I18n.format(element.getLanguageKey()).toLowerCase());
            tooltipKeys[index].add(I18n.format(element.getLanguageKey() + ".tooltip").toLowerCase());
            generateSearchKeys(element.getChildElements(), index);
        }
    }

    public void updateFilteredEntries(@Nonnull final String word) {
        if(word.isEmpty()) {
            lastFilterText = word;
            filteredEntries = Collections.unmodifiableList(listEntries);
            return;
        }

        lastFilterText = word.toLowerCase();
        filteredEntries = Arrays.stream(lastFilterText.split("\\s*\\|\\s*"))
                .flatMapToInt(s -> {
                    if(s.isEmpty()) return IntStream.range(0, listEntries.size());

                    @Nonnull final IntStream.Builder builder = IntStream.builder();
                    @Nonnull final List<String>[] keys = s.charAt(0) == '#' ? tooltipKeys : nameKeys;
                    @Nonnull final String term = keys == tooltipKeys ? s.substring(1) : s;

                    for(int i = 0; i < keys.length; i++) if(keys[i].stream().anyMatch(k -> k.contains(term))) builder.add(i);
                    return builder.build();
                })
                .distinct().sorted().mapToObj(listEntries::get)
                .collect(ImmutableList.toImmutableList());
    }

    @Nonnull
    @Override
    public IConfigEntry getListEntry(final int index) { return filteredEntries.get(index); }

    @Override
    public int getSize() { return filteredEntries.size(); }

    @Override
    public void keyTyped(final char eventChar, final int eventKey) {
        if(!guiFactory.getSearchBar().textboxKeyTyped(eventChar, eventKey)) filteredEntries.forEach(e -> e.keyTyped(eventChar, eventKey));
    }

    @Override
    public boolean mouseClicked(final int mouseX, final int mouseY, final int mouseEvent) {
        return guiFactory.getSearchBar().mouseClicked(mouseX, mouseY, mouseEvent) || super.mouseClicked(mouseX, mouseY, mouseEvent);
    }

    @Override
    public void mouseClickedPassThru(final int mouseX, final int mouseY, final int mouseEvent) {
        filteredEntries.forEach(e -> e.mouseClicked(mouseX, mouseY, mouseEvent));
    }

    @Override
    public void drawScreenPost(final int mouseX, final int mouseY, final float partialTicks) {
        filteredEntries.forEach(e -> e.drawToolTip(mouseX, mouseY));
    }

    @Override
    public void drawScreen(final int mouseXIn, final int mouseYIn, final float partialTicks) {
        super.drawScreen(mouseXIn, mouseYIn, partialTicks);

        @Nonnull final GuiTextField searchBar = guiFactory.getSearchBar();
        @Nonnull final String text = I18n.format("fml.menu.mods.search");

        final int x = searchBar.x + (searchBar.width >> 1) - (mc.fontRenderer.getStringWidth(text) >> 1);
        mc.fontRenderer.drawString(text, x, searchBar.y - 5 - mc.fontRenderer.FONT_HEIGHT, 0xFFFFFF);
        searchBar.drawTextBox();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        @Nonnull final GuiTextField searchBar = guiFactory.getSearchBar();
        searchBar.updateCursorCounter();
        if(!lastFilterText.equals(searchBar.getText())) updateFilteredEntries(searchBar.getText());
    }
}
