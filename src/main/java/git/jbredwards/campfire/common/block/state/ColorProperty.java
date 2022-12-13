package git.jbredwards.campfire.common.block.state;

import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public enum ColorProperty implements IUnlistedProperty<Integer>
{
    INSTANCE;

    @Nonnull
    @Override
    public String getName() { return "color"; }

    @Override
    public boolean isValid(@Nullable Integer value) { return value != null; }

    @Nonnull
    @Override
    public Class<Integer> getType() { return Integer.class; }

    @Nonnull
    @Override
    public String valueToString(@Nonnull Integer value) { return value.toString(); }
}
