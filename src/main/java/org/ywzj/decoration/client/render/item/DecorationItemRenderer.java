package org.ywzj.decoration.client.render.item;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.model.SlotModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.ywzj.decoration.client.resource.ClientAssetsManager;
import org.ywzj.decoration.client.resource.display.BaseDisplay;
import org.ywzj.decoration.client.resource.display.DecorationBedrockModel;
import org.ywzj.decoration.item.DecorationItem;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class DecorationItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final SlotModel ICON_MODEL = new SlotModel();

    public DecorationItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(@Nonnull ItemStack itemStack, @Nonnull ItemDisplayContext transformType,
                             @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        if (!(itemStack.getItem() instanceof DecorationItem decorationItem)) {
            return;
        }
        BaseDisplay display = ClientAssetsManager.INSTANCE.getDecorationDisplay(decorationItem.getDisplayId()).orElse(null);
        poseStack.pushPose();
        try {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            if (display != null) {
                ResourceLocation slotTexture = display.getSlotTexture();
                DecorationBedrockModel model = display.getModel();
                ResourceLocation texture = display.getTexture();
                if (slotTexture != null) {
                    if (transformType != ItemDisplayContext.GUI) {
                        poseStack.mulPose(Axis.YN.rotationDegrees(-45.0F));
                    }
                    VertexConsumer slotBuffer = buffer.getBuffer(RenderType.entityTranslucent(slotTexture));
                    ICON_MODEL.renderToBuffer(poseStack, slotBuffer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
                    return;
                } else if (model != null && texture != null && model.hasBakedModel()) {
                    centerOnModel(poseStack, model);
                    model.renderToBuffer(poseStack, buffer, texture, packedLight);
                    return;
                }
            }
            VertexConsumer fallbackBuffer = buffer.getBuffer(RenderType.entityTranslucent(MissingTextureAtlasSprite.getLocation()));
            ICON_MODEL.renderToBuffer(poseStack, fallbackBuffer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        } finally {
            poseStack.popPose();
        }
    }

    public static void centerOnModel(PoseStack poseStack, DecorationBedrockModel model) {
        AABB boundingBox = model.getRenderBoundingBox();
        if (boundingBox == null) {
            return;
        }
        Vec3 center = boundingBox.getCenter();
        poseStack.translate(-center.x, -center.y, -center.z);
    }

}
