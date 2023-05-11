package git.jbredwards.campfire.common.config.deserializer;

import com.google.gson.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public enum ItemStackListDeserializer implements JsonDeserializer<List<ItemStack>>
{
    INSTANCE;

    @Nonnull
    @Override
    public List<ItemStack> deserialize(@Nonnull JsonElement json, @Nullable Type typeOfT, @Nullable JsonDeserializationContext context) throws JsonParseException {
        try {
            //oredict support
            final NBTTagCompound nbt = JsonToNBT.getTagFromJson(json.toString());
            if(nbt.hasKey("oreid", Constants.NBT.TAG_STRING)) {
                final List<ItemStack> ores = getSubTypes(OreDictionary.getOres(nbt.getString("oreid")));

                ores.removeIf(ItemStack::isEmpty);
                return Collections.unmodifiableList(ores);
            }

            //fix bad stack count
            else if(!nbt.hasKey("Count", Constants.NBT.TAG_INT)) nbt.setInteger("Count", 1);
            final ItemStack stack = new ItemStack(nbt);

            //wildcard support
            if(stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                final NonNullList<ItemStack> tabStacks = NonNullList.create();
                stack.getItem().getSubItems(CreativeTabs.SEARCH, tabStacks);

                tabStacks.removeIf(ItemStack::isEmpty);
                return Collections.unmodifiableList(tabStacks);
            }

            return stack.isEmpty() ? Collections.emptyList() : Collections.singletonList(stack);
        }

        //oops
        catch(NBTException e) { throw new JsonSyntaxException("Malformed ItemStack NBT", e); }
    }

    @Nonnull
    public static List<ItemStack> getSubTypes(@Nonnull List<ItemStack> stacks) {
        final NonNullList<ItemStack> tabStacks = NonNullList.create();
        stacks.forEach(stack -> {
            if(stack.getMetadata() == OreDictionary.WILDCARD_VALUE) stack.getItem().getSubItems(CreativeTabs.SEARCH, tabStacks);
            else tabStacks.add(stack);
        });

        return tabStacks;
    }
}
