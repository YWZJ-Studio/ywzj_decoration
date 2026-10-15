package org.ywzj.decoration.custom;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.ywzj.decoration.util.GsonUtil;
import org.ywzj.decoration.util.ResourceScanner;

import java.util.Map;

public class DecorationDataManager extends SimplePreparableReloadListener<Map<ResourceLocation, DecorationData>> {

    @Override
    public Map<ResourceLocation, DecorationData> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return ResourceScanner.scanDirectory(pResourceManager, FileToIdConverter.json("decorations"), GsonUtil.GSON, DecorationData.class);
    }

    @Override
    public void apply(Map<ResourceLocation, DecorationData> resources, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Decorations.setServerData(resources);
    }

}
