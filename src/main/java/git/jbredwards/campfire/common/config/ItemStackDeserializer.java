package git.jbredwards.campfire.common.config;

import com.google.gson.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 *
 * @author jbred
 *
 */
public enum ItemStackDeserializer implements JsonDeserializer<ItemStack>
{
    INSTANCE;

    @Nonnull
    @Override
    public ItemStack deserialize(@Nonnull JsonElement json, @Nonnull Type typeOfT, @Nonnull JsonDeserializationContext context) throws JsonParseException {
        try {
            final NBTTagCompound nbt = JsonToNBT.getTagFromJson(json.toString());
            if(!nbt.hasKey("Count", Constants.NBT.TAG_INT)) nbt.setInteger("Count", 1);
            return new ItemStack(nbt);
        }
        //oops
        catch(NBTException e) { throw new JsonSyntaxException("Malformed NBT tag", e); }
    }
}
