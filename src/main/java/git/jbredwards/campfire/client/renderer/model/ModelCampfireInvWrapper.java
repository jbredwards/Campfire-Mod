package git.jbredwards.campfire.client.renderer.model;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 * wraps around the parent model and applies the transformations specified in the model,
 * mainly used to apply the correct item model transformations
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public final class ModelCampfireInvWrapper implements IModel
{
    @Nonnull
    public static final ModelCampfireInvWrapper DEFAULT = new ModelCampfireInvWrapper(new ResourceLocation("campfire", "logs"));

    @Nonnull
    final ResourceLocation parent;
    ModelCampfireInvWrapper(@Nonnull ResourceLocation parent) { this.parent = parent; }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getTextures() { return Collections.emptyList(); }

    @Nonnull
    @Override
    public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new BakedModel(ModelLoaderRegistry.getModelOrLogError(parent, "Couldn't load Campfire inventory model dependency: " + parent).bake(state, format, bakedTextureGetter), PerspectiveMapWrapper.getTransforms(state));
    }

    @Nonnull
    @Override
    public IModel process(@Nonnull ImmutableMap<String, String> customData) {
        if(customData.containsKey("parent")) {
            final JsonElement element = new JsonParser().parse(customData.get("parent"));
            if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
                return new ModelCampfireInvWrapper(new ModelResourceLocation(element.getAsString()));

            FMLLog.log.fatal("Expect ModelResourceLocation, got: {}", customData.get("parent"));
        }

        return DEFAULT;
    }

    static final class BakedModel extends BakedModelWrapper<IBakedModel>
    {
        @Nonnull
        final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> cameraTransforms;
        BakedModel(@Nonnull IBakedModel originalModel, @Nonnull ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> cameraTransforms) {
            super(originalModel);
            this.cameraTransforms = cameraTransforms;
        }

        @Nonnull
        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull ItemCameraTransforms.TransformType cameraTransformType) {
            return PerspectiveMapWrapper.handlePerspective(this, cameraTransforms, cameraTransformType);
        }

        @Override
        public ItemOverrideList getOverrides() {
            final ItemOverrideList overrides = super.getOverrides();
            return new ItemOverrideList(overrides.getOverrides()) {
                @Nonnull
                @Override
                public IBakedModel handleItemState(@Nonnull IBakedModel originalModelIn, @Nonnull ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                    return new BakedModel(overrides.handleItemState(originalModel, stack, world, entity), cameraTransforms);
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
            return modelLocation.getNamespace().equals("campfire") && modelLocation.getPath().endsWith("inventory_builtin");
        }

        @Nonnull
        @Override
        public IModel loadModel(@Nonnull ResourceLocation modelLocation) { return DEFAULT; }
    }
}
