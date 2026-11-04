package org.ywzj.decoration.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * Loader-facing services that the shared code needs. One implementation lives
 * in every target; nothing in {@code common} may reference a loader type.
 */
public interface DecorationPlatform {

    boolean isClient();

    Path gameDirectory();

    @Nullable
    String modVersion(String modId);

    void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos);

    void sendToServer(Object payload);

    void sendToAll(Object payload);

    void sendToPlayer(ServerPlayer player, Object payload);

    @Nullable
    DecorationClientPlatform client();
}
