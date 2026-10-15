package org.ywzj.decoration.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sighs.apricityui.init.Document;
import com.sighs.apricityui.init.Element;
import com.sighs.apricityui.screen.ApricityScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.ywzj.decoration.blockentity.DecorationBlockEntity;
import org.ywzj.decoration.client.resource.ClientAssetsManager;
import org.ywzj.decoration.client.resource.display.BaseDisplay;
import org.ywzj.decoration.client.resource.display.DecorationBedrockModel;
import org.ywzj.decoration.network.Channel;
import org.ywzj.decoration.network.message.ClientDecorationAction;

import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class DecorationSettingsScreen extends ApricityScreen {

    private static final String TEMPLATE = "screens/decoration_settings.html";
    private static final int FULL_BRIGHT = 15728880;
    private final DecorationBlockEntity decoration;
    private Document document;
    private Element scaleInput;
    private Element xOffsetInput;
    private Element yOffsetInput;
    private Element zOffsetInput;
    private Element xRotationInput;
    private Element yRotationInput;
    private Element zRotationInput;
    private Element previewAnchor;
    private Element doneButton;
    private Element deleteButton;
    private float viewShiftX;
    private float viewShiftY;
    private float viewScale = 1.0F;
    private float viewRotX = 165.0F;
    private float viewRotY;
    private boolean previewDragging;
    private boolean serverUpdatePending;
    private int serverUpdateCooldown;
    private boolean sliderDragging;
    private boolean renderStateInitialized;
    private long lastInterpolationNanos;
    private float renderScale;
    private float renderXRotation;
    private float renderYRotation;
    private float renderZRotation;
    private float renderXOffset;
    private float renderYOffset;
    private float renderZOffset;
    private long lastRangeValueUpdateNanos;

    public DecorationSettingsScreen(DecorationBlockEntity decoration) {
        super(TEMPLATE);
        this.decoration = decoration;
        setPauseGame(false);
        setShowDefaultBackground(false);
    }

    @Override
    protected void init() {
        super.init();
        document = getLinkedDocument();
        if (document == null) {
            return;
        }
        scaleInput = element("decoration-scale");
        xOffsetInput = element("decoration-x-offset");
        yOffsetInput = element("decoration-y-offset");
        zOffsetInput = element("decoration-z-offset");
        xRotationInput = element("decoration-x-rotation");
        yRotationInput = element("decoration-y-rotation");
        zRotationInput = element("decoration-z-rotation");
        previewAnchor = element("decoration-settings-preview");
        doneButton = element("close-button");
        deleteButton = element("delete-button");
        Element nameElement = element("decoration-name");
        if (nameElement != null) {
            BaseDisplay display = ClientAssetsManager.INSTANCE.getDecorationDisplay(decoration.getDisplayId()).orElse(null);
            String name = display != null && display.getDescription() != null && !display.getDescription().isBlank()
                    ? net.minecraft.network.chat.Component.translatable(display.getDescription()).getString()
                    : String.valueOf(decoration.getDisplayId());
            nameElement.setTextContent(name);
        }

        bindNumber(scaleInput, decoration.getScale(), decoration::setScale);
        bindNumber(xOffsetInput, decoration.getXOffset(), decoration::setXOffset);
        bindNumber(yOffsetInput, decoration.getYOffset(), decoration::setYOffset);
        bindNumber(zOffsetInput, decoration.getZOffset(), decoration::setZOffset);
        bindNumber(xRotationInput, decoration.getXRot(), decoration::setXRot);
        bindNumber(yRotationInput, decoration.getYRot(), decoration::setYRot);
        bindNumber(zRotationInput, decoration.getZRot(), decoration::setZRot);
        if (doneButton != null) {
            doneButton.addEventListener("click", event -> onClose());
        }
        if (deleteButton != null) {
            deleteButton.addEventListener("click", event -> {
                serverUpdatePending = false;
                removeDecoration();
                onClose();
            });
        }
    }

    private Element element(String id) {
        return document.getElementById(id);
    }

    private void bindNumber(Element input, float initial, FloatConsumer consumer) {
        if (input == null) {
            return;
        }
        input.setValue(String.format(Locale.ROOT, "%.2f", initial));
        updateRangeValue(input, true);
        input.addEventListener("input", event -> applyNumber(input, consumer, initial, false));
        input.addEventListener("change", event -> applyNumber(input, consumer, initial, true));
        if ("range".equalsIgnoreCase(input.getAttribute("type"))) {
            Element valueInput = rangeValueInput(input);
            if (valueInput != null) {
                valueInput.addEventListener("input", event -> applyRangeValueInput(input, valueInput, consumer, false));
                valueInput.addEventListener("change", event -> applyRangeValueInput(input, valueInput, consumer, true));
            }
            input.addEventListener("mousedown", event -> sliderDragging = true);
            input.addEventListener("mouseup", event -> sliderDragging = false);
            input.addEventListener("blur", event -> sliderDragging = false);
        }
    }

    private void applyNumber(Element input, FloatConsumer consumer, float fallback, boolean commit) {
        float value = roundToHundredth(parseSafe(input.getValue(), fallback));
        consumer.accept(value);
        if (commit && !"range".equalsIgnoreCase(input.getAttribute("type"))) {
            input.setValue(String.format(Locale.ROOT, "%.2f", value));
        }
        updateRangeValue(input, commit);
        if (commit) {
            requestServerUpdate();
        }
    }

    private void updateRangeValue(Element input, boolean force) {
        if (!"range".equalsIgnoreCase(input.getAttribute("type"))) {
            return;
        }
        Element valueInput = rangeValueInput(input);
        if (valueInput != null) {
            String text = String.format(Locale.ROOT, "%.2f", parseSafe(input.getValue(), 0));
            long now = System.nanoTime();
            if (!force && now - lastRangeValueUpdateNanos < 33_000_000L) {
                return;
            }
            if (!text.equals(valueInput.getValue())) {
                valueInput.setValue(text);
            }
            lastRangeValueUpdateNanos = now;
        }
    }

    private Element rangeValueInput(Element rangeInput) {
        return document.getElementById(rangeInput.getAttribute("id") + "-value");
    }

    private void applyRangeValueInput(Element rangeInput, Element valueInput, FloatConsumer consumer, boolean commit) {
        float value;
        try {
            value = roundToHundredth(Mth.clamp(Float.parseFloat(valueInput.getValue()), -180.0F, 180.0F));
        } catch (NumberFormatException exception) {
            return;
        }
        rangeInput.setValue(Float.toString(value));
        consumer.accept(value);
        if (commit) {
            valueInput.setValue(String.format(Locale.ROOT, "%.2f", value));
            requestServerUpdate();
        }
    }

    private float roundToHundredth(float value) {
        return Math.round(value * 100.0F) / 100.0F;
    }

    private float parseSafe(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (serverUpdatePending) {
            if (serverUpdateCooldown > 0) {
                serverUpdateCooldown--;
            } else {
                serverUpdatePending = false;
                serverUpdateCooldown = 2;
                updateServer();
            }
        }
    }

    private void requestServerUpdate() {
        serverUpdatePending = true;
    }

    private void updateServer() {
        ClientDecorationAction update = new ClientDecorationAction();
        update.action = ClientDecorationAction.Action.SET;
        update.pos = decoration.getBlockPos();
        update.scale = decoration.getScale();
        update.xOffset = decoration.getXOffset();
        update.yOffset = decoration.getYOffset();
        update.zOffset = decoration.getZOffset();
        update.xRot = decoration.getXRot();
        update.yRot = decoration.getYRot();
        update.zRot = decoration.getZRot();
        Channel.CHANNEL.sendToServer(update);
    }

    private void removeDecoration() {
        ClientDecorationAction update = new ClientDecorationAction();
        update.action = ClientDecorationAction.Action.REMOVE;
        update.pos = decoration.getBlockPos();
        Channel.CHANNEL.sendToServer(update);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderPreview(graphics);
    }

    private void renderPreview(GuiGraphics graphics) {
        Bounds bounds = getPreviewBounds();
        if (bounds == null) {
            return;
        }
        BaseDisplay display = ClientAssetsManager.INSTANCE.getDecorationDisplay(decoration.getDisplayId()).orElse(null);
        if (display == null) {
            return;
        }
        DecorationBedrockModel model = display.getModel();
        ResourceLocation texture = display.getTexture();
        if (model == null || texture == null || !model.hasBakedModel()) {
            return;
        }
        updateRenderState();
        float scale = (float) (Math.min(bounds.width(), bounds.height()) / 2.6D) * viewScale;
        graphics.enableScissor((int) bounds.left, (int) bounds.top, (int) Math.ceil(bounds.right), (int) Math.ceil(bounds.bottom));
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        try {
            poseStack.translate(bounds.centerX() + viewShiftX, bounds.centerY() + viewShiftY, 512.0D);
            poseStack.mulPoseMatrix(new Matrix4f().scaling(scale, scale, -scale));
            poseStack.mulPose(Axis.XP.rotationDegrees(viewRotX));
            poseStack.mulPose(Axis.YP.rotationDegrees(viewRotY));
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            VertexConsumer lineBuffer = graphics.bufferSource().getBuffer(RenderType.lines());
            LevelRenderer.renderLineBox(poseStack, lineBuffer, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D, 0.35F, 1.0F, 0.45F, 1.0F);
            poseStack.pushPose();
            try {
                poseStack.translate(0.5D + renderXOffset, renderYOffset, 0.5D + renderZOffset);
                poseStack.scale(renderScale, renderScale, renderScale);
                poseStack.mulPose(Axis.YP.rotationDegrees(-renderYRotation));
                poseStack.mulPose(Axis.XP.rotationDegrees(renderXRotation));
                poseStack.mulPose(Axis.ZP.rotationDegrees(renderZRotation));
                Lighting.setupForEntityInInventory();
                RenderSystem.runAsFancy(() -> model.renderToBuffer(poseStack, graphics.bufferSource(), texture, FULL_BRIGHT));
            } finally {
                poseStack.popPose();
            }
            graphics.flush();
        } finally {
            Lighting.setupFor3DItems();
            poseStack.popPose();
            graphics.disableScissor();
        }
    }

    private void updateRenderState() {
        if (sliderDragging) {
            decoration.setXRot(readInputValue(xRotationInput, decoration.getXRot()));
            decoration.setYRot(readInputValue(yRotationInput, decoration.getYRot()));
            decoration.setZRot(readInputValue(zRotationInput, decoration.getZRot()));
        }
        if (!renderStateInitialized) {
            renderScale = decoration.getScale();
            renderXRotation = decoration.getXRot();
            renderYRotation = decoration.getYRot();
            renderZRotation = decoration.getZRot();
            renderXOffset = decoration.getXOffset();
            renderYOffset = decoration.getYOffset();
            renderZOffset = decoration.getZOffset();
            renderStateInitialized = true;
            lastInterpolationNanos = System.nanoTime();
            return;
        }
        float alpha = sliderDragging ? 1.0F : interpolationAlpha();
        renderScale = Mth.lerp(alpha, renderScale, decoration.getScale());
        renderXRotation = Mth.rotLerp(alpha, renderXRotation, decoration.getXRot());
        renderYRotation = Mth.rotLerp(alpha, renderYRotation, decoration.getYRot());
        renderZRotation = Mth.rotLerp(alpha, renderZRotation, decoration.getZRot());
        renderXOffset = Mth.lerp(alpha, renderXOffset, decoration.getXOffset());
        renderYOffset = Mth.lerp(alpha, renderYOffset, decoration.getYOffset());
        renderZOffset = Mth.lerp(alpha, renderZOffset, decoration.getZOffset());
    }

    private float readInputValue(Element input, float fallback) {
        if (input == null) {
            return fallback;
        }
        return roundToHundredth(parseSafe(input.getValue(), fallback));
    }

    private float interpolationAlpha() {
        long now = System.nanoTime();
        float delta = Math.min(0.1F, Math.max(0.0F, (now - lastInterpolationNanos) / 1_000_000_000.0F));
        lastInterpolationNanos = now;
        return 1.0F - (float) Math.exp(-48.0F * delta);
    }

    private Bounds getPreviewBounds() {
        if (previewAnchor == null || document == null) {
            return null;
        }
        Element.DOMRect rect = previewAnchor.getBoundingClientRect();
        if (rect.width <= 0 || rect.height <= 0) {
            return null;
        }
        double scale = document.getViewport().renderScale();
        return new Bounds(rect.x * scale, rect.y * scale, (rect.x + rect.width) * scale, (rect.y + rect.height) * scale);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Bounds bounds = getPreviewBounds();
        if (bounds != null && bounds.contains(mouseX, mouseY)) {
            viewScale = Mth.clamp(viewScale * (float) Math.pow(1.1D, delta), 0.1F, 10.0F);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Bounds bounds = getPreviewBounds();
        if ((button == 0 || button == 1) && bounds != null && bounds.contains(mouseX, mouseY)) {
            previewDragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && previewDragging) {
            viewRotY += (float) dragX;
            viewRotX += (float) dragY;
            return true;
        }
        if (button == 1 && previewDragging) {
            viewShiftX += (float) dragX;
            viewShiftY += (float) dragY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            boolean wasDragging = sliderDragging;
            sliderDragging = false;
            if (wasDragging) {
                requestServerUpdate();
            }
        }
        if (previewDragging && (button == 0 || button == 1)) {
            previewDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        if (serverUpdatePending) {
            serverUpdatePending = false;
            serverUpdateCooldown = 0;
            updateServer();
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @FunctionalInterface
    private interface FloatConsumer {
        void accept(float value);
    }

    private record Bounds(double left, double top, double right, double bottom) {

        private boolean contains(double x, double y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }

        private double width() {
            return right - left;
        }

        private double height() {
            return bottom - top;
        }

        private double centerX() {
            return (left + right) / 2.0D;
        }

        private double centerY() {
            return (top + bottom) / 2.0D;
        }

    }

}
