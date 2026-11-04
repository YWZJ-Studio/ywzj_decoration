package org.ywzj.decoration.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.nio.file.Path;

public final class NeoForgePlatform implements DecorationPlatform {

    private final NeoForgeClientPlatform client =
            FMLEnvironment.dist.isClient() ? new NeoForgeClientPlatform() : null;

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public Path gameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    @Nullable
    public String modVersion(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse(null);
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(provider, pos);
    }

    @Override
    public void sendToServer(Object payload) {
        PacketDistributor.sendToServer((CustomPacketPayload) payload);
    }

    @Override
    public void sendToAll(Object payload) {
        PacketDistributor.sendToAllPlayers((CustomPacketPayload) payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Object payload) {
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) payload);
    }

    @Override
    @Nullable
    public DecorationClientPlatform client() {
        return client;
    }
}
