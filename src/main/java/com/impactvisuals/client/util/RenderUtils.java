package com.impactvisuals.client.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public final class RenderUtils {

    private RenderUtils() {}

    public static Vec3d chestPos(Entity entity) {
        return entity.getPos().add(0, entity.getHeight() * 0.6, 0);
    }
}
