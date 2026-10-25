package com.example.utilitymod.module;

/** Числовая настройка модуля (ползунок в GUI). */
public class NumberSetting {
    public final String name;
    public final double min;
    public final double max;
    public final double step;
    private double value;

    public NumberSetting(String name, double value, double min, double max, double step) {
        this.name = name;
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double get() {
        return value;
    }

    public float getFloat() {
        return (float) value;
    }

    public void set(double v) {
        v = Math.max(min, Math.min(max, v));
        if (step > 0) {
            v = Math.round(v / step) * step;
        }
        this.value = Math.max(min, Math.min(max, v));
    }

    public double progress() {
        return (value - min) / (max - min);
    }

    public String display() {
        if (step >= 1.0) {
            return String.valueOf((int) Math.round(value));
        }
        return String.format("%.1f", value).replace(',', '.');
    }
}
