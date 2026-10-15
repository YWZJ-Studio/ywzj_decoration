package org.ywzj.decoration.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import org.ywzj.decoration.all.AllItems;
import org.ywzj.decoration.blockentity.DecorationBlockEntity;
import org.ywzj.decoration.pojo.DecorationAction;

import java.util.function.Supplier;

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

    public static void onClientMessageReceived(ClientDecorationAction message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context context = ctxSupplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.isSpectator()) {
                return;
            }
            if (message.pos == null) {
                return;
            }
            ServerLevel level = player.serverLevel();
            if (!level.isLoaded(message.pos) || !level.mayInteract(player, message.pos)) {
                return;
            }
            if (player.distanceToSqr(message.pos.getX() + 0.5D, message.pos.getY() + 0.5D, message.pos.getZ() + 0.5D)
                    > MAX_INTERACT_DISTANCE_SQR) {
                return;
            }
            BlockEntity blockEntity = level.getBlockEntity(message.pos);
            if (!(blockEntity instanceof DecorationBlockEntity decoration)) {
                return;
            }
            if (message.action == Action.REMOVE) {
                ResourceLocation displayId = decoration.getDisplayId();
                ItemStack itemStack = AllItems.createDecorationItemStack(displayId);
                level.removeBlock(message.pos, false);
                if (!itemStack.isEmpty()) {
                    level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), itemStack));
                }
            } else if (message.action == Action.SET) {
                decoration.applyUpdate(message);
                BlockState state = level.getBlockState(message.pos);
                level.sendBlockUpdated(message.pos, state, state, 3);
            }
        });
    }

}
