/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.item.tab;

import git.jbredwards.campfire.api.creativetab.TexturedCreativeTab;
import git.jbredwards.campfire.mod.common.init.CampfireItems;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class CampfireCreativeTab extends TexturedCreativeTab
{
    @Nonnull
    public static final CampfireCreativeTab INSTANCE = new CampfireCreativeTab("campfire");
    private CampfireCreativeTab(@Nonnull final String label) { super(label); }

    @SideOnly(Side.CLIENT)
    @Override
    protected void buildIcons() {
        CampfireItems.BRAZIER.getSubItems(this, icons);
        CampfireItems.CAMPFIRE.getSubItems(this, icons);
    }

    @Override
    public int getLabelColor() { return 0xDFB453; }
}
