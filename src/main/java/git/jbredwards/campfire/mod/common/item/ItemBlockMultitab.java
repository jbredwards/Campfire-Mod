/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.item;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author jbred
 *
 */
public class ItemBlockMultitab extends ItemBlock
{
    @Nonnull
    public final Collection<CreativeTabs> additionalTabs = new HashSet<>();
    public ItemBlockMultitab(@Nonnull final Block block) { super(block); }

    @Nonnull
    @Override
    public CreativeTabs[] getCreativeTabs() {
        @Nonnull final CreativeTabs[] tabArray = new CreativeTabs[additionalTabs.size() + 2];
        tabArray[tabArray.length - 1] = getCreativeTab();
        return additionalTabs.toArray(tabArray);
    }
}
