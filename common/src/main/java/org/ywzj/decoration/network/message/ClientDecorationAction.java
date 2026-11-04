package org.ywzj.decoration.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.ywzj.decoration.blockentity.DecorationBlockEntity;
import org.ywzj.decoration.pojo.DecorationAction;
import org.ywzj.decoration.registry.DecorationItems;

public class ClientDecorationAction extends DecorationAction {

    private static final double MAX_INTERACT_DISTANCE_SQR = 64.0D;

    public ClientDecorationAction() {}

    public static ClientDecorationAction decode(FriendlyByteBuf buf) {
        ClientDecorationAction action = new ClientDecorationAction();
        action.action = buf.readEnum(Action.class);
        action.pos = buf.readBlockPos();
        if (action.action == Action.REMOVE) {
            return action;
        }
        action.scale = buf.readFloat();
        action.xOffset = buf.readFloat();
        action.yOffset = buf.readFloat();
        action.zOffset = buf.readFloat();
        action.xRot = buf.readFloat();
        action.yRot = buf.readFloat();
        action.zRot = buf.readFloat();
        return action;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeBlockPos(pos);
        if (action == Action.REMOVE) {
            return;
        }
        buf.writeFloat(scale);
        buf.writeFloat(xOffset);
        buf.writeFloat(yOffset);
        buf.writeFloat(zOffset);
        buf.writeFloat(xRot);
        buf.writeFloat(yRot);
        buf.writeFloat(zRot);
    }

    /**
     * Validates and applies this action on the server. Loader network code only
     * unwraps the sender and calls this method.
     */
    public void handle(ServerPlayer player) {
        if (player == null || player.isSpectator() || pos == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (!level.isLoaded(pos) || !level.mayInteract(player, pos)) {
            return;
        }
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)
                > MAX_INTERACT_DISTANCE_SQR) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof DecorationBlockEntity decoration)) {
            return;
        }
        if (action == Action.REMOVE) {
            ResourceLocation displayId = decoration.getDisplayId();
            ItemStack itemStack = DecorationItems.createDecorationItemStack(displayId);
            level.removeBlock(pos, false);
            if (!itemStack.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), itemStack));
            }
        } else if (action == Action.SET) {
            decoration.applyUpdate(this);
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}
