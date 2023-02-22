package git.jbredwards.campfire.common.dispenser;

import git.jbredwards.campfire.common.block.AbstractCampfire;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public class BehaviorCampfireIgnite extends Bootstrap.BehaviorDispenseOptional
{
    @Nonnull
    protected final IBehaviorDispenseItem fallback;
    public BehaviorCampfireIgnite(@Nonnull Item item) { this(BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(item)); }
    public BehaviorCampfireIgnite(@Nullable IBehaviorDispenseItem fallbackIn) {
        fallback = fallbackIn == null ? new BehaviorDefaultDispenseItem() : fallbackIn;
    }

    @Nonnull
    @Override
    protected ItemStack dispenseStack(@Nonnull IBlockSource source, @Nonnull ItemStack stack) {
        final BlockPos pos = source.getBlockPos().offset(source.getBlockState().getValue(BlockDispenser.FACING));
        final IBlockState state = source.getWorld().getBlockState(pos);

        if(state.getBlock() instanceof AbstractCampfire && ((AbstractCampfire)state.getBlock()).igniteFire(source.getWorld(), pos, state)) {
            successful = true;
            if(!state.getValue(AbstractCampfire.LIT)) {
                if(stack.getMaxStackSize() == 1) {
                    if(stack.attemptDamageItem(1, source.getWorld().rand, null))
                        stack.setCount(0);
                }

                else stack.shrink(1);
            }

            return stack;
        }

        successful = false;
        return fallback.dispense(source, stack);
    }
}
