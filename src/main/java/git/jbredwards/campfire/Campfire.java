package git.jbredwards.campfire;

import git.jbredwards.campfire.common.capability.ICampfireType;
import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod(modid = "campfire", name = "Campfire", version = "1.0.0")
public final class Campfire
{
    @Mod.EventHandler
    static void preInit(@Nonnull FMLPreInitializationEvent event) {
        CapabilityManager.INSTANCE.register(ICampfireType.class, ICampfireType.Storage.INSTANCE, ICampfireType.Impl::new);
    }

    @Mod.EventHandler
    static void postInit(@Nonnull FMLPostInitializationEvent event) { CampfireConfigHandler.buildRecipes(); }
}
