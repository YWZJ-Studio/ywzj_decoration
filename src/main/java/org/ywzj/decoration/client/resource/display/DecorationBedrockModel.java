package org.ywzj.decoration.client.resource.display;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.BedrockModelRenderTypes;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.baked.BakedBedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.baked.BakedGeometryChunk;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.baked.BakerOptions;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BakedModelInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class DecorationBedrockModel extends BedrockModel {

    public static final Pattern ILLUMINATED_BONE_PATTERN = Pattern.compile("^illum_.*");
    private final BakedBedrockModel bakedModel;
    private final BakedModelInstance defaultModelInstance;
    private AABB geometryBoundingBox;
    private boolean geometryBoundsComputed;

    public DecorationBedrockModel(BedrockModelPOJO pojo, @Nullable BakerOptions bakerOptions) {
        super(pojo);
        this.boneMap.forEach((name, bone) -> {
            if (ILLUMINATED_BONE_PATTERN.matcher(name).matches()) {
                bone.illuminated = true;
            }
        });
        if (bakerOptions != null) {
            Set<String> preservedBones = new HashSet<>(bakerOptions.preservedBones());
            Set<Pattern> preservedBonePatterns = new HashSet<>(bakerOptions.preservedBonePatterns());
            preservedBonePatterns.add(ILLUMINATED_BONE_PATTERN);
            BakerOptions options = new BakerOptions(
                    bakerOptions.animatedBones(),
                    preservedBones,
                    preservedBonePatterns,
                    bakerOptions.bakeStaticGeometry(),
                    bakerOptions.debugFoldedTree(),
                    bakerOptions.retainCubeGeometry()
            );
            this.bakedModel = BakedBedrockModel.bake(pojo, options);
            this.defaultModelInstance = createBakedInstance();
        } else {
            this.bakedModel = null;
            this.defaultModelInstance = null;
        }
    }

    public BakedModelInstance createBakedInstance() {
        if (bakedModel == null) {
            throw new IllegalStateException("This display does not enable a baked model");
        }
        BakedModelInstance instance = bakedModel.createInstance();
        instance.resetPose();
        for (BoneState bone : instance.getBoneIndexes()) {
            bone.illuminated = ILLUMINATED_BONE_PATTERN.matcher(bone.name()).matches();
        }
        return instance;
    }

    @OnlyIn(Dist.CLIENT)
    public void renderToBuffer(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int packedLight) {
        renderToBuffer(defaultModelInstance, poseStack, bufferSource, texture, packedLight);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderToBuffer(BakedModelInstance instance, PoseStack poseStack, MultiBufferSource bufferSource,
                               ResourceLocation texture, int packedLight) {
        if (instance == null) {
            return;
        }
        instance.renderToBuffer(poseStack, bufferSource,
                RenderType.entityCutout(texture),
                BedrockModelRenderTypes.polyMeshCutout(texture),
                packedLight,
                OverlayTexture.NO_OVERLAY);
    }

    public boolean hasBakedModel() {
        return bakedModel != null;
    }

    @Nullable
    public BakedBedrockModel getBakedModel() {
        return bakedModel;
    }

    @Nullable
    public BakedModelInstance getDefaultModelInstance() {
        return defaultModelInstance;
    }

    @Nullable
    public AABB getGeometryBoundingBox() {
        if (!geometryBoundsComputed) {
            geometryBoundsComputed = true;
            if (bakedModel != null && defaultModelInstance != null) {
                Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
                Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);
                Vector3f point = new Vector3f();
                for (BakedGeometryChunk chunk : bakedModel.chunks()) {
                    Matrix4f transform = chunk.isRootAttached() ? new Matrix4f()
                            : defaultModelInstance.getGlobalTransform(chunk.attachBoneIndex());
                    includeVertices(chunk.quads().positions(), transform, point, min, max);
                    includeVertices(chunk.vertices().positions(), transform, point, min, max);
                }
                if (min.isFinite() && max.isFinite()) {
                    geometryBoundingBox = new AABB(min.x, min.y, min.z, max.x, max.y, max.z);
                }
            }
        }
        return geometryBoundingBox != null ? geometryBoundingBox : getRenderBoundingBox();
    }

    private static void includeVertices(float[] positions, Matrix4f transform, Vector3f point,
                                        Vector3f min, Vector3f max) {
        for (int i = 0; i + 2 < positions.length; i += 3) {
            point.set(positions[i], positions[i + 1], positions[i + 2]);
            transform.transformPosition(point);
            if (point.isFinite()) {
                min.min(point);
                max.max(point);
            }
        }
    }

    @Nullable
    public AABB getRenderBoundingBox() {
        if (bakedModel != null && bakedModel.getRenderBoundingBox() != null) {
            return bakedModel.getRenderBoundingBox();
        }
        return super.getRenderBoundingBox();
    }

}
