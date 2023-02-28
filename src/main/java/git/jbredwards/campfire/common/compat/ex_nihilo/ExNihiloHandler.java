package git.jbredwards.campfire.common.compat.ex_nihilo;

import exnihilocreatio.registries.manager.ExNihiloRegistryManager;
import git.jbredwards.campfire.common.init.CampfireBlocks;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 *
 * @author jbred
 *
 */
public final class ExNihiloHandler
{
    public static void handleHeatSources() throws IllegalAccessException {
        ExNihiloRegistryManager.HEAT_REGISTRY.register(new CampfireBlockInfo(CampfireBlocks.BRAZIER.getDefaultState()), 4);
        ExNihiloRegistryManager.HEAT_REGISTRY.register(new CampfireBlockInfo(CampfireBlocks.CAMPFIRE.getDefaultState()), 4);
        //replace ex nihilo heat registry with one that has a special case for campfires
        //(without this, entries for campfires are needed for every lit blockState, thus leading to duplicate jei recipes)
        final Field heatRegistry = ReflectionHelper.findField(ExNihiloRegistryManager.class, "HEAT_REGISTRY", null);
        ReflectionHelper.setPrivateValue(Field.class, heatRegistry, heatRegistry.getModifiers() &~ Modifier.FINAL, "modifiers", null);
        heatRegistry.set(null, new CampfireHeatRegistry(ExNihiloRegistryManager.HEAT_REGISTRY));
    }
}
