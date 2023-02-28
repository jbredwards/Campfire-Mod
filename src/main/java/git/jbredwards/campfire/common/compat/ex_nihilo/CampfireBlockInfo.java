package git.jbredwards.campfire.common.compat.ex_nihilo;

import exnihilocreatio.util.BlockInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class CampfireBlockInfo extends BlockInfo
{
    public CampfireBlockInfo(@Nonnull IBlockState state) { super(state); }

    @Override
    public boolean isWildcard() { return true; }

    @Nonnull
    @Override
    public ItemStack getItemStack() { return new ItemStack(getBlock(), 1, OreDictionary.WILDCARD_VALUE); }
}
