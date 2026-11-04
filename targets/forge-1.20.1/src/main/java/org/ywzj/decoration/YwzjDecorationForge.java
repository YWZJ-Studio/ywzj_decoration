package org.ywzj.decoration;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraft.server.packs.PackType;
import org.ywzj.decoration.all.AllBlockEntities;
import org.ywzj.decoration.all.AllBlocks;
import org.ywzj.decoration.all.AllItems;
import org.ywzj.decoration.all.AllMenus;
import org.ywzj.decoration.all.AllRecipes;
import org.ywzj.decoration.all.AllTabs;
import org.ywzj.decoration.network.Channel;
import org.ywzj.decoration.platform.DecorationPlatforms;
import org.ywzj.decoration.platform.ForgePlatform;
import org.ywzj.decoration.resource.DecorationPackManager;

@Mod(YwzjDecoration.MOD_ID)
public final class YwzjDecorationForge {

    @SuppressWarnings("removal")
    public YwzjDecorationForge() {
        DecorationPlatforms.install(new ForgePlatform());

        Dist side = FMLLoader.getDist();
        DecorationPackManager.INSTANCE.initialize(
                side.isClient() ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA);

        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        IEventBus modEventBus = context.getModEventBus();
        AllBlocks.register(modEventBus);
        AllBlockEntities.register(modEventBus);
        AllItems.register(modEventBus);
        AllMenus.register(modEventBus);
        AllRecipes.register(modEventBus);
        AllTabs.register(modEventBus);
        modEventBus.register(Channel.class);
    }
}
