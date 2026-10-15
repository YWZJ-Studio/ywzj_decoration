package org.ywzj.decoration.custom;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.network.Channel;
import org.ywzj.decoration.network.message.ServerDecorationData;

@Mod.EventBusSubscriber(modid = YwzjDecoration.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
            Channel.CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), packet);
        } else {
            Channel.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        }
    }

    public static void reload(ResourceManager resourceManager) {
        if (decorationDataManager != null) {
            decorationDataManager.apply(decorationDataManager.prepare(resourceManager, null), resourceManager, null);
        }
        Channel.CHANNEL.send(PacketDistributor.ALL.noArg(), new ServerDecorationData(Decorations.getServerData()));
    }

}
