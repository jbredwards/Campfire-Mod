package git.jbredwards.campfire.common.config;

import com.google.common.collect.Lists;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipeHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @Config.LangKey("config.campfire.hasExtraSlots")
    public static boolean hasExtraSlots = true;

    @Config.LangKey("config.campfire.isBurningBlock")
    public static boolean isBurningBlock = true;

    @Config.LangKey("config.campfire.poweredAction")
    @Nonnull public static PoweredAction poweredAction = PoweredAction.COLOR;

    @Nonnull
    static final List<ItemStack> TYPES = new ArrayList<>();

    @Nonnull
    public static List<ItemStack> getAllTypes() { return Collections.unmodifiableList(TYPES); }

    public static void buildTypes() {
        //autogenerate campfire logs based on oredict
        final NonNullList<ItemStack> types = NonNullList.create();
        OreDictionary.getOres("logWood").forEach(stack -> {
            if(stack.getMetadata() != OreDictionary.WILDCARD_VALUE) types.add(stack);
            else stack.getItem().getSubItems(CreativeTabs.SEARCH, types);
        });

        TYPES.clear();
        TYPES.addAll(types);
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

    @SubscribeEvent
    static void sync(@Nonnull ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals("campfire")) ConfigManager.sync("campfire", Config.Type.INSTANCE);
    }

    public enum PoweredAction
    {
        IGNORE,
        COLOR,
        DISABLE;
    }
}
