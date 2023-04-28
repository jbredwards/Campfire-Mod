package git.jbredwards.campfire;

import git.jbredwards.campfire.client.renderer.tileentity.CampfireTESR;
import git.jbredwards.campfire.common.capability.ICampfireType;
import git.jbredwards.campfire.common.compat.ex_nihilo.ExNihiloHandler;
import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.dispenser.BehaviorCampfireIgnite;
import git.jbredwards.campfire.common.message.MessageExtinguishEffects;
import git.jbredwards.campfire.common.message.MessageFallParticles;
import git.jbredwards.campfire.common.message.MessageSyncCampfireSlot;
import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import net.minecraft.block.BlockDispenser;
import net.minecraft.init.Items;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod(modid = "campfire", name = "Campfire", version = "1.1.5")
public final class Campfire
{
    public static final boolean isFluidloggedAPI = Loader.isModLoaded("fluidlogged_api");

    @SuppressWarnings("NotNullFieldNotInitialized")
    @Nonnull public static SimpleNetworkWrapper WRAPPER;

    @Mod.EventHandler
    static void preInit(@Nonnull FMLPreInitializationEvent event) {
        WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel("campfire");
        WRAPPER.registerMessage(MessageFallParticles.Handler.INSTANCE, MessageFallParticles.class, 0, Side.CLIENT);
        WRAPPER.registerMessage(MessageSyncCampfireSlot.Handler.INSTANCE, MessageSyncCampfireSlot.class, 1, Side.CLIENT);
        WRAPPER.registerMessage(MessageExtinguishEffects.Handler.INSTANCE, MessageExtinguishEffects.class, 2, Side.CLIENT);
        CapabilityManager.INSTANCE.register(ICampfireType.class, ICampfireType.Storage.INSTANCE, ICampfireType.Impl::new);
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    static void preInitClient(@Nonnull FMLPreInitializationEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCampfire.class, new CampfireTESR());
    }

    @Mod.EventHandler
    static void init(@Nonnull FMLInitializationEvent event) throws IllegalAccessException {
        if(Loader.isModLoaded("exnihilocreatio")) ExNihiloHandler.handleHeatSources();
    }

    @Mod.EventHandler
    static void postInit(@Nonnull FMLPostInitializationEvent event) {
        CampfireConfigHandler.buildRecipes();
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FIRE_CHARGE, new BehaviorCampfireIgnite(Items.FIRE_CHARGE));
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FLINT_AND_STEEL, new BehaviorCampfireIgnite(Items.FLINT_AND_STEEL));
    }
}
