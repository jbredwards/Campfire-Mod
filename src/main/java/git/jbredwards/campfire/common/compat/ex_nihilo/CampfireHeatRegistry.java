package git.jbredwards.campfire.common.compat.ex_nihilo;

import exnihilocreatio.registries.registries.HeatRegistry;
import exnihilocreatio.util.BlockInfo;
import git.jbredwards.campfire.common.block.AbstractCampfire;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class CampfireHeatRegistry extends HeatRegistry
{
    public CampfireHeatRegistry(@Nonnull HeatRegistry oldRegistry) { getRegistry().putAll(oldRegistry.getRegistry()); }

    @Override
    public int getHeatAmount(@Nonnull BlockInfo info) {
        if(info.hasBlock() && info.getBlock() instanceof AbstractCampfire) {
            if(!info.getBlockState().getValue(AbstractCampfire.LIT)) return 0;
            info = new CampfireBlockInfo(info.getBlock().getDefaultState());
        }

        return super.getHeatAmount(info);
    }
}
