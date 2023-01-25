package git.jbredwards.campfire;

import git.jbredwards.campfire.client.renderer.tileentity.CampfireTESR;
import git.jbredwards.campfire.common.capability.ICampfireType;
import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import git.jbredwards.campfire.common.dispenser.BehaviorCampfireIgnite;
import git.jbredwards.campfire.common.message.MessageFallParticles;
import git.jbredwards.campfire.common.message.MessageSyncCampfireSlot;
import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import net.minecraft.block.BlockDispenser;
import net.minecraft.init.Items;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
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
@Mod(modid = "campfire", name = "Campfire", version = "1.0.1")
public final class Campfire
{
    public static final boolean isFluidloggedAPI = Loader.isModLoaded("fluidlogged_api");

    @SuppressWarnings("NotNullFieldNotInitialized")
    @Nonnull public static SimpleNetworkWrapper wrapper;

    @Mod.EventHandler
    static void preInit(@Nonnull FMLPreInitializationEvent event) {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel("campfire");
        wrapper.registerMessage(MessageFallParticles.Handler.INSTANCE, MessageFallParticles.class, 0, Side.CLIENT);
        wrapper.registerMessage(MessageSyncCampfireSlot.Handler.INSTANCE, MessageSyncCampfireSlot.class, 1, Side.CLIENT);
        CapabilityManager.INSTANCE.register(ICampfireType.class, ICampfireType.Storage.INSTANCE, ICampfireType.Impl::new);
        if(event.getSide().isClient()) registerTESR();
    }

    @SideOnly(Side.CLIENT)
    static void registerTESR() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCampfire.class, new CampfireTESR());
    }

    @Mod.EventHandler
    static void postInit(@Nonnull FMLPostInitializationEvent event) {
        CampfireConfigHandler.buildRecipes();
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FIRE_CHARGE, new BehaviorCampfireIgnite(Items.FIRE_CHARGE));
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FLINT_AND_STEEL, new BehaviorCampfireIgnite(Items.FLINT_AND_STEEL));
    }
}
