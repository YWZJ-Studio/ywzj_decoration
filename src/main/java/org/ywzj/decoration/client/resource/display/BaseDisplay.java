package org.ywzj.decoration.client.resource.display;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.BoneIndexProvider;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.baked.BakerOptions;
import net.minecraft.resources.ResourceLocation;
import org.ywzj.decoration.client.resource.ClientAssetsManager;

import javax.annotation.Nullable;
import java.util.*;

public class BaseDisplay {

    protected ResourceLocation displayId;
    protected ResourceLocation modelPath;
    protected BedrockModelPOJO modelPojo;
    protected DecorationBedrockModel model;
    protected ResourceLocation texture;
    protected ResourceLocation slotTexture;
    protected Map<String, BedrockAnimation> animations = Map.of();
    protected String description;
    protected BakerOptions bakerOptions;

    public BaseDisplay() {}

    public BaseDisplay(BaseDisplayPojo pojo) {
        var animationPojo = pojo.animations == null ? null : ClientAssetsManager.INSTANCE.getAnimation(pojo.animations).orElse(null);
        Set<String> animatedBones = animationPojo == null ? Set.of() : BakerOptions.collectAnimatedBones(animationPojo);
        this.modelPath = pojo.model;
        this.bakerOptions = new BakerOptions(animatedBones, new LinkedHashSet<>(animatedBones), new HashSet<>(), true, false, false);
        ClientAssetsManager.INSTANCE.getModel(pojo.model).ifPresent(bedrockModelPOJO -> {
            this.modelPojo = bedrockModelPOJO;
            this.model = new DecorationBedrockModel(bedrockModelPOJO, this.bakerOptions);
        });
        this.texture = pojo.texture;
        this.slotTexture = pojo.slotTexture;

        BoneIndexProvider animationIndexProvider = getAnimationIndexProvider();
        if (animationPojo != null && animationIndexProvider != null) {
            Map<String, BedrockAnimation> map = new HashMap<>();
            for (BedrockAnimation animation : BedrockAnimation.createAnimation(animationPojo, animationIndexProvider)) {
                map.put(animation.getName(), animation);
            }
            this.animations = map;
        } else {
            this.animations = Map.of();
        }
        this.description = pojo.description;
    }

    @Nullable
    protected BoneIndexProvider getAnimationIndexProvider() {
        if (model == null) {
            return null;
        }
        BoneIndexProvider bakedModel = model.getBakedModel();
        return bakedModel != null ? bakedModel : model;
    }

    @Nullable
    public ResourceLocation getDisplayId() {
        return displayId;
    }

    public void setDisplayId(ResourceLocation displayId) {
        this.displayId = displayId;
    }

    @Nullable
    public ResourceLocation getModelPath() {
        return modelPath;
    }

    @Nullable
    public BedrockModelPOJO getModelPojo() {
        return modelPojo;
    }

    @Nullable
    public ResourceLocation getTexture() {
        return texture;
    }

    @Nullable
    public ResourceLocation getSlotTexture() {
        return slotTexture;
    }

    @Nullable
    public DecorationBedrockModel getModel() {
        return model;
    }

    public Map<String, BedrockAnimation> getAnimations() {
        return animations;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

}
