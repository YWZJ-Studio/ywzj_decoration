package org.ywzj.decoration.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.all.AllBlockEntities;
import org.ywzj.decoration.all.AllMenus;
import org.ywzj.decoration.client.render.entity.DecorationBlockRenderer;
import org.ywzj.decoration.client.resource.ClientAssetsManager;
import org.ywzj.decoration.client.screen.SewingMachineScreen;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = YwzjDecoration.MOD_ID)
public class ClientSetupHandler {

    @SuppressWarnings("removal")
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockEntityRenderers.register(
                AllBlockEntities.DECORATION_BLOCK_ENTITY.get(), DecorationBlockRenderer::new));
        event.enqueueWork(() -> MenuScreens.register(AllMenus.SEWING_MACHINE.get(), SewingMachineScreen::new));
    }

    @SubscribeEvent
    public static void onRegisterResourceListener(RegisterClientReloadListenersEvent event) {
        ClientAssetsManager.INSTANCE.registerListeners(event::registerReloadListener);
    }

}
