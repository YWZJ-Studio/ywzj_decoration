package org.ywzj.decoration.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.all.AllBlockEntities;
import org.ywzj.decoration.all.AllMenus;
import org.ywzj.decoration.client.render.entity.DecorationBlockRenderer;
import org.ywzj.decoration.client.render.item.DecorationItemRenderer;
import org.ywzj.decoration.client.render.item.PlainTextureItemRenderer;
import org.ywzj.decoration.client.resource.ClientAssetsManager;
import org.ywzj.decoration.client.screen.SewingMachineScreen;
import org.ywzj.decoration.registry.DecorationItems;
import org.ywzj.decoration.registry.DecorationRegistry;

@EventBusSubscriber(modid = YwzjDecoration.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetupHandler {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AllBlockEntities.DECORATION_BLOCK_ENTITY.get(), DecorationBlockRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(AllMenus.SEWING_MACHINE.get(), SewingMachineScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        ClientAssetsManager.INSTANCE.registerListeners(event::registerReloadListener);
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions decorationRenderer = new IClientItemExtensions() {

            private DecorationItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                Minecraft minecraft = Minecraft.getInstance();
                if (renderer == null) {
                    renderer = new DecorationItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                }
                return renderer;
            }
        };
        for (Item item : DecorationItems.decorations().values()) {
            event.registerItem(decorationRenderer, item);
        }

        IClientItemExtensions textureRenderer = new IClientItemExtensions() {

            private PlainTextureItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                Minecraft minecraft = Minecraft.getInstance();
                if (renderer == null) {
                    renderer = new PlainTextureItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                }
                return renderer;
            }
        };
        event.registerItem(textureRenderer, DecorationRegistry.plainTextureItem());
    }
}
