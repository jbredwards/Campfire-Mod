/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.futuremc;

import git.jbredwards.campfire.api.registry.BlockStateDataFixer;
import git.jbredwards.campfire.api.registry.RegistryContainer;
import git.jbredwards.campfire.mod.common.init.CampfireBlocks;
import git.jbredwards.campfire.mod.common.init.CampfireItems;
import git.jbredwards.campfire.mod.common.tileentity.slot.CampfireSlotInfo;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thedarkcolour.futuremc.config.FConfig;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@RegistryContainer(hasEvents = true, dependencies = "futuremc")
final class FutureMCHandler
{
    static int oldCampfireID = -1;
    @RegistryContainer.Action
    static void resetOldCampfireID(@Nonnull final FMLServerStoppedEvent event) { oldCampfireID = -1; }

    @SubscribeEvent
    static void remapBlocks(@Nonnull final RegistryEvent.MissingMappings<Block> event) {
        event.getAllMappings().stream().filter(m -> "futuremc".equals(m.key.getNamespace())).forEach(mapping -> {
            switch(mapping.key.getPath()) {
                case "campfire": mapping.remap(CampfireBlocks.CAMPFIRE); oldCampfireID = mapping.id; break;
                case "chain": mapping.remap(CampfireBlocks.CHAIN); break;
                case "lantern": mapping.remap(CampfireBlocks.LANTERN); break;
            }
        });
    }

    @SubscribeEvent
    static void remapItems(@Nonnull final RegistryEvent.MissingMappings<Item> event) {
        event.getAllMappings().stream().filter(m -> "futuremc".equals(m.key.getNamespace())).forEach(mapping -> {
            switch(mapping.key.getPath()) {
                case "campfire": mapping.remap(CampfireItems.CAMPFIRE); break;
                case "chain": mapping.remap(CampfireItems.CHAIN); break;
                case "lantern": mapping.remap(CampfireItems.LANTERN); break;
            }
        });
    }

    @RegistryContainer.Action
    static void remapWorldData(@Nonnull final FMLInitializationEvent event) {
        @Nonnull final ModFixs dataFixer = FMLCommonHandler.instance().getDataFixer().init("campfire:futuremc_adapter", 1);
        // fix block state data
        dataFixer.registerFix(FixTypes.CHUNK, new BlockStateDataFixer(1, ((blockID, blockMeta) -> blockID == oldCampfireID ?
            (EnumFacing.HORIZONTALS[blockMeta & 3].getAxis() == EnumFacing.Axis.X ? 8 : 0) |
            ((blockMeta & 8) != 0 ? 4 : 0) | ((blockMeta & 4) != 0 ? 2 : 0) : blockMeta
        )));
        // fix tile entity data
        dataFixer.registerFix(FixTypes.BLOCK_ENTITY, new IFixableData() {
            @Override
            public int getFixVersion() { return 1; }

            @Nonnull
            @Override
            public NBTTagCompound fixTagCompound(@Nonnull final NBTTagCompound compound) {
                if("futuremc:campfire".equals(compound.getString("id"))) {
                    // get old data (scrap cooking progress, futuremc does not save an output stack)
                    // @Nonnull final int[] cookingTimes = compound.getIntArray("CookingTimes"), cookingTotalTimes = compound.getIntArray("CookingTotalTimes");
                    @Nonnull final NBTTagList inventory = compound.getCompoundTag("Buffer").getTagList("Items", Constants.NBT.TAG_COMPOUND);

                    // remap to new data
                    @Nonnull final NBTTagList slots = new NBTTagList();
                    for(int i = 0; i < 4; i++) slots.appendTag(new NBTTagCompound());
                    for(int i = 0; i < inventory.tagCount() && i < 4; i++) {
                        final int slot = MathHelper.clamp(inventory.getCompoundTagAt(i).getInteger("Slot"), 0, 3);

                        @Nonnull final CampfireSlotInfo slotInfo = new CampfireSlotInfo(null, slot);
                        @Nonnull final ItemStack stack = new ItemStack(inventory.getCompoundTagAt(i));

                        slotInfo.stack = stack;
                        slotInfo.cookTime = 0; // cookingTimes[slot];
                        slotInfo.maxCookTime = stack.isEmpty() ? 0 : -1; // cookingTotalTimes[slot];

                        slots.set(slot, slotInfo.serializeNBT());
                    }

                    // set new data
                    compound.setTag("Slots", slots);
                    compound.setString("id", "campfire:campfire");
                }

                return compound;
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void preBlockRegister(@Nonnull final RegistryEvent.Register<Block> event) {
        FConfig.INSTANCE.getVillageAndPillage().campfire.enabled = false;
        FConfig.INSTANCE.getNetherUpdate().chain = false;
        FConfig.INSTANCE.getVillageAndPillage().lantern = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void preItemRegister(@Nonnull final RegistryEvent.Register<Item> event) {
        FConfig.INSTANCE.getVillageAndPillage().campfire.enabled = false;
        FConfig.INSTANCE.getNetherUpdate().chain = false;
        FConfig.INSTANCE.getVillageAndPillage().lantern = false;
    }
}
