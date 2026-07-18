package com.impactvisuals.client.visual;

public class HitParticle {

    public final double x, y, z;
    public final float r, g, b;
    public float age = 0f;
    public final float maxAge;

    public HitParticle(double x, double y, double z, float r, float g, float b, float maxAge) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.maxAge = maxAge;
    }

    public boolean tick(float deltaSeconds) {
        age += deltaSeconds;
        return age < maxAge;
    }

    public float alpha() {
        return Math.max(0f, 1f - (age / maxAge));
    }

    public double renderY() {
        return y + (age / maxAge) * 0.4;
    }
}
