package org.ywzj.decoration.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.ywzj.decoration.custom.DecorationData;
import org.ywzj.decoration.custom.Decorations;

import java.util.HashMap;
import java.util.Map;

public class ServerDecorationData {

    public Map<ResourceLocation, DecorationData> data = Map.of();

    public ServerDecorationData() {}

    public ServerDecorationData(Map<ResourceLocation, DecorationData> data) {
        this.data = data;
    }

    public static ServerDecorationData decode(FriendlyByteBuf buf) {
        ServerDecorationData message = new ServerDecorationData();
        message.data = buf.readMap(HashMap::new, FriendlyByteBuf::readResourceLocation, ServerDecorationData::readDecorationData);
        return message;
    }

    private static DecorationData readDecorationData(FriendlyByteBuf buf) {
        DecorationData data = new DecorationData();
        data.setWidth(buf.readFloat());
        data.setLength(buf.readFloat());
        return data;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(data, FriendlyByteBuf::writeResourceLocation, (buffer, decorationData) -> {
            buffer.writeFloat(decorationData.getWidth());
            buffer.writeFloat(decorationData.getLength());
        });
    }

    public void handle() {
        Decorations.setClientData(data);
    }
}
