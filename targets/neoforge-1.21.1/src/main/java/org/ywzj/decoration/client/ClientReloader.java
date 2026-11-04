package org.ywzj.decoration.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import org.ywzj.decoration.client.resource.ClientAssetsManager;

public final class ClientReloader {

    private ClientReloader() {}

    public static void reload() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientAssetsManager.INSTANCE.reload(minecraft.getResourceManager());
        if (player != null && player.connection != null) {
            CreativeModeTabs.tryRebuildTabContents(player.connection.enabledFeatures(), true, player.level().registryAccess());
        }
    }
}
