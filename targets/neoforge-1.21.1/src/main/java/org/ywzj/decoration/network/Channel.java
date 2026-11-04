package org.ywzj.decoration.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.network.message.ClientDecorationAction;
import org.ywzj.decoration.network.message.ClientSewingAction;
import org.ywzj.decoration.network.message.ServerDecorationData;

public final class Channel {

    private Channel() {}

    public static void sendToServer(ClientDecorationAction action) {
        PacketDistributor.sendToServer(new ActionPayload(action));
    }

    public static void sendToServer(ClientSewingAction action) {
        PacketDistributor.sendToServer(new SewingPayload(action));
    }

    public static void sendToAll(ServerDecorationData data) {
        PacketDistributor.sendToAllPlayers(new DataPayload(data));
    }

    public static void sendToPlayer(ServerPlayer player, ServerDecorationData data) {
        PacketDistributor.sendToPlayer(player, new DataPayload(data));
    }

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(YwzjDecoration.PROTOCOL);
        registrar.playToServer(ActionPayload.TYPE, ActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> payload.action.handle((ServerPlayer) context.player())));
        registrar.playToClient(DataPayload.TYPE, DataPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(payload.data::handle));
        registrar.playToServer(SewingPayload.TYPE, SewingPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> payload.action.handle((ServerPlayer) context.player())));
    }

    public record ActionPayload(ClientDecorationAction action) implements CustomPacketPayload {

        public static final Type<ActionPayload> TYPE = new Type<>(YwzjDecoration.modLocation("c_decoration_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ActionPayload> STREAM_CODEC = StreamCodec.of(
                (buffer, payload) -> payload.action.encode(buffer),
                buffer -> new ActionPayload(ClientDecorationAction.decode(buffer)));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DataPayload(ServerDecorationData data) implements CustomPacketPayload {

        public static final Type<DataPayload> TYPE = new Type<>(YwzjDecoration.modLocation("s_decoration_data"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DataPayload> STREAM_CODEC = StreamCodec.of(
                (buffer, payload) -> payload.data.encode(buffer),
                buffer -> new DataPayload(ServerDecorationData.decode(buffer)));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SewingPayload(ClientSewingAction action) implements CustomPacketPayload {

        public static final Type<SewingPayload> TYPE = new Type<>(YwzjDecoration.modLocation("c_sewing_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SewingPayload> STREAM_CODEC = StreamCodec.of(
                (buffer, payload) -> payload.action.encode(buffer),
                buffer -> new SewingPayload(ClientSewingAction.decode(buffer)));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
