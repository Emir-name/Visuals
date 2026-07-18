package com.impactvisuals.client.visual;

public class DamageNumber {

    public final double x, y, z;
    public final float amount;
    public final boolean critical;
    public float age = 0f;
    public final float maxAge;

    public DamageNumber(double x, double y, double z, float amount, boolean critical, float maxAge) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.amount = amount;
        this.critical = critical;
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
        return y + (age / maxAge) * 0.9;
    }

    public String text() {
        String rounded = String.format("%.1f", amount);
        return critical ? rounded + "!" : rounded;
    }
}
