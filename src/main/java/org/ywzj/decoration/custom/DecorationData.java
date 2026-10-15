package org.ywzj.decoration.custom;

import com.google.gson.annotations.SerializedName;

public class DecorationData {

    public static final DecorationData DEFAULT = new DecorationData();

    @SerializedName("width")
    private float width = 1.0F;

    @SerializedName("length")
    private float length = 1.0F;

    public float getWidth() {
        return Math.max(0.0625F, width);
    }

    public float getLength() {
        return Math.max(0.0625F, length);
    }

    public float getHeight() {
        return Math.max(getWidth(), getLength());
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setLength(float length) {
        this.length = length;
    }

}
