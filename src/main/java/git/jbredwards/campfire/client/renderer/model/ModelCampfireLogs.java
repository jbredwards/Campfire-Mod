package git.jbredwards.campfire.client.renderer.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import git.jbredwards.campfire.common.block.state.ItemStackProperty;
import git.jbredwards.campfire.common.capability.ICampfireType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * takes in a parent model and applies the log textures stored in the ICampfireType capability
 * (side texture are applied to all faces, top texture is applied to faces that have a tintIndex of 1)
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public final class ModelCampfireLogs implements IModel
{
    @Nonnull
    public static final ModelCampfireLogs DEFAULT = new ModelCampfireLogs(new ResourceLocation("campfire", "logs"));

    @Nonnull
    final ResourceLocation parent;
    ModelCampfireLogs(@Nonnull ResourceLocation parent) { this.parent = parent; }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getTextures() { return Collections.emptyList(); }

    @Nonnull
    @Override
    public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new BakedModel(ModelLoaderRegistry.getModelOrLogError(parent, "Couldn't load Campfire log model dependency: " + parent).bake(state, format, bakedTextureGetter), ItemStack.EMPTY);
    }

    @Nonnull
    @Override
    public IModel process(@Nonnull ImmutableMap<String, String> customData) {
        if(customData.containsKey("parent")) {
            final JsonElement element = new JsonParser().parse(customData.get("parent"));
            if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
                return new ModelCampfireLogs(new ModelResourceLocation(element.getAsString()));

            FMLLog.log.fatal("Expect ModelResourceLocation, got: {}", customData.get("parent"));
        }

        return DEFAULT;
    }

    static final class BakedModel extends BakedModelWrapper<IBakedModel>
    {
        @Nonnull
        final ItemStack forcedType;
        BakedModel(@Nonnull IBakedModel originalModel, @Nonnull ItemStack forcedTypeIn) {
            super(originalModel);
            forcedType = forcedTypeIn;
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            final ItemStack type = forcedType.isEmpty() && state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).getValue(ItemStackProperty.INSTANCE) : forcedType;
            final List<BakedQuad> bakedQuads = super.getQuads(state, side, rand);
            if(type == null || type.isEmpty()) return bakedQuads;

            //get side texture
            final IBakedModel itemModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(type, null, null);
            final TextureAtlasSprite sideTex = itemModel.getParticleTexture();

            //get top texture
            final List<BakedQuad> itemQuads = new ArrayList<>();
            for(EnumFacing facing : EnumFacing.values()) itemQuads.addAll(itemModel.getQuads(null, facing, 0));
            TextureAtlasSprite topTex = sideTex;
            for(BakedQuad quad : itemQuads) {
                if(sideTex != quad.getSprite()) {
                    topTex = quad.getSprite();
                    break;
                }
            }

            //apply textures to parent model
            final ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
            for(BakedQuad quad : bakedQuads) {
                TextureAtlasSprite tex = quad.getTintIndex() == 1 ? topTex : sideTex;
                builder.add(new BakedQuadRetextured(quad, tex));
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
                    final @Nullable ICampfireType type = ICampfireType.get(stack);
                    return type != null ? new BakedModel(originalModel, type.get()) : originalModelIn;
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
            return modelLocation.getNamespace().equals("campfire") && modelLocation.getPath().endsWith("logs_builtin");
        }

        @Nonnull
        @Override
        public IModel loadModel(@Nonnull ResourceLocation modelLocation) { return DEFAULT; }
    }
}
