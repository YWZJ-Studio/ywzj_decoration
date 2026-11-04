package org.ywzj.decoration.client.resource;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.AnimationKeyframes;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.CubesItem;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.SoundEffectKeyframes;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.serialize.AnimationKeyframesSerializer;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.serialize.SoundEffectKeyframesSerializer;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.serialize.Vector3fSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

/**
 * Bedrock JSON adapters come from SimpleBedrockModel, which is a target
 * dependency. Shared data uses {@link org.ywzj.decoration.util.GsonUtil}.
 */
public final class BedrockGson {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(CubesItem.class, new CubesItem.Deserializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fSerializer())
            .registerTypeAdapter(AnimationKeyframes.class, new AnimationKeyframesSerializer())
            .registerTypeAdapter(SoundEffectKeyframes.class, new SoundEffectKeyframesSerializer())
            .create();

    private BedrockGson() {}
}
