package git.jbredwards.campfire.common.config;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipeHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 *
 * @author jbred
 *
 */
@Config(modid = "campfire")
@Mod.EventBusSubscriber(modid = "campfire")
public final class CampfireConfigHandler
{
    @Config.LangKey("config.campfire.brazierEmitsSmoke")
    public static boolean brazierEmitsSmoke = true;

    @Config.LangKey("config.campfire.campfireEmitsSmoke")
    public static boolean campfireEmitsSmoke = true;

    @Config.LangKey("config.campfire.doesSmokeFollowDye")
    public static boolean doesSmokeFollowDye = true;

    @Config.LangKey("config.campfire.hasExtraSlots")
    public static boolean hasExtraSlots = true;

    @Config.LangKey("config.campfire.isBurningBlock")
    public static boolean isBurningBlock = true;

    @Config.LangKey("config.campfire.poweredAction")
    @Nonnull public static PoweredAction poweredAction = PoweredAction.COLOR;

    @Config.RequiresMcRestart
    @Config.LangKey("config.campfire.logTypesWhitelist")
    @Nonnull public static String[] logTypesWhitelist = new String[0];

    @Config.RequiresMcRestart
    @Config.LangKey("config.campfire.logTypesBlacklist")
    @Nonnull public static String[] logTypesBlacklist = new String[0];

    @Nonnull
    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ItemStack.class, ItemStackDeserializer.INSTANCE)
            .create();

    @Nonnull
    static final List<ItemStack> TYPES = new ArrayList<>();

    @Nonnull
    public static List<ItemStack> getAllTypes() { return Collections.unmodifiableList(TYPES); }

    public static void buildTypes() {
        final List<ItemStack> types = new ArrayList<>();
        //autogenerate initial campfire logs based on oredict
        OreDictionary.getOres("logWood").forEach(stack -> addNonDuplicateStack(types, stack));

        //per-mod config whitelist
        Loader.instance().getIndexedModList().keySet().forEach(modid -> {
            //whitelist
            try {
                final @Nullable InputStream whitelistIn = Loader.class.getResourceAsStream(String.format("/assets/%s/campfire/log_whitelist.json", modid));
                if(whitelistIn != null) {
                    final List<ItemStack> whitelist = new ArrayList<>();
                    for(ItemStack stack : GSON.fromJson(IOUtils.toString(whitelistIn, Charset.defaultCharset()), ItemStack[].class))
                        addNonDuplicateStack(whitelist, stack);

                    whitelist.removeIf(stack -> isStackInList(types, stack));
                    types.addAll(whitelist);
                }
            } catch(IOException ignored) {}

            //blacklist
            try {
                final @Nullable InputStream blacklistIn = Loader.class.getResourceAsStream(String.format("/assets/%s/campfire/log_blacklist.json", modid));
                if(blacklistIn != null) {
                    final List<ItemStack> blacklist = new ArrayList<>();
                    for(ItemStack stack : GSON.fromJson(IOUtils.toString(blacklistIn, Charset.defaultCharset()), ItemStack[].class))
                        addNonDuplicateStack(blacklist, stack);

                    types.removeIf(stack -> isStackInList(blacklist, stack));
                }
            } catch(IOException ignored) {}
        });

        //modpack config whitelist
        final List<ItemStack> configWhitelist = getStacks(logTypesWhitelist);
        configWhitelist.removeIf(stack -> isStackInList(types, stack));
        types.addAll(configWhitelist);

        //modpack config blacklist
        final List<ItemStack> configBlacklist = getStacks(logTypesBlacklist);
        types.removeIf(stack -> isStackInList(configBlacklist, stack));

        //cache types
        TYPES.clear();
        TYPES.addAll(types);
        TYPES.removeIf(ItemStack::isEmpty);
    }

    public static void buildRecipes() {
        //autogenerate campfire recipes based on furnace food recipes
        final Map<ItemStack, ItemStack> furnaceRecipes = FurnaceRecipes.instance().getSmeltingList();
        furnaceRecipes.forEach((in, out) -> {
            if(in.getItem() instanceof ItemFood && out.getItem() instanceof ItemFood) {
                CampfireRecipeHandler.createRecipe(Lists.newArrayList(in), out, 400, FurnaceRecipes.instance().getSmeltingExperience(out));
            }
        });
    }

    @Nonnull
    static List<ItemStack> getStacks(@Nonnull String[] data) {
        final List<ItemStack> stacks = new ArrayList<>();
        for(String dataStr : data) {
            try {
                final NBTTagCompound nbt = JsonToNBT.getTagFromJson(dataStr);
                if(!nbt.hasKey("Count", Constants.NBT.TAG_INT)) nbt.setInteger("Count", 1);
                addNonDuplicateStack(stacks, new ItemStack(nbt));
            }
            catch(NBTException ignore) {}
        }

        return stacks;
    }

    static void addNonDuplicateStack(@Nonnull List<ItemStack> stacks, @Nonnull ItemStack stack) {
        if(stack.getMetadata() != OreDictionary.WILDCARD_VALUE) { if(!isStackInList(stacks, stack)) stacks.add(stack); }
        else {
            final NonNullList<ItemStack> tabItems = NonNullList.create();
            stack.getItem().getSubItems(CreativeTabs.SEARCH, tabItems);
            tabItems.forEach(tabItem -> {
                if(!isStackInList(stacks, tabItem)) stacks.add(tabItem);
            });
        }
    }

    static boolean isStackInList(@Nonnull List<ItemStack> stacks, @Nonnull ItemStack toCompare) {
        for(ItemStack stack : stacks) {
            if(ItemHandlerHelper.canItemStacksStack(stack, toCompare)) {
                return true;
            }
        }

        return false;
    }

    @SubscribeEvent
    static void sync(@Nonnull ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals("campfire")) ConfigManager.sync("campfire", Config.Type.INSTANCE);
    }

    public enum PoweredAction
    {
        IGNORE,
        COLOR,
        DISABLE
    }
}
