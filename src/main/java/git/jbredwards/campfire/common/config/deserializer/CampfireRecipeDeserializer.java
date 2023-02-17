package git.jbredwards.campfire.common.config.deserializer;

import com.google.gson.*;
import git.jbredwards.campfire.common.recipe.campfire.CampfireRecipe;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public enum CampfireRecipeDeserializer implements JsonDeserializer<CampfireRecipe>
{
    INSTANCE;

    @Nonnull
    @Override
    public CampfireRecipe deserialize(@Nonnull JsonElement jsonIn, @Nullable Type typeOfT, @Nullable JsonDeserializationContext context) throws JsonParseException {
        final JsonObject json = jsonIn.getAsJsonObject();
        if(!json.has("output")) throw new JsonSyntaxException("No Provided Output for Recipe: " + jsonIn);
        if(!json.has("inputs")) throw new JsonSyntaxException("No Provided Inputs for Recipe: " + jsonIn);

        //gather inputs
        final List<ItemStack> inputs = new ArrayList<>();
        final JsonElement inputsElement = json.get("inputs");
        if(inputsElement.isJsonObject()) {
            final ItemStack stack = ItemStackDeserializer.INSTANCE.deserialize(inputsElement, null, null);
            if(!stack.isEmpty()) inputs.add(stack);
        }
        else for(JsonElement element : inputsElement.getAsJsonArray()) {
            final ItemStack stack = ItemStackDeserializer.INSTANCE.deserialize(element, null, null);
            if(!stack.isEmpty()) inputs.add(stack);
        }

        final ItemStack output = ItemStackDeserializer.INSTANCE.deserialize(json.get("output"), null, null);
        final float experience = json.has("experience") ? json.getAsJsonPrimitive("experience").getAsFloat() : 0;
        final int cookTime = json.has("cookTime") ? json.getAsJsonPrimitive("cookTime").getAsInt() : 400;
        if(!json.has("campfireTypes")) return CampfireRecipe.of(inputs, output, cookTime, experience);

        //gather campfire types
        final List<ItemStack> campfireTypes = new ArrayList<>();
        final JsonElement campfireTypesElement = json.get("campfireTypes");
        if(campfireTypesElement.isJsonObject()) {
            final ItemStack stack = ItemStackDeserializer.INSTANCE.deserialize(campfireTypesElement, null, null);
            if(!stack.isEmpty()) campfireTypes.add(stack);
        }
        else for(JsonElement element : campfireTypesElement.getAsJsonArray()) {
            final ItemStack stack = ItemStackDeserializer.INSTANCE.deserialize(element, null, null);
            if(!stack.isEmpty()) campfireTypes.add(stack);
        }

        return CampfireRecipe.of(campfireTypes, inputs, output, cookTime, experience);
    }
}
