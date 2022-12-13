package git.jbredwards.campfire.client.renderer.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public final class ModelCampfireFire implements IModel
{
    @Nonnull
    final ResourceLocation parent, coloredTex;
    ModelCampfireFire(@Nonnull ResourceLocation parent, @Nonnull ResourceLocation coloredTex) {
        this.parent = parent;
        this.coloredTex = coloredTex;
    }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getTextures() {
        return Collections.unmodifiableList(Lists.newArrayList(coloredTex));
    }

    @Nonnull
    @Override
    public IModel process(@Nonnull ImmutableMap<String, String> customData) {
        if(customData.containsKey("parent") && customData.containsKey("coloredTex")) {
            final JsonElement parent = new JsonParser().parse(customData.get("parent"));
            if(parent.isJsonPrimitive() && parent.getAsJsonPrimitive().isString()) {
                return new ModelCampfireFire(
                        new ModelResourceLocation(parent.getAsString()),
                        new ResourceLocation(customData.get("coloredTex")));
            }
        }

        throw new IllegalArgumentException("Invalid args for: " + customData);
    }

    @Nonnull
    @Override
    public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return null;
    }
}
