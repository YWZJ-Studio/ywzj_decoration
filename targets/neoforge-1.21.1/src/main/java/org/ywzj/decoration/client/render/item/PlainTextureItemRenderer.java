package org.ywzj.decoration.client.render.item;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.model.SlotModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.ywzj.decoration.item.PlainTextureItem;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class PlainTextureItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final SlotModel PLAIN_TEXTURE_ITEM_MODEL = new SlotModel();

    public PlainTextureItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(@Nonnull ItemStack itemStack, @Nonnull ItemDisplayContext transformType,
                             @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        if (!(itemStack.getItem() instanceof PlainTextureItem)) {
            return;
        }
        ResourceLocation textureLocation = PlainTextureItem.textureOf(itemStack);
        if (textureLocation == null) {
            return;
        }
        poseStack.pushPose();
        try {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(textureLocation));
            PLAIN_TEXTURE_ITEM_MODEL.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, -1);
        } finally {
            poseStack.popPose();
        }
    }

}
