package org.ywzj.decoration.client.resource.display;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;

public class BaseDisplayPojo {

    @SerializedName("model")
    public ResourceLocation model;

    @SerializedName("texture")
    public ResourceLocation texture;

    @SerializedName("slot_texture")
    public ResourceLocation slotTexture;

    @SerializedName("animations")
    public ResourceLocation animations;

    @SerializedName("description")
    public String description;

}
