package git.jbredwards.campfire.common.block.state;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public enum ItemStackProperty implements IUnlistedProperty<ItemStack>
{
    INSTANCE;

    @Nonnull
    @Override
    public String getName() { return "stack"; }

    @Override
    public boolean isValid(@Nullable ItemStack value) { return value != null; }

    @Nonnull
    @Override
    public Class<ItemStack> getType() { return ItemStack.class; }

    @Nonnull
    @Override
    public String valueToString(@Nonnull ItemStack value) { return value.toString(); }
}
