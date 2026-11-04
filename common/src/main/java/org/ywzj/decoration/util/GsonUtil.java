package org.ywzj.decoration.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;

public final class GsonUtil {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();

    private GsonUtil() {}
}
