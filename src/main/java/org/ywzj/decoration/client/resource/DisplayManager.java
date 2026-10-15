package org.ywzj.decoration.client.resource;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.client.resource.display.BaseDisplay;
import org.ywzj.decoration.client.resource.display.BaseDisplayPojo;
import org.ywzj.decoration.util.GsonUtil;
import org.ywzj.decoration.util.ResourceScanner;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class DisplayManager extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {

    public final Marker marker;
    private final String path;
    private Map<ResourceLocation, BaseDisplay> displayMap = Map.of();

    public DisplayManager(String path) {
        this.path = path;
        this.marker = MarkerManager.getMarker("DisplayManager$" + path);
    }

    @Override
    public Map<ResourceLocation, JsonElement> prepare(ResourceManager manager, ProfilerFiller pProfiler) {
        return ResourceScanner.scanDirectory(manager, "display/" + path, GsonUtil.GSON);
    }

    @Override
    public void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller pProfiler) {
        ImmutableMap.Builder<ResourceLocation, BaseDisplay> displayMapBuilder = ImmutableMap.builder();
        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            try {
                BaseDisplayPojo pojo = GsonUtil.GSON.fromJson(entry.getValue(), BaseDisplayPojo.class);
                if (pojo == null || pojo.model == null || pojo.texture == null) {
                    YwzjDecoration.LOGGER.warn(marker, "Failed to load decoration display: {}", entry.getKey());
                    continue;
                }
                BaseDisplay display = new BaseDisplay(pojo);
                display.setDisplayId(entry.getKey());
                displayMapBuilder.put(entry.getKey(), display);
            } catch (Exception exception) {
                YwzjDecoration.LOGGER.error(marker, "Failed to load decoration display: {}", entry.getKey(), exception);
            }
        }
        displayMap = displayMapBuilder.build();
    }

    public Map<ResourceLocation, BaseDisplay> getDisplayMap() {
        return displayMap;
    }

    public String getPath() {
        return path;
    }

}
