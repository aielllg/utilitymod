package com.example.utilitymod;

import com.example.utilitymod.gui.ClickGuiScreen;
import com.example.utilitymod.gui.Hud;
import com.example.utilitymod.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Клиентский мод (Fabric 1.16.5). Right Shift открывает ClickGUI. */
public class UtilityMod implements ClientModInitializer {

    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.utilitymod.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.utilitymod"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.openScreen(new ClickGuiScreen());
                }
            }
            ModuleManager.tick();
        });

        HudRenderCallback.EVENT.register(Hud::render);
    }
}
