/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.client.renderer.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import git.jbredwards.campfire.mod.common.block.state.ColorProperty;
import git.jbredwards.campfire.mod.common.item.ItemBlockColored;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public final class ModelCampfireFire implements IModel
{
    @Nonnull static final ModelCampfireFire DEFAULT = new ModelCampfireFire(TextureMap.LOCATION_MISSING_TEXTURE, Collections.emptyList());

    @Nonnull final ResourceLocation parent;
    @Nonnull final List<Int2ObjectMap.Entry<Int2ObjectMap.Entry<ResourceLocation>>> colorKeys;

    ModelCampfireFire(@Nonnull ResourceLocation parentIn, @Nonnull List<Int2ObjectMap.Entry<Int2ObjectMap.Entry<ResourceLocation>>> colorKeysIn) {
        parent = parentIn;
        colorKeys = colorKeysIn;
    }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singletonList(parent);
    }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getTextures() {
        return colorKeys.stream().map(entry -> entry.getValue().getValue()).collect(ImmutableList.toImmutableList());
    }

    @Nonnull
    @Override
    public IModel process(@Nonnull ImmutableMap<String, String> customData) {
        if(customData.containsKey("parent") && customData.containsKey("colorKeys")) {
            @Nonnull final JsonElement parent = new JsonParser().parse(customData.get("parent"));
            if(parent.isJsonPrimitive() && parent.getAsJsonPrimitive().isString()) {
                @Nonnull final JsonElement colorKeys = new JsonParser().parse(customData.get("colorKeys"));
                if(colorKeys.isJsonArray()) {
                    @Nonnull final List<Int2ObjectMap.Entry<Int2ObjectMap.Entry<ResourceLocation>>> keys = new ArrayList<>(colorKeys.getAsJsonArray().size());
                    colorKeys.getAsJsonArray().forEach(element -> {
                        @Nonnull final JsonObject key = element.getAsJsonObject();
                        @Nonnull final ResourceLocation texture = key.has("texture") ? new ResourceLocation(key.get("texture").getAsString()) : TextureMap.LOCATION_MISSING_TEXTURE;

                        final int tint = key.get("tint").getAsInt();
                        final int newTint = key.has("new_tint") ? key.get("new_tint").getAsInt() : tint;

                        keys.add(new AbstractInt2ObjectMap.BasicEntry<>(tint, new AbstractInt2ObjectMap.BasicEntry<>(newTint, texture)));
                    });

                    return new ModelCampfireFire(new ModelResourceLocation(parent.getAsString()), keys);
                }
            }
        }

        throw new IllegalArgumentException("Invalid args for: " + customData);
    }

    @Nonnull
    @Override
    public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        @Nonnull final Int2ObjectMap<Int2ObjectMap.Entry<TextureAtlasSprite>> texColorKeys = new Int2ObjectOpenHashMap<>(colorKeys.size());
        colorKeys.forEach(entry -> {
            @Nonnull final ResourceLocation location = entry.getValue().getValue();
            @Nullable final TextureAtlasSprite tex = location.equals(TextureMap.LOCATION_MISSING_TEXTURE) ? null : bakedTextureGetter.apply(location);

            texColorKeys.put(entry.getIntKey(), new AbstractInt2ObjectMap.BasicEntry<>(entry.getValue().getIntKey(), tex));
        });

        return new BakedModel(ModelLoaderRegistry.getModelOrMissing(parent).bake(state, format, bakedTextureGetter), texColorKeys, -1, PerspectiveMapWrapper.getTransforms(state));
    }

    static final class BakedModel extends ModelCampfireInvWrapper.BakedModel
    {
        @Nonnull
        final Int2ObjectMap<Int2ObjectMap.Entry<TextureAtlasSprite>> colorKeys;
        final int forcedColor;

        BakedModel(@Nonnull IBakedModel originalModel, @Nonnull Int2ObjectMap<Int2ObjectMap.Entry<TextureAtlasSprite>> colorKeysIn, int forcedColorIn,
                   @Nonnull ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> cameraTransformsIn) {
            super(originalModel, cameraTransformsIn);
            colorKeys = colorKeysIn;
            forcedColor = forcedColorIn;
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            @Nonnull final List<BakedQuad> bakedQuads = super.getQuads(state, side, rand);
            if((forcedColor == -1 && state instanceof IExtendedBlockState ?
            Optional.ofNullable(((IExtendedBlockState)state).getValue(ColorProperty.INSTANCE)).orElse(forcedColor) : forcedColor) == -1)
                return bakedQuads;

            @Nonnull final ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
            for(@Nonnull final BakedQuad quad : bakedQuads) {
                if(quad.hasTintIndex()) {
                    @Nullable final Int2ObjectMap.Entry<TextureAtlasSprite> tex = colorKeys.get(quad.getTintIndex());
                    if(tex != null) {
                        @Nonnull final BakedQuad newQuad = new BakedQuadRetextured(quad, tex.getValue() == null ? quad.getSprite() : tex.getValue());
                        newQuad.tintIndex = tex.getIntKey();

                        builder.add(newQuad);
                        continue;
                    }
                }

                builder.add(quad);
            }

            return builder.build();
        }

        @Nonnull
        @Override
        IBakedModel createOverrideModel(@Nonnull final IBakedModel original, @Nonnull final ItemStack stack) {
            return new BakedModel(original, colorKeys, ItemBlockColored.getColor(stack), cameraTransforms);
        }
    }

    public enum Loader implements ICustomModelLoader
    {
        INSTANCE;

        @Override
        public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {}

        @Override
        public boolean accepts(@Nonnull ResourceLocation modelLocation) {
            return modelLocation.getNamespace().equals("campfire") && modelLocation.getPath().endsWith("colored_builtin");
        }

        @Nonnull
        @Override
        public IModel loadModel(@Nonnull ResourceLocation modelLocation) { return DEFAULT; }
    }
}
