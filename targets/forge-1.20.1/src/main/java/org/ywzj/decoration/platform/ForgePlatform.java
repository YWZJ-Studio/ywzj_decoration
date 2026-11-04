package org.ywzj.decoration.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.ywzj.decoration.network.Channel;

import javax.annotation.Nullable;
import java.nio.file.Path;

public final class ForgePlatform implements DecorationPlatform {

    private final ForgeClientPlatform client =
            FMLEnvironment.dist.isClient() ? new ForgeClientPlatform() : null;

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
        NetworkHooks.openScreen(player, provider, pos);
    }

    @Override
    public void sendToServer(Object payload) {
        Channel.CHANNEL.sendToServer(payload);
    }

    @Override
    public void sendToAll(Object payload) {
        Channel.CHANNEL.send(PacketDistributor.ALL.noArg(), payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Object payload) {
        Channel.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    @Nullable
    public DecorationClientPlatform client() {
        return client;
    }
}
