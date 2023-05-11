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
        if(inputsElement.isJsonObject()) inputs.addAll(ItemStackListDeserializer.INSTANCE.deserialize(inputsElement, null, null));
        else for(JsonElement element : inputsElement.getAsJsonArray()) inputs.addAll(ItemStackListDeserializer.INSTANCE.deserialize(element, null, null));

        final List<ItemStack> outputs = ItemStackListDeserializer.INSTANCE.deserialize(json.get("output"), null, null);
        if(outputs.size() != 1) throw new IllegalArgumentException("The output for the recipe must be exactly one item!: " + outputs);

        final float experience = json.has("experience") ? json.getAsJsonPrimitive("experience").getAsFloat() : 0;
        final int cookTime = json.has("cookTime") ? json.getAsJsonPrimitive("cookTime").getAsInt() : 400;
        if(!json.has("campfireTypes")) return CampfireRecipe.of(inputs, outputs.get(0), cookTime, experience);

        //gather campfire types
        final List<ItemStack> campfireTypes = new ArrayList<>();
        final JsonElement campfireTypesElement = json.get("campfireTypes");
        if(campfireTypesElement.isJsonObject()) campfireTypes.addAll(ItemStackListDeserializer.INSTANCE.deserialize(campfireTypesElement, null, null));
        else for(JsonElement element : campfireTypesElement.getAsJsonArray()) campfireTypes.addAll(ItemStackListDeserializer.INSTANCE.deserialize(element, null, null));

        return CampfireRecipe.of(campfireTypes, inputs, outputs.get(0), cookTime, experience);
    }
}
