package org.ywzj.decoration;

import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.ywzj.decoration.all.AllBlockEntities;
import org.ywzj.decoration.all.AllBlocks;
import org.ywzj.decoration.all.AllItems;
import org.ywzj.decoration.all.AllMenus;
import org.ywzj.decoration.all.AllRecipes;
import org.ywzj.decoration.all.AllTabs;
import org.ywzj.decoration.network.Channel;
import org.ywzj.decoration.platform.DecorationPlatforms;
import org.ywzj.decoration.platform.NeoForgePlatform;
import org.ywzj.decoration.resource.DecorationPackManager;

@Mod(YwzjDecoration.MOD_ID)
public final class YwzjDecorationNeoForge121 {

    public YwzjDecorationNeoForge121(IEventBus modEventBus) {
        DecorationPlatforms.install(new NeoForgePlatform());
        DecorationPackManager.INSTANCE.initialize(
                FMLEnvironment.dist.isClient() ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA);

        AllBlocks.register(modEventBus);
        AllBlockEntities.register(modEventBus);
        AllItems.register(modEventBus);
        AllMenus.register(modEventBus);
        AllRecipes.register(modEventBus);
        AllTabs.register(modEventBus);
        modEventBus.register(Channel.class);
    }
}
