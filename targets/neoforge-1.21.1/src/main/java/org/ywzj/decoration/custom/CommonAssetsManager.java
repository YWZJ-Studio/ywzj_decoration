package org.ywzj.decoration.custom;

import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.network.Channel;
import org.ywzj.decoration.network.message.ServerDecorationData;

@EventBusSubscriber(modid = YwzjDecoration.MOD_ID)
public class CommonAssetsManager {

    private static DecorationDataManager decorationDataManager;

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        decorationDataManager = new DecorationDataManager();
        event.addListener(decorationDataManager);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        decorationDataManager = null;
        Decorations.clearServerData();
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ServerDecorationData packet = new ServerDecorationData(Decorations.getServerData());
        if (event.getPlayer() != null) {
            Channel.sendToPlayer(event.getPlayer(), packet);
        } else {
            Channel.sendToAll(packet);
        }
    }

    public static void reload(ResourceManager resourceManager) {
        if (decorationDataManager != null) {
            decorationDataManager.apply(decorationDataManager.prepare(resourceManager, null), resourceManager, null);
        }
        Channel.sendToAll(new ServerDecorationData(Decorations.getServerData()));
    }
}
