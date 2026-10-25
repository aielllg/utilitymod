package com.example.utilitymod.gui;

import com.example.utilitymod.module.Module;
import com.example.utilitymod.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** HUD: ватермарк с FPS/координатами и анимированный список включённых модулей. */
public final class Hud {

    private static int frames = 0;
    private static int fps = 0;
    private static long lastFpsTime = System.currentTimeMillis();

    private Hud() {
    }

    public static void render(MatrixStack ms, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // Счётчик FPS
        frames++;
        long now = System.currentTimeMillis();
        if (now - lastFpsTime >= 1000) {
            fps = frames;
            frames = 0;
            lastFpsTime = now;
        }

        // Анимации обновляем всегда, чтобы модули плавно исчезали
        for (Module m : ModuleManager.all()) {
            float target = m.isEnabled() ? 1f : 0f;
            m.hudAnim += (target - m.hudAnim) * 0.18f;
        }

        if (!ModuleManager.hud.isEnabled() || mc.player == null || mc.options.debugEnabled) {
            return;
        }

        TextRenderer font = mc.textRenderer;
        drawWatermark(ms, mc, font);
        drawModuleList(ms, mc, font);
    }

    private static void drawWatermark(MatrixStack ms, MinecraftClient mc, TextRenderer font) {
        String brand = "UTILITY";
        String info = "  §7" + fps + " FPS  §8|  §7"
                + (int) mc.player.getX() + " " + (int) mc.player.getY() + " " + (int) mc.player.getZ();

        int brandW = font.getWidth(brand);
        int w = brandW + font.getWidth(info) + 12;
        int x = 6;
        int y = 6;
        int h = 16;

        Render2D.rounded(ms, x, y, x + w, y + h, 3, 0xB0101016);
        Render2D.accentLine(ms, x + 3, y + h - 1, x + w - 3, 1f);

        Render2D.gradientText(ms, font, brand, x + 6, y + 4, 25f, 0f, 1f);
        font.drawWithShadow(ms, info, x + 6 + brandW, y + 4, 0xFFFFFFFF);
    }

    private static void drawModuleList(MatrixStack ms, MinecraftClient mc, TextRenderer font) {
        List<Module> list = new ArrayList<>();
        for (Module m : ModuleManager.all()) {
            if (m != ModuleManager.hud && m.hudAnim > 0.02f) {
                list.add(m);
            }
        }
        list.sort(new Comparator<Module>() {
            @Override
            public int compare(Module a, Module b) {
                return Integer.compare(font.getWidth(b.hudLabel()), font.getWidth(a.hudLabel()));
            }
        });

        int screenW = mc.getWindow().getScaledWidth();
        int rowH = 13;
        int y = 4;

        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            String label = m.hudLabel();
            int textW = font.getWidth(label);
            int w = textW + 10;

            float a = m.hudAnim;
            int x = screenW - (int) (w * a) - 2;
            int rowY = y + (int) (i * rowH);
            int color = Render2D.accent(i * 22f);

            Render2D.rounded(ms, x, rowY, x + w, rowY + rowH - 1, 2, Render2D.withAlpha(0xB0101016, a));
            DrawableHelper.fill(ms, x + w - 2, rowY + 1, x + w, rowY + rowH - 2, Render2D.withAlpha(color, a));
            font.drawWithShadow(ms, label, x + 4, rowY + 2, Render2D.withAlpha(color, a));
        }
    }
}
