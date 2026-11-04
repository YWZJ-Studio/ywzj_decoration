package org.ywzj.decoration.platform;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.ywzj.decoration.blockentity.DecorationBlockEntity;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Client-only services. The implementation is only created on a physical
 * client, so shared code must guard calls with {@link DecorationPlatform#isClient()}.
 */
public interface DecorationClientPlatform {

    void openDecorationScreen(DecorationBlockEntity decoration);

    @Nullable
    String displayDescription(@Nullable ResourceLocation displayId);

    void registerReloadListeners(Consumer<PreparableReloadListener> consumer);

    void reloadAssets(ResourceManager resourceManager);

    void rebuildCreativeTabs(LocalPlayer player);
}
