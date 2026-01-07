/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.init;

import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.api.registry.RegistryContainer;
import git.jbredwards.campfire.mod.common.item.ItemBlockColored;
import git.jbredwards.campfire.mod.common.item.ItemBlockMultitab;
import git.jbredwards.campfire.mod.common.item.ItemCampfire;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 *
 * @author jbred
 *
 */
@RegistryContainer(Item.class)
public final class CampfireItems
{
    // =====
    // ITEMS
    // =====
    
    @RegistryContainer.Entry
    public static final ItemBlockColored BRAZIER = new ItemBlockColored(CampfireBlocks.BRAZIER);
    @RegistryContainer.Entry
    public static final ItemCampfire<CampfireWoodType.Normal> CAMPFIRE = new ItemCampfire<>(CampfireBlocks.CAMPFIRE);
    @RegistryContainer.Entry
    public static final ItemBlockMultitab CAMPFIRE_ASH = new ItemBlockMultitab(CampfireBlocks.CAMPFIRE_ASH);
    @RegistryContainer.Entry
    public static final ItemBlockMultitab CHAIN = new ItemBlockMultitab(CampfireBlocks.CHAIN);
    @RegistryContainer.Entry
    public static final ItemBlockColored LANTERN = new ItemBlockColored(CampfireBlocks.LANTERN);
    
    // ==============
    // ORE DICTIONARY
    // ==============
    
    @RegistryContainer.Action
    static void registerOreDictionary() {
        OreDictionary.registerOre("brazier", new ItemStack(BRAZIER, 1, 0));
        OreDictionary.registerOre("brazierIron", new ItemStack(BRAZIER, 1, 0));
        OreDictionary.registerOre("brazierLit", new ItemStack(BRAZIER, 1, 0));
        OreDictionary.registerOre("brazier", new ItemStack(BRAZIER, 1, 1));
        OreDictionary.registerOre("brazierIron", new ItemStack(BRAZIER, 1, 1));
        OreDictionary.registerOre("brazierUnlit", new ItemStack(BRAZIER, 1, 1));

        OreDictionary.registerOre("campfire", new ItemStack(CAMPFIRE, 1, 0));
        OreDictionary.registerOre("campfireLit", new ItemStack(CAMPFIRE, 1, 0));
        OreDictionary.registerOre("campfire", new ItemStack(CAMPFIRE, 1, 1));
        OreDictionary.registerOre("campfireUnlit", new ItemStack(CAMPFIRE, 1, 1));

        OreDictionary.registerOre("chain", CHAIN);
        OreDictionary.registerOre("chainIron", CHAIN);

        OreDictionary.registerOre("lantern", LANTERN);
        OreDictionary.registerOre("lanternIron", LANTERN);

        OreDictionary.registerOre("charcoal", new ItemStack(Items.COAL, 1, 1));
        OreDictionary.registerOre("coal", new ItemStack(Items.COAL, 1, 0));
    }
}
