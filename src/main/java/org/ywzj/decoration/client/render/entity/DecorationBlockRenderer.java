package org.ywzj.decoration.client.render.entity;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.AnimationRateLimiter;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.time.AnimationClocks;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BakedModelInstance;
import com.maydaymemory.mae.basic.ArrayPoseBuilder;
import com.maydaymemory.mae.basic.Pose;
import com.maydaymemory.mae.basic.ZYXBoneTransformFactory;
import com.maydaymemory.mae.blend.EulerAdditiveBlender;
import com.maydaymemory.mae.blend.SimpleEulerAdditiveBlender;
import com.maydaymemory.mae.control.runner.AnimationContext;
import com.maydaymemory.mae.control.runner.AnimationRunner;
import com.maydaymemory.mae.control.runner.PlayingState;
import com.maydaymemory.mae.control.runner.StopState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.ywzj.decoration.blockentity.DecorationBlockEntity;
import org.ywzj.decoration.client.resource.ClientAssetsManager;
import org.ywzj.decoration.client.resource.display.BaseDisplay;
import org.ywzj.decoration.client.resource.display.DecorationBedrockModel;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

@OnlyIn(Dist.CLIENT)
public class DecorationBlockRenderer implements BlockEntityRenderer<DecorationBlockEntity> {

    private static final String INTERACTION_ANIMATION_NAME = "animation.use";

    private static final EulerAdditiveBlender BLENDER =
            new SimpleEulerAdditiveBlender(new ZYXBoneTransformFactory(), ArrayPoseBuilder::new);

    private final Map<DecorationBlockEntity, AnimationState> animationStates = new WeakHashMap<>();

    public DecorationBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(DecorationBlockEntity decoration, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BaseDisplay display = ClientAssetsManager.INSTANCE.getDecorationDisplay(decoration.getDisplayId()).orElse(null);
        if (display == null) {
            return;
        }
        DecorationBedrockModel model = display.getModel();
        ResourceLocation texture = display.getTexture();
        if (model == null || texture == null || !model.hasBakedModel()) {
            return;
        }
        BakedModelInstance modelInstance = modelInstanceOf(decoration, display, model);
        if (modelInstance == null) {
            return;
        }
        poseStack.pushPose();
        try {
            poseStack.translate(0.5D + decoration.getXOffset(), decoration.getYOffset(), 0.5D + decoration.getZOffset());
            float scale = decoration.getScale();
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Axis.YP.rotationDegrees(-decoration.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(decoration.getXRot()));
            poseStack.mulPose(Axis.ZP.rotationDegrees(decoration.getZRot()));
            model.renderToBuffer(modelInstance, poseStack, bufferSource, texture, packedLight);
        } finally {
            poseStack.popPose();
        }
    }

    @Nullable
    private BakedModelInstance modelInstanceOf(DecorationBlockEntity decoration, BaseDisplay display, DecorationBedrockModel model) {
        AnimationState animationState = animationStates.computeIfAbsent(decoration, key -> new AnimationState());
        if (animationState.modelInstance == null || animationState.model != model
                || !display.getDisplayId().equals(animationState.displayId)) {
            animationState.init(display, model);
        }
        animationState.updateTrigger(decoration.getAnimationTrigger());
        animationState.tick();
        return animationState.modelInstance;
    }

    private static final class AnimationState {

        private ResourceLocation displayId;
        private DecorationBedrockModel model;
        private BakedModelInstance modelInstance;
        private AnimationRunner interactionRunner;
        private BedrockAnimation interactionAnimation;
        private AnimationRateLimiter<Pose> animationRateLimiter;
        private Pose lastPose;
        private int observedTrigger;
        private boolean triggerInitialized;

        private void init(BaseDisplay display, DecorationBedrockModel model) {
            this.displayId = display.getDisplayId();
            this.model = model;
            this.modelInstance = model.createBakedInstance();
            this.interactionRunner = null;
            this.interactionAnimation = null;
            this.animationRateLimiter = new AnimationRateLimiter<>(AnimationClocks.client(), AnimationRateLimiter.FPS_60);
            this.lastPose = null;
            this.observedTrigger = 0;
            this.triggerInitialized = false;
            Map<String, BedrockAnimation> animations = display.getAnimations();
            this.interactionAnimation = animations.get(INTERACTION_ANIMATION_NAME);
        }

        private void updateTrigger(int trigger) {
            if (!triggerInitialized) {
                observedTrigger = trigger;
                triggerInitialized = true;
                if (trigger != 0) {
                    startInteractionAnimation();
                }
                return;
            }
            if (trigger == observedTrigger || interactionAnimation == null) {
                return;
            }
            observedTrigger = trigger;
            startInteractionAnimation();
        }

        private void startInteractionAnimation() {
            if (interactionAnimation == null) {
                return;
            }
            AnimationContext context = new AnimationContext(interactionAnimation.getSpecifiedEndTimeS());
            interactionRunner = new AnimationRunner(interactionAnimation, context);
            interactionRunner.setState(new PlayingState(System::nanoTime, StopState::new));
            animationRateLimiter.reset();
        }

        private void tick() {
            if (animationRateLimiter == null || modelInstance == null) {
                return;
            }
            if (interactionRunner == null) {
                return;
            }
            interactionRunner.tick();
            if (interactionRunner.getAnimationContext().isEnd()) {
                interactionRunner = null;
                modelInstance.resetPose();
                lastPose = null;
                return;
            }
            Pose pose = animationRateLimiter.update(() -> BLENDER.blend(modelInstance.getBindPose(), interactionRunner.evaluate()));
            if (pose != lastPose) {
                modelInstance.applyPose(pose);
                lastPose = pose;
            }
        }

    }

}
