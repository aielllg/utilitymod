package com.example.utilitymod.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/** Мелкие помощники для 2D-рендера: скруглённые прямоугольники, радужный цвет, градиентный текст. */
public final class Render2D {

    private Render2D() {
    }

    /** Переливающийся акцентный цвет. offset сдвигает оттенок (0..360). */
    public static int accent(float offset) {
        float hue = (((System.currentTimeMillis() / 20) % 360) + offset) % 360f / 360f;
        return 0xFF000000 | MathHelper.hsvToRgb(hue, 0.55f, 1.0f);
    }

    /** Умножает альфу цвета на alpha (0..1). */
    public static int withAlpha(int color, float alpha) {
        int a = (int) (((color >>> 24) & 0xFF) * MathHelper.clamp(alpha, 0f, 1f));
        return (a << 24) | (color & 0x00FFFFFF);
    }

    public static int lerpColor(int from, int to, float t) {
        t = MathHelper.clamp(t, 0f, 1f);
        int a = (int) (((from >>> 24) & 0xFF) + ((((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * t));
        int r = (int) (((from >> 16) & 0xFF) + ((((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t));
        int g = (int) (((from >> 8) & 0xFF) + ((((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t));
        int b = (int) ((from & 0xFF) + (((to & 0xFF) - (from & 0xFF)) * t));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Прямоугольник со скруглёнными углами. */
    public static void rounded(MatrixStack ms, int x1, int y1, int x2, int y2, int r, int color) {
        int w = x2 - x1;
        int h = y2 - y1;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) {
            DrawableHelper.fill(ms, x1, y1, x2, y2, color);
            return;
        }
        DrawableHelper.fill(ms, x1, y1 + r, x2, y2 - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.ceil(r - Math.sqrt(r * r - dy * dy));
            DrawableHelper.fill(ms, x1 + inset, y1 + i, x2 - inset, y1 + i + 1, color);
            DrawableHelper.fill(ms, x1 + inset, y2 - 1 - i, x2 - inset, y2 - i, color);
        }
    }

    /** Горизонтальная линия с переливающимся цветом. */
    public static void accentLine(MatrixStack ms, int x1, int y, int x2, float alpha) {
        for (int x = x1; x < x2; x++) {
            DrawableHelper.fill(ms, x, y, x + 1, y + 1, withAlpha(accent((x - x1) * 2f), alpha));
        }
    }

    /** Текст, где каждая буква имеет свой оттенок. Возвращает ширину. */
    public static int gradientText(MatrixStack ms, TextRenderer font, String text, float x, float y,
                                   float hueStep, float hueOffset, float alpha) {
        float cx = x;
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            font.drawWithShadow(ms, ch, cx, y, withAlpha(accent(hueOffset + i * hueStep), alpha));
            cx += font.getWidth(ch);
        }
        return (int) (cx - x);
    }
}
