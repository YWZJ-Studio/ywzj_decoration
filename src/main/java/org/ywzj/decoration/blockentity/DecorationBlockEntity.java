package org.ywzj.decoration.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.ywzj.decoration.all.AllBlockEntities;
import org.ywzj.decoration.custom.DecorationData;
import org.ywzj.decoration.custom.Decorations;
import org.ywzj.decoration.pojo.DecorationAction;

import javax.annotation.Nullable;

public class DecorationBlockEntity extends BlockEntity {

    public static final String TAG_DISPLAY_ID = "displayId";
    private static final String TAG_SCALE = "scale";
    private static final String TAG_X_OFFSET = "xOffset";
    private static final String TAG_Y_OFFSET = "yOffset";
    private static final String TAG_Z_OFFSET = "zOffset";
    private static final String TAG_X_ROT = "xRot";
    private static final String TAG_Y_ROT = "yRot";
    private static final String TAG_Z_ROT = "zRot";

    @Nullable
    private ResourceLocation displayId;
    private float scale = 1.0F;
    private float xOffset;
    private float yOffset;
    private float zOffset;
    private float xRot;
    private float yRot;
    private float zRot;
    private int animationTrigger;
    private boolean shapeDirty = true;
    @Nullable
    private VoxelShape cachedShape;

    public DecorationBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.DECORATION_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    public ResourceLocation getDisplayId() {
        return displayId;
    }

    public void setDisplayId(@Nullable ResourceLocation displayId) {
        this.displayId = displayId;
        this.shapeDirty = true;
        setChanged();
    }

    public void triggerAnimation() {
        animationTrigger++;
    }

    public int getAnimationTrigger() {
        return animationTrigger;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        this.shapeDirty = true;
    }

    public float getXOffset() {
        return xOffset;
    }

    public void setXOffset(float xOffset) {
        this.xOffset = xOffset;
    }

    public float getYOffset() {
        return yOffset;
    }

    public void setYOffset(float yOffset) {
        this.yOffset = yOffset;
    }

    public float getZOffset() {
        return zOffset;
    }

    public void setZOffset(float zOffset) {
        this.zOffset = zOffset;
    }

    public float getXRot() {
        return xRot;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
    }

    public float getYRot() {
        return yRot;
    }

    public void setYRot(float yRot) {
        this.yRot = yRot;
    }

    public float getZRot() {
        return zRot;
    }

    public void setZRot(float zRot) {
        this.zRot = zRot;
    }

    public void applyUpdate(DecorationAction action) {
        this.scale = action.scale;
        this.xOffset = action.xOffset;
        this.yOffset = action.yOffset;
        this.zOffset = action.zOffset;
        this.xRot = action.xRot;
        this.yRot = action.yRot;
        this.zRot = action.zRot;
        this.shapeDirty = true;
        setChanged();
    }

    public VoxelShape getShape() {
        if (cachedShape == null || shapeDirty) {
            DecorationData data = Decorations.get(displayId);
            float halfWidth = data.getWidth() * Math.abs(scale) / 2.0F;
            float halfLength = data.getLength() * Math.abs(scale) / 2.0F;
            float height = data.getHeight() * Math.abs(scale);
            cachedShape = Shapes.box(0.5D - halfWidth, 0.0D, 0.5D - halfLength,
                    0.5D + halfWidth, height, 0.5D + halfLength);
            shapeDirty = false;
        }
        return cachedShape;
    }

    @Override
    public AABB getRenderBoundingBox() {
        float inflate = Math.abs(scale) + Math.abs(xOffset) + Math.abs(yOffset) + Math.abs(zOffset) + 1.0F;
        return new AABB(worldPosition).inflate(inflate);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (displayId != null) {
            tag.putString(TAG_DISPLAY_ID, displayId.toString());
        }
        tag.putFloat(TAG_SCALE, scale);
        tag.putFloat(TAG_X_OFFSET, xOffset);
        tag.putFloat(TAG_Y_OFFSET, yOffset);
        tag.putFloat(TAG_Z_OFFSET, zOffset);
        tag.putFloat(TAG_X_ROT, xRot);
        tag.putFloat(TAG_Y_ROT, yRot);
        tag.putFloat(TAG_Z_ROT, zRot);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        displayId = tag.contains(TAG_DISPLAY_ID) ? ResourceLocation.tryParse(tag.getString(TAG_DISPLAY_ID)) : null;
        scale = tag.contains(TAG_SCALE) ? tag.getFloat(TAG_SCALE) : 1.0F;
        xOffset = tag.getFloat(TAG_X_OFFSET);
        yOffset = tag.getFloat(TAG_Y_OFFSET);
        zOffset = tag.getFloat(TAG_Z_OFFSET);
        xRot = tag.getFloat(TAG_X_ROT);
        yRot = tag.getFloat(TAG_Y_ROT);
        zRot = tag.getFloat(TAG_Z_ROT);
        shapeDirty = true;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) {
            handleUpdateTag(packet.getTag());
        }
    }

}
