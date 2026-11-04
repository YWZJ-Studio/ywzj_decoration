package org.ywzj.decoration.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.CreativeModeTabs;
import org.ywzj.decoration.blockentity.DecorationBlockEntity;
import org.ywzj.decoration.client.resource.ClientAssetsManager;
import org.ywzj.decoration.client.screen.DecorationSettingsScreen;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class ForgeClientPlatform implements DecorationClientPlatform {

    @Override
    public void openDecorationScreen(DecorationBlockEntity decoration) {
        Minecraft.getInstance().setScreen(new DecorationSettingsScreen(decoration));
    }

    @Override
    @Nullable
    public String displayDescription(@Nullable ResourceLocation displayId) {
        return ClientAssetsManager.INSTANCE.getDecorationDisplay(displayId)
                .map(display -> display.getDescription())
                .filter(description -> description != null && !description.isBlank())
                .orElse(null);
    }

    @Override
    public void registerReloadListeners(Consumer<PreparableReloadListener> consumer) {
        ClientAssetsManager.INSTANCE.registerListeners(consumer);
    }

    @Override
    public void reloadAssets(ResourceManager resourceManager) {
        ClientAssetsManager.INSTANCE.reload(resourceManager);
    }

    @Override
    public void rebuildCreativeTabs(LocalPlayer player) {
        if (player.connection != null) {
            CreativeModeTabs.tryRebuildTabContents(player.connection.enabledFeatures(), true, player.level().registryAccess());
        }
    }
}
