package org.ywzj.decoration.pojo;

import net.minecraft.core.BlockPos;

public class DecorationAction {

    public Action action;
    public BlockPos pos;
    public float scale = 1.0F;
    public float xOffset;
    public float yOffset;
    public float zOffset;
    public float xRot;
    public float yRot;
    public float zRot;

    public enum Action {
        SET,
        REMOVE
    }

}
