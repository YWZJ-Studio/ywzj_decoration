package org.ywzj.decoration.network;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.network.message.ClientDecorationAction;
import org.ywzj.decoration.network.message.ClientSewingAction;
import org.ywzj.decoration.network.message.ServerDecorationData;

import java.util.Optional;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

public class Channel {

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(YwzjDecoration.modLocation(YwzjDecoration.CHANNEL))
            .networkProtocolVersion(() -> YwzjDecoration.PROTOCOL)
            .clientAcceptedVersions(YwzjDecoration.PROTOCOL::equals)
            .serverAcceptedVersions(YwzjDecoration.PROTOCOL::equals)
            .simpleChannel();

    @SubscribeEvent
    public static void onCommonSetupEvent(FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(PacketId.C_DECORATION_ACTION.value(), ClientDecorationAction.class,
                ClientDecorationAction::encode, ClientDecorationAction::decode,
                ClientDecorationAction::onClientMessageReceived,
                Optional.of(PLAY_TO_SERVER));

        CHANNEL.registerMessage(PacketId.S_DECORATION_DATA.value(), ServerDecorationData.class,
                ServerDecorationData::encode, ServerDecorationData::decode,
                ServerDecorationData::onServerMessageReceived,
                Optional.of(PLAY_TO_CLIENT));

        CHANNEL.registerMessage(PacketId.C_SEWING_ACTION.value(), ClientSewingAction.class,
                ClientSewingAction::encode, ClientSewingAction::decode,
                ClientSewingAction::handle, Optional.of(PLAY_TO_SERVER));
    }

}

enum PacketId {

    C_DECORATION_ACTION(100),
    S_DECORATION_DATA(101),
    C_SEWING_ACTION(102);

    private final int id;

    PacketId(int id) {
        this.id = id;
    }

    int value() {
        return id;
    }

}
