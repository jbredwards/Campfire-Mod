package git.jbredwards.campfire.client.renderer.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
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
public final class ModelCampfireLogs implements IModel
{
    @Nonnull
    public static final ModelCampfireLogs DEFAULT = new ModelCampfireLogs(new ResourceLocation("campfire", "logs"));

    @Nonnull
    final ResourceLocation parent;
    public ModelCampfireLogs(@Nonnull ResourceLocation parent) {
        this.parent = parent;
    }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getTextures() { return Collections.emptyList(); }

    @Nonnull
    @Override
    public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new BakedModel(bakedTextureGetter, ModelLoaderRegistry.getModelOrLogError(parent, "Couldn't load Campfire log model dependency: " + parent).bake(state, format, bakedTextureGetter), ItemStack.EMPTY);
    }

    @Nonnull
    @Override
    public IModel process(@Nonnull ImmutableMap<String, String> customData) {
        if(customData.containsKey("parent")) {

        }

        return DEFAULT;
    }

    public static final class BakedModel implements IBakedModel
    {
        @Nonnull final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
        @Nonnull final IBakedModel parentModel;
        @Nonnull final ItemStack forcedType;

        public BakedModel(@Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, @Nonnull IBakedModel parentModel, @Nonnull ItemStack forcedType) {
            this.bakedTextureGetter = bakedTextureGetter;
            this.parentModel = parentModel;
            this.forcedType = forcedType;
        }

        @SuppressWarnings("ConstantConditions")
        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            final List<BakedQuad> bakedQuads = parentModel.getQuads(state, side, rand);
            final ItemStack type = forcedType.isEmpty() ? getType(state) : forcedType;
            if(type.isEmpty()) return bakedQuads;

            //gather textures
            final IBakedModel itemModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(type, null, null);
            final TextureAtlasSprite sideTex = itemModel.getParticleTexture();
            TextureAtlasSprite topTex = sideTex;
            if(side == null || side.getAxis().isHorizontal()) {
                for(BakedQuad quad : itemModel.getQuads(null, EnumFacing.UP, rand)) {
                    if(EnumFacing.UP == quad.getFace()) {
                        topTex = quad.getSprite();
                        break;
                    }
                }
            }

            //apply textures to parent model
            final ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
            for(BakedQuad quad : bakedQuads) {
                TextureAtlasSprite tex = quad.getFace() != null && quad.getFace().getAxis().isHorizontal() ? topTex : sideTex;
                builder.add(new BakedQuadRetextured(quad, tex));
            }

            return builder.build();
        }

        @Nonnull
        ItemStack getType(@Nullable IBlockState state) {
            return state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).getValue() : ItemStack.EMPTY;
        }

        @Override
        public boolean isAmbientOcclusion() { return parentModel.isAmbientOcclusion(); }

        @Override
        public boolean isGui3d() { return false; }

        @Override
        public boolean isBuiltInRenderer() { return false; }

        @Nonnull
        @Override
        public TextureAtlasSprite getParticleTexture() { return parentModel.getParticleTexture(); }

        @Nonnull
        @Override
        public ItemOverrideList getOverrides() {
            return new ItemOverrideList(Collections.emptyList()) {
                @Nonnull
                @Override
                public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, @Nonnull ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                    if(!(originalModel instanceof BakedModel)) return originalModel;
                    final @Nullable ICampfireType type = ICampfireType.get(stack);
                    return type != null ? new BakedModel(bakedTextureGetter, parentModel, type.getLog()) : originalModel;
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
