package org.ywzj.decoration.client.resource;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.ywzj.decoration.client.resource.display.BaseDisplay;
import org.ywzj.decoration.util.GsonUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public enum ClientAssetsManager {

    INSTANCE;

    private JsonDataManager<BedrockModelPOJO> models;
    private JsonDataManager<BedrockAnimationFile> animations;
    private DisplayManager decorationDisplayManager;

    public void registerListeners(Consumer<PreparableReloadListener> consumer) {
        models = new JsonDataManager<>(BedrockModelPOJO.class, GsonUtil.GSON, "models/bedrock", "BedrockModelPojo");
        animations = new JsonDataManager<>(BedrockAnimationFile.class, GsonUtil.GSON, "animations/bedrock", "BedrockAnimationPojo");
        decorationDisplayManager = new DisplayManager("decoration");

        consumer.accept(models);
        consumer.accept(animations);
        consumer.accept(decorationDisplayManager);

        consumer.accept(new SimplePreparableReloadListener<Void>() {

            @Override
            @ParametersAreNonnullByDefault
            protected Void prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                return null;
            }

            @Override
            @ParametersAreNonnullByDefault
            protected void apply(Void pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                models.clearData();
                animations.clearData();
            }

        });
    }

    @OnlyIn(Dist.CLIENT)
    public void reload(ResourceManager resourceManager) {
        models.apply(models.prepare(resourceManager, null), null, null);
        animations.apply(animations.prepare(resourceManager, null), null, null);
        decorationDisplayManager.apply(decorationDisplayManager.prepare(resourceManager, null), null, null);
        decorationDisplayManager.getDisplayMap().values().forEach(display -> {
            try {
                if (display.getTexture() != null) {
                    Minecraft.getInstance().textureManager.register(display.getTexture(), new SimpleTexture(display.getTexture()));
                }
                if (display.getSlotTexture() != null) {
                    Minecraft.getInstance().textureManager.register(display.getSlotTexture(), new SimpleTexture(display.getSlotTexture()));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    @Nullable
    public Map<ResourceLocation, BedrockModelPOJO> getModels() {
        return models != null ? models.getAllData() : null;
    }

    public Optional<BedrockModelPOJO> getModel(@Nullable ResourceLocation id) {
        if (models == null || id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(models.getAllData().get(id));
    }

    @Nullable
    public Map<ResourceLocation, BedrockAnimationFile> getAnimations() {
        return animations != null ? animations.getAllData() : null;
    }

    public Optional<BedrockAnimationFile> getAnimation(@Nullable ResourceLocation id) {
        if (animations == null || id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(animations.getAllData().get(id));
    }

    public Map<ResourceLocation, BaseDisplay> getDecorationDisplayMap() {
        return decorationDisplayManager != null ? decorationDisplayManager.getDisplayMap() : Map.of();
    }

    public Optional<BaseDisplay> getDecorationDisplay(@Nullable ResourceLocation id) {
        if (decorationDisplayManager == null || id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(decorationDisplayManager.getDisplayMap().get(id));
    }

    public List<BaseDisplay> getDecorationDisplays() {
        if (decorationDisplayManager == null) {
            return List.of();
        }
        return new ArrayList<>(decorationDisplayManager.getDisplayMap().values());
    }

}
