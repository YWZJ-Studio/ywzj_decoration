package org.ywzj.decoration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ywzj.decoration.all.*;
import org.ywzj.decoration.network.Channel;
import org.ywzj.decoration.resource.DecorationPackLoader;

@Mod(YwzjDecoration.MOD_ID)
public class YwzjDecoration {

    public static final String MOD_ID = "ywzj_decoration";
    public static final String PROTOCOL = "2.0";
    public static final String CHANNEL = "ywzj_decoration_channel";
    public static final Logger LOGGER = LogManager.getLogger(YwzjDecoration.class);

    @SuppressWarnings("removal")
    public YwzjDecoration() {
        Dist side = FMLLoader.getDist();
        DecorationPackLoader.INSTANCE.packType = side.isClient() ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
        DecorationPackLoader.INSTANCE.scanDecorationPacks();
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

    @SuppressWarnings("removal")
    public static ResourceLocation modLocation(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    @SuppressWarnings("removal")
    public static ResourceLocation resourceLocation(String location) {
        return new ResourceLocation(location);
    }

}
