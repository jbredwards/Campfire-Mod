package git.jbredwards.campfire.client.renderer.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import git.jbredwards.campfire.common.block.state.ColorProperty;
import git.jbredwards.campfire.common.item.ItemBlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    static final ModelCampfireFire DEFAULT = new ModelCampfireFire(TextureMap.LOCATION_MISSING_TEXTURE, TextureMap.LOCATION_MISSING_TEXTURE, TextureMap.LOCATION_MISSING_TEXTURE);

    @Nonnull
    final ResourceLocation parent, coloredFire, coloredAsh;
    ModelCampfireFire(@Nonnull ResourceLocation parentIn, @Nonnull ResourceLocation coloredFireIn, @Nonnull ResourceLocation coloredAshIn) {
        parent = parentIn;
        coloredFire = coloredFireIn;
        coloredAsh = coloredAshIn;
    }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getTextures() { return ImmutableList.of(coloredFire, coloredAsh); }

    @Nonnull
    @Override
    public IModel process(@Nonnull ImmutableMap<String, String> customData) {
        if(customData.containsKey("parent") && customData.containsKey("coloredFire") && customData.containsKey("coloredAsh")) {
            final JsonElement parent = new JsonParser().parse(customData.get("parent"));
            if(parent.isJsonPrimitive() && parent.getAsJsonPrimitive().isString()) {
                final JsonElement coloredFire = new JsonParser().parse(customData.get("coloredFire"));
                if(coloredFire.isJsonPrimitive() && coloredFire.getAsJsonPrimitive().isString()) {
                    final JsonElement coloredAsh = new JsonParser().parse(customData.get("coloredAsh"));
                    if(coloredAsh.isJsonPrimitive() && coloredAsh.getAsJsonPrimitive().isString())
                        return new ModelCampfireFire(
                                new ModelResourceLocation(parent.getAsString()),
                                new ResourceLocation(coloredFire.getAsString()),
                                new ResourceLocation(coloredAsh.getAsString()));
                }
            }
        }

        throw new IllegalArgumentException("Invalid args for: " + customData);
    }

    @Nonnull
    @Override
    public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new BakedModel(ModelLoaderRegistry.getModelOrMissing(parent).bake(state, format, bakedTextureGetter), bakedTextureGetter.apply(coloredFire), bakedTextureGetter.apply(coloredAsh), -1);
    }

    static final class BakedModel extends BakedModelWrapper<IBakedModel>
    {
        @Nonnull
        final TextureAtlasSprite coloredFire, coloredAsh;
        final int forcedColor;

        BakedModel(@Nonnull IBakedModel originalModel, @Nonnull TextureAtlasSprite coloredFireIn, @Nonnull TextureAtlasSprite coloredAshIn, int forcedColorIn) {
            super(originalModel);
            coloredFire = coloredFireIn;
            coloredAsh = coloredAshIn;
            forcedColor = forcedColorIn;
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            final int color = forcedColor == -1 && state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).getValue(ColorProperty.INSTANCE) : forcedColor;
            final List<BakedQuad> bakedQuads = super.getQuads(state, side, rand);
            if(color == -1) return bakedQuads;

            final ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
            for(BakedQuad quad : bakedQuads) {
                if(quad.getTintIndex() == 5) builder.add(new BakedQuadRetextured(quad, coloredFire));
                else if(quad.getTintIndex() == 6) builder.add(new BakedQuadRetextured(quad, coloredAsh));
                else builder.add(quad);
            }

            return builder.build();
        }

        @Nonnull
        @Override
        public ItemOverrideList getOverrides() {
            return new ItemOverrideList(Collections.emptyList()) {
                @Nonnull
                @Override
                public IBakedModel handleItemState(@Nonnull IBakedModel originalModelIn, @Nonnull ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                    return new BakedModel(originalModel, coloredFire, coloredAsh, ItemBlockColored.getColor(stack));
                }
            };
        }
    }

    public enum Loader implements ICustomModelLoader
    {
        INSTANCE;

        @Override
        public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {}

        @Override
        public boolean accepts(@Nonnull ResourceLocation modelLocation) {
            return modelLocation.getNamespace().equals("campfire") && modelLocation.getPath().endsWith("fire_builtin");
        }

        @Nonnull
        @Override
        public IModel loadModel(@Nonnull ResourceLocation modelLocation) { return DEFAULT; }
    }
}
