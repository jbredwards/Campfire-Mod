package git.jbredwards.campfire.common.config.deserializer;

import com.google.gson.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    public ItemStack deserialize(@Nonnull JsonElement json, @Nullable Type typeOfT, @Nullable JsonDeserializationContext context) throws JsonParseException {
        try {
            final NBTTagCompound nbt = JsonToNBT.getTagFromJson(json.toString());
            if(!nbt.hasKey("Count", Constants.NBT.TAG_INT)) nbt.setInteger("Count", 1);
            return new ItemStack(nbt);
        }
        //oops
        catch(NBTException e) { throw new JsonSyntaxException("Malformed ItemStack NBT", e); }
    }
}
