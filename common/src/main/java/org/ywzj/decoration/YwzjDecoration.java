package org.ywzj.decoration;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class YwzjDecoration {

    public static final String MOD_ID = "ywzj_decoration";
    public static final String PROTOCOL = "2.0";
    public static final String CHANNEL = "ywzj_decoration_channel";
    public static final Logger LOGGER = LogManager.getLogger(YwzjDecoration.class);

    private YwzjDecoration() {}

    public static ResourceLocation modLocation(String name) {
        return ResourceLocation.tryParse(MOD_ID + ":" + name);
    }

    public static ResourceLocation resourceLocation(String location) {
        return ResourceLocation.tryParse(location);
    }
}
