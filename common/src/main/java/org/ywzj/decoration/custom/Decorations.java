package org.ywzj.decoration.custom;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;

public final class Decorations {

    private static volatile Map<ResourceLocation, DecorationData> serverData = Map.of();
    private static volatile Map<ResourceLocation, DecorationData> clientData = Map.of();

    private Decorations() {}

    public static void setServerData(Map<ResourceLocation, DecorationData> data) {
        serverData = Map.copyOf(data);
    }

    public static void setClientData(Map<ResourceLocation, DecorationData> data) {
        clientData = Map.copyOf(data);
    }

    public static void clearServerData() {
        serverData = Map.of();
    }

    public static Map<ResourceLocation, DecorationData> getServerData() {
        return serverData;
    }

    public static Map<ResourceLocation, DecorationData> getClientData() {
        return clientData;
    }

    public static DecorationData get(@Nullable ResourceLocation id) {
        if (id == null) {
            return DecorationData.DEFAULT;
        }
        DecorationData data = serverData.get(id);
        if (data == null) {
            data = clientData.get(id);
        }
        return data == null ? DecorationData.DEFAULT : data;
    }

}
