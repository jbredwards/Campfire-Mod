/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.item;

import git.jbredwards.campfire.api.block.campfire.ICampfireType;
import git.jbredwards.campfire.api.capability.CampfireWoodType;
import git.jbredwards.campfire.mod.common.block.AbstractCampfire;
import git.jbredwards.campfire.mod.common.capability.ICampfireWoodType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public class ItemCampfire<V extends CampfireWoodType<V>> extends ItemBlockColored
{
    @Nonnull
    public final ICampfireType<V> campfireType;
    public ItemCampfire(@Nonnull AbstractCampfire<V> block) {
        super(block);
        campfireType = block.campfireType;
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        return Optional.ofNullable(ICampfireWoodType.get(stack))
                .flatMap(ICampfireWoodType::getOptional)
                .map(CampfireWoodType::asItemStack)
                .map(type -> {
                    // if a defined special case exists in .lang file, use that instead of auto generating a name
                    final String specialCase = String.format("%s.type.%s.name", stack.getTranslationKey(), type.getTranslationKey());
                    if(I18n.canTranslate(specialCase)) return I18n.translateToLocal(specialCase);
                    // auto generate a name
                    return type.getDisplayName().replaceFirst(I18n.translateToLocal(getRegexTarget()), I18n.translateToLocal(getRegexReplacement()));
                })
                //should never pass
                .orElseGet(() -> super.getItemStackDisplayName(stack));
    }

    @Nonnull
    public String getRegexTarget() { return "regex.campfire.target"; }

    @Nonnull
    public String getRegexReplacement() { return "regex.campfire.replacement"; }

    @Nonnull
    public ItemStack applyType(@Nonnull ItemStack type) {
        return CampfireWoodType.fromStack(campfireType.getWoodTypes(), type).map(wt -> applyType(new ItemStack(this), wt)).orElseGet(() -> new ItemStack(this));
    }

    @Nonnull
    public static ItemStack applyType(@Nonnull ItemStack campfire, @Nonnull CampfireWoodType<?> type) {
        final ICampfireWoodType cap = ICampfireWoodType.get(campfire);
        if(cap != null) cap.set(type);
        return campfire;
    }
}
