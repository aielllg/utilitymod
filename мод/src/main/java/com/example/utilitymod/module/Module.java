package com.example.utilitymod.module;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public final Category category;
    public final List<NumberSetting> settings = new ArrayList<>();

    private boolean enabled;

    /** Анимации (0..1) для GUI и списка на HUD. */
    public float guiAnim = 0f;
    public float hudAnim = 0f;
    public boolean expanded = false;

    protected final MinecraftClient mc = MinecraftClient.getInstance();

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
    }

    protected NumberSetting add(NumberSetting setting) {
        settings.add(setting);
        return setting;
    }

    /** Строка для списка на HUD. */
    public String hudLabel() {
        return name;
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick() {
    }
}
