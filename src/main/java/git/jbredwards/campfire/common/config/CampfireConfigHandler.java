package git.jbredwards.campfire.common.config;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.*;
import git.jbredwards.campfire.common.config.deserializer.CampfireRecipeDeserializer;
import git.jbredwards.campfire.common.config.deserializer.ItemStackDeserializer;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipeHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
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

    @Config.LangKey("config.campfire.isBrazierBurningBlock")
    public static boolean isBrazierBurningBlock = true;

    @Config.LangKey("config.campfire.isCampfireBurningBlock")
    public static boolean isCampfireBurningBlock = true;

    @Config.LangKey("config.campfire.resetDyeOnExtinguish")
    public static boolean resetDyeOnExtinguish = true;

    @Config.LangKey("config.campfire.unlitOnCraft")
    public static boolean unlitOnCraft = false;

    @Config.RangeInt(min = 0, max = 200)
    @Config.LangKey("config.campfire.brazierBurnOut")
    public static int brazierBurnOut = 0;

    @Config.RangeInt(min = 0, max = 200)
    @Config.LangKey("config.campfire.campfireBurnOut")
    public static int campfireBurnOut = 0;

    @Config.LangKey("config.campfire.poweredAction")
    @Nonnull public static PoweredAction poweredAction = PoweredAction.COLOR;

    @Nonnull
    static final List<ItemStack> TYPES = new ArrayList<>();

    @Nonnull
    public static List<ItemStack> getAllTypes() { return Collections.unmodifiableList(TYPES); }

    @SuppressWarnings("UnstableApiUsage")
    public static void buildTypes() {
        final List<ItemStack> types = new ArrayList<>();
        //autogenerate initial campfire logs based on oredict
        OreDictionary.getOres("logWood").forEach(stack -> addNonDuplicateStack(types, stack));

        //per-mod types config
        Loader.instance().getIndexedModList().keySet().forEach(modid -> {
            final @Nullable InputStream stream = Loader.class.getResourceAsStream(String.format("/assets/%s/campfire/types.json", modid));
            if(stream != null) loadCampfireTypes(stream, types);
        });

        //modpack types config
        final File file = new File("config/campfire", "types.json");
        try { loadCampfireTypes(new FileInputStream(file), types); }
        catch(FileNotFoundException e) {
            try { //create new file if not present
                Files.createParentDirs(file);
                if(file.createNewFile()) {
                    final FileWriter writer = new FileWriter(file);
                    writer.write("{\n\n}\n");
                    writer.close();
                }
            }
            //should never pass
            catch(IOException ioException) { ioException.printStackTrace(); }
        }

        //cache types
        TYPES.clear();
        TYPES.addAll(types);
        TYPES.removeIf(ItemStack::isEmpty);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void buildRecipes() {
        //autogenerate campfire recipes based on furnace food recipes
        FurnaceRecipes.instance().getSmeltingList().forEach((in, out) -> {
            if(in.getItem() instanceof ItemFood && out.getItem() instanceof ItemFood) {
                CampfireRecipeHandler.createRecipe(Lists.newArrayList(in), out, 400, FurnaceRecipes.instance().getSmeltingExperience(out));
            }
        });

        //per-mod recipe config
        Loader.instance().getIndexedModList().keySet().forEach(modid -> {
            final @Nullable InputStream stream = Loader.class.getResourceAsStream(String.format("/assets/%s/campfire/recipes.json", modid));
            if(stream != null) loadCampfireRecipes(stream);
        });

        //modpack recipe config
        final File file = new File("config/campfire", "recipes.json");
        try { loadCampfireRecipes(new FileInputStream(file)); }
        catch(FileNotFoundException e) {
            try { //create new file if not present
                Files.createParentDirs(file);
                if(file.createNewFile()) {
                    final FileWriter writer = new FileWriter(file);
                    writer.write("{\n\n}\n");
                    writer.close();
                }
            }
            //should never pass
            catch(IOException ioException) { ioException.printStackTrace(); }
        }
    }

    static void loadCampfireTypes(@Nonnull InputStream stream, @Nonnull List<ItemStack> types) {
        try {
            final JsonObject logs = new JsonParser().parse(IOUtils.toString(stream, Charset.defaultCharset())).getAsJsonObject();
            //whitelist
            if(logs.has("add")) {
                final List<ItemStack> add = new ArrayList<>();
                logs.getAsJsonArray("add").forEach(element -> addNonDuplicateStack(add,
                        ItemStackDeserializer.INSTANCE.deserialize(element, null, null)));
                add.removeIf(stack -> isStackInList(types, stack));
                types.addAll(add);
            }
            //blacklist
            if(logs.has("remove")) {
                final List<ItemStack> remove = new ArrayList<>();
                logs.getAsJsonArray("remove").forEach(element -> addNonDuplicateStack(remove,
                        ItemStackDeserializer.INSTANCE.deserialize(element, null, null)));
                types.removeIf(stack -> isStackInList(remove, stack));
            }
        }
        //oops
        catch(IOException ignored) {}
    }

    static void loadCampfireRecipes(@Nonnull InputStream stream) {
        try {
            final JsonObject recipes = new JsonParser().parse(IOUtils.toString(stream, Charset.defaultCharset())).getAsJsonObject();
            //add new recipes
            if(recipes.has("add")) {
                final JsonElement add = recipes.get("add");
                if(add.isJsonObject()) CampfireRecipeHandler.createRecipe(
                        CampfireRecipeDeserializer.INSTANCE.deserialize(add, null, null));

                else add.getAsJsonArray().forEach(element -> CampfireRecipeHandler.createRecipe(
                        CampfireRecipeDeserializer.INSTANCE.deserialize(element, null, null)));
            }
            //remove existing recipes
            if(recipes.has("remove")) {
                final JsonElement removeElement = recipes.get("remove");
                //only one recipe to remove
                if(removeElement.isJsonObject()) {
                    final JsonObject remove = removeElement.getAsJsonObject();
                    if(remove.has("input")) CampfireRecipeHandler.removeInput(
                            ItemStackDeserializer.INSTANCE.deserialize(remove.get("input"), null, null));
                    else if(remove.has("output")) CampfireRecipeHandler.removeOutput(
                            ItemStackDeserializer.INSTANCE.deserialize(remove.get("output"), null, null));
                }
                //multiple recipes to remove
                else removeElement.getAsJsonArray().forEach(element -> {
                    if(element.isJsonObject()) {
                        final JsonObject remove = element.getAsJsonObject();
                        if(remove.has("input")) CampfireRecipeHandler.removeInput(
                                ItemStackDeserializer.INSTANCE.deserialize(remove.get("input"), null, null));
                        else if(remove.has("output")) CampfireRecipeHandler.removeOutput(
                                ItemStackDeserializer.INSTANCE.deserialize(remove.get("output"), null, null));
                    }
                });
            }
        }
        //oops
        catch(IOException ignored) {}
    }

    static void addNonDuplicateStack(@Nonnull List<ItemStack> stacks, @Nonnull ItemStack stack) {
        if(!stack.isEmpty()) {
            if(stack.getMetadata() != OreDictionary.WILDCARD_VALUE) {
                if(!isStackInList(stacks, stack)) stacks.add(stack);
            }
            else {
                final NonNullList<ItemStack> tabItems = NonNullList.create();
                stack.getItem().getSubItems(CreativeTabs.SEARCH, tabItems);
                tabItems.forEach(tabItem -> {
                    if(!isStackInList(stacks, tabItem)) stacks.add(tabItem);
                });
            }
        }
    }

    public static boolean isStackInList(@Nonnull List<ItemStack> stacks, @Nonnull ItemStack toCompare) {
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
