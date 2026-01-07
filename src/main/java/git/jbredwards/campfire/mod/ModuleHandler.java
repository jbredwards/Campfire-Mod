/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import git.jbredwards.campfire.api.registry.RegistryContainer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;

/**
 * Internal, other mods should not invoke this class themselves.
 * This class handles other mods using the registry container annotations as well.
 * @author jbred
 *
 */
final class ModuleHandler
{
    @Nonnull private static final Map<Class<?>, Multimap<EventPriority, Consumer<FMLStateEvent>>> FML_EVENTS = new WeakHashMap<>();
    @Nonnull private static final ListMultimap<String, ModContainer> PACKAGE_OWNERS = ReflectionHelper.getPrivateValue(
            LoadController.class, ReflectionHelper.getPrivateValue(Loader.class, Loader.instance(), "modController"), "packageOwners");

    @SuppressWarnings({"deprecation", "unchecked", "rawtypes"})
    static void initializeRegistryModules(@Nonnull final Set<ASMDataTable.ASMData> all) throws Exception {
        if(!all.isEmpty()) {
            final int busID = ReflectionHelper.findField(EventBus.class, "busID").getInt(MinecraftForge.EVENT_BUS);

            for(@Nonnull final ASMDataTable.ASMData data : all) {
                @Nonnull final Class<?> containerClass = Class.forName(data.getClassName(), false, ModuleHandler.class.getClassLoader());
                @Nonnull final RegistryContainer containerInfo = containerClass.getAnnotation(RegistryContainer.class);
                if(!Arrays.stream(containerInfo.dependencies()).allMatch(Loader::isModLoaded)) continue;

                // owning mod
                @Nullable final List<ModContainer> activeContainer = containerInfo.modid().isEmpty() ? PACKAGE_OWNERS.get(containerClass.getPackage().getName()) : null;
                if(activeContainer == null && containerInfo.modid().isEmpty()) throw new IllegalStateException("Could not get mod id from container: \"" + data.getClassName() + '"');
                @Nonnull final String activeMod = activeContainer == null ? containerInfo.modid() : activeContainer.get(0).getModId();

                // normal events
                if(containerInfo.hasEvents()) MinecraftForge.EVENT_BUS.register(containerClass);
                @Nonnull final Class<? extends IForgeRegistryEntry>[] registryTypes = containerInfo.value();

                // registry
                if(registryTypes.length > 1) throw new IllegalStateException("A maximum of one registry type can be defined per registry container, \"" + data.getClassName() + "\" has " + registryTypes.length);
                @Nullable final IForgeRegistry<?> registry = registryTypes.length == 0 ? null : RegistryManager.ACTIVE.getRegistry(registryTypes[0]);

                // registry found
                if(registry != null) {
                    @Nonnull final ListenerList listeners = createEvent(registry).getListenerList();

                    // handle defined registry entries
                    if(containerClass.getDeclaredFields().length != 0) listeners.register(busID, containerInfo.priority(), event -> {
                        if(((RegistryEvent.Register<?>)event).getRegistry() == registry) {

                            for(@Nonnull final Field registryEntry : containerClass.getDeclaredFields()) {
                                if(Modifier.isStatic(registryEntry.getModifiers()) && registryEntry.isAnnotationPresent(RegistryContainer.Entry.class)) {
                                    if(!registryEntry.isAccessible()) registryEntry.setAccessible(true);

                                    @Nonnull final IForgeRegistryEntry<? extends IForgeRegistryEntry> entry;
                                    try { entry = (IForgeRegistryEntry<? extends IForgeRegistryEntry>)registryEntry.get(null); }
                                    catch(@Nonnull final IllegalAccessException e) { throw new RuntimeException(e); }
                                    if(entry.getRegistryType() != ((RegistryEvent.Register<?>)event).getGenericType()) continue;

                                    // for entries that already have their registry name set (like entity entries or villager professions)
                                    else if(entry.getRegistryName() != null) {
                                        register(registry, entry);
                                        continue;
                                    }

                                    @Nonnull String registryName = registryEntry.getAnnotation(RegistryContainer.Entry.class).value();
                                    if(registryName.isEmpty()) { // try to get a registry name if it was defined some other way
                                        if(entry instanceof Block) registryName = ((Block)entry).translationKey.replaceFirst(activeMod + '.', "");
                                        else if(entry instanceof ItemBlock) registryName = ((ItemBlock)entry).getBlock().translationKey.replaceFirst(activeMod + '.', "");

                                        else if(entry instanceof Item) registryName = ((Item)entry).translationKey.replaceFirst(activeMod + '.', "");
                                        else if(entry instanceof Potion) registryName = ((Potion)entry).getName().replaceFirst(activeMod + ".effect.", "");
                                        else if(entry instanceof PotionType) registryName = ((PotionType)entry).baseName.replaceFirst(activeMod + '.', "");
                                        else if(entry instanceof SoundEvent) registryName = ((SoundEvent)entry).soundName.getPath();

                                        else throw new IllegalArgumentException("Could not generate registry name for field: \"" + registryEntry.toGenericString() + "\", it must be specified manually.");
                                    }

                                    register(registry, entry.setRegistryName(new ResourceLocation(activeMod, registryName)));
                                }
                            }
                        }
                    });

                    // handle additional registry calls (like ore dictionary, tile entities, or recipes)
                    for(@Nonnull final Method registryAction : containerClass.getDeclaredMethods()) {
                        if(registryAction.isAnnotationPresent(RegistryContainer.Action.class)) {
                            if(!Modifier.isStatic(registryAction.getModifiers())) throw new IllegalArgumentException("Non-static method cannot be invoked in class \"" + data.getClassName() + "\": " + registryAction.getName());

                            if(!registryAction.isAccessible()) registryAction.setAccessible(true);
                            if(registryAction.getParameterCount() == 0 || !FMLStateEvent.class.isAssignableFrom(registryAction.getParameterTypes()[0])) {
                                listeners.register(busID, registryAction.getAnnotation(RegistryContainer.Action.class).priority(), eventIn -> {
                                    @Nonnull final RegistryEvent.Register<?> event = (RegistryEvent.Register<?>)eventIn;
                                    if(event.getRegistry() == registry) {
                                        try { registryAction.invoke(null, registryAction.getParameterCount() == 0 ? new Object[0] : new Object[] {event.getRegistry()}); }
                                        catch(@Nonnull final IllegalAccessException | InvocationTargetException e) { throw new RuntimeException(e); }
                                    }
                                });
                            }
                        }
                    }
                }

                // register fml events
                for(@Nonnull final Method fmlEventAction : containerClass.getDeclaredMethods()) {
                    if(!fmlEventAction.isAccessible()) fmlEventAction.setAccessible(true);
                    if(fmlEventAction.getParameterCount() == 1 && FMLStateEvent.class.isAssignableFrom(fmlEventAction.getParameterTypes()[0])) {
                        if(!Modifier.isStatic(fmlEventAction.getModifiers())) throw new IllegalArgumentException("Non-static method cannot be invoked in class \"" + data.getClassName() + "\": " + fmlEventAction.getName());
                        @Nonnull final EventPriority priority = fmlEventAction.isAnnotationPresent(RegistryContainer.Action.class) ? fmlEventAction.getAnnotation(RegistryContainer.Action.class).priority() : containerInfo.priority();

                        FML_EVENTS.computeIfAbsent(fmlEventAction.getParameterTypes()[0], key -> ArrayListMultimap.create()).put(priority, event -> {
                            @Nullable final ModContainer prev = Loader.instance().activeModContainer(), active = Loader.instance().getIndexedModList().get(activeMod);
                            try {
                                if(prev != active) { Loader.instance().setActiveModContainer(active); event.applyModContainer(active); }
                                fmlEventAction.invoke(null, event);
                                if(prev != active) { Loader.instance().setActiveModContainer(prev); event.applyModContainer(prev); }
                            }
                            catch(@Nonnull final InvocationTargetException | IllegalAccessException e) { throw new RuntimeException(e); }
                        });
                    }
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nonnull
    private static RegistryEvent.Register createEvent(@Nonnull final IForgeRegistry registry) {
        return new RegistryEvent.Register(RegistryManager.ACTIVE.getName(registry), registry);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void register(@Nonnull final IForgeRegistry registry, @Nonnull final IForgeRegistryEntry entry) {
        registry.register(entry);
    }

    static void fireFMLEvents(@Nonnull final FMLStateEvent fmlEvent) {
        if(FML_EVENTS.containsKey(fmlEvent.getClass())) {
            @Nonnull final Multimap<EventPriority, Consumer<FMLStateEvent>> events = FML_EVENTS.get(fmlEvent.getClass());
            events.get(EventPriority.HIGHEST).forEach(event -> event.accept(fmlEvent));
            events.get(EventPriority.HIGH).forEach(event -> event.accept(fmlEvent));
            events.get(EventPriority.NORMAL).forEach(event -> event.accept(fmlEvent));
            events.get(EventPriority.LOW).forEach(event -> event.accept(fmlEvent));
            events.get(EventPriority.LOWEST).forEach(event -> event.accept(fmlEvent));
        }
    }
}
