package com.example.utilitymod.gui;

import com.example.utilitymod.module.Category;
import com.example.utilitymod.module.Module;
import com.example.utilitymod.module.ModuleManager;
import com.example.utilitymod.module.NumberSetting;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * ClickGUI: панели по категориям (перетаскиваются за заголовок).
 * ЛКМ по модулю - включить/выключить, ПКМ - раскрыть настройки (ползунки).
 * Right Shift или Esc - закрыть.
 */
public class ClickGuiScreen extends Screen {

    private static final int PANEL_W = 134;
    private static final int HEADER_H = 22;
    private static final int ROW_H = 18;
    private static final int SET_H = 22;

    private static final int COL_PANEL = 0xF0121218;
    private static final int COL_HEADER = 0xFF1A1A24;
    private static final int COL_TEXT_OFF = 0xFF9A9AA8;
    private static final int COL_TEXT_ON = 0xFFFFFFFF;

    private static class Panel {
        final Category category;
        int x;
        int y;

        Panel(Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
        }
    }

    /** Позиции панелей сохраняются между открытиями меню. */
    private static final List<Panel> PANELS = new ArrayList<>();

    private long openTime;
    private Panel draggingPanel;
    private int dragOffX;
    private int dragOffY;

    private NumberSetting draggingSetting;
    private int sliderX;
    private int sliderW;

    public ClickGuiScreen() {
        super(new LiteralText("Utility Mod"));
    }

    @Override
    protected void init() {
        openTime = System.currentTimeMillis();
        if (PANELS.isEmpty()) {
            int total = Category.values().length;
            int startX = Math.max(10, this.width / 2 - (total * (PANEL_W + 10) - 10) / 2);
            int i = 0;
            for (Category c : Category.values()) {
                PANELS.add(new Panel(c, startX + i * (PANEL_W + 10), 50));
                i++;
            }
        }
    }

    private float ease() {
        float t = Math.min(1f, (System.currentTimeMillis() - openTime) / 260f);
        float inv = 1f - t;
        return 1f - inv * inv * inv;
    }

    private int panelHeight(Panel p) {
        int h = HEADER_H;
        for (Module m : ModuleManager.byCategory(p.category)) {
            h += ROW_H;
            if (m.expanded) {
                h += m.settings.size() * SET_H;
            }
        }
        return h + 4;
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float delta) {
        float e = ease();

        // Затемнение фона
        this.fillGradient(ms, 0, 0, this.width, this.height,
                Render2D.withAlpha(0xD0060612, e), Render2D.withAlpha(0xE0181030, e));

        // Заголовок
        ms.push();
        ms.translate(this.width / 2f, 14 - (1f - e) * 10f, 0);
        ms.scale(1.7f, 1.7f, 1f);
        String title = "UTILITY MOD";
        int tw = this.textRenderer.getWidth(title);
        Render2D.gradientText(ms, this.textRenderer, title, -tw / 2f, 0, 18f, 0f, e);
        ms.pop();

        String hoveredDescription = null;

        for (Panel p : PANELS) {
            int px = p.x;
            int py = p.y + (int) ((1f - e) * -24f);
            int ph = panelHeight(p);

            // Тень и фон панели
            Render2D.rounded(ms, px - 1, py - 1, px + PANEL_W + 1, py + ph + 1, 5, Render2D.withAlpha(0x40000000, e));
            Render2D.rounded(ms, px, py, px + PANEL_W, py + ph, 4, Render2D.withAlpha(COL_PANEL, e));
            Render2D.rounded(ms, px, py, px + PANEL_W, py + HEADER_H, 4, Render2D.withAlpha(COL_HEADER, e));
            Render2D.accentLine(ms, px + 4, py + HEADER_H - 1, px + PANEL_W - 4, e);

            this.textRenderer.drawWithShadow(ms, p.category.title,
                    px + 8, py + 7, Render2D.withAlpha(COL_TEXT_ON, e));

            int y = py + HEADER_H + 2;
            int index = 0;
            for (Module m : ModuleManager.byCategory(p.category)) {
                m.guiAnim += ((m.isEnabled() ? 1f : 0f) - m.guiAnim) * 0.25f;

                boolean hover = mouseX >= px && mouseX < px + PANEL_W && mouseY >= y && mouseY < y + ROW_H;
                if (hover) {
                    hoveredDescription = m.description;
                    DrawableHelper.fill(ms, px + 3, y, px + PANEL_W - 3, y + ROW_H,
                            Render2D.withAlpha(0x22FFFFFF, e));
                }

                // Плавная "заливка" включённого модуля
                int accent = Render2D.accent(index * 35f + p.y);
                int fillW = (int) ((PANEL_W - 6) * m.guiAnim);
                if (fillW > 0) {
                    DrawableHelper.fill(ms, px + 3, y, px + 3 + fillW, y + ROW_H,
                            Render2D.withAlpha(accent, 0.28f * e));
                    DrawableHelper.fill(ms, px + 3, y + 1, px + 5, y + ROW_H - 1,
                            Render2D.withAlpha(accent, e));
                }

                int textColor = Render2D.lerpColor(COL_TEXT_OFF, COL_TEXT_ON, m.guiAnim);
                this.textRenderer.drawWithShadow(ms, m.name, px + 10, y + 5, Render2D.withAlpha(textColor, e));

                if (!m.settings.isEmpty()) {
                    String mark = m.expanded ? "-" : "+";
                    this.textRenderer.drawWithShadow(ms, mark,
                            px + PANEL_W - 12, y + 5, Render2D.withAlpha(COL_TEXT_OFF, e));
                }
                y += ROW_H;

                if (m.expanded) {
                    for (NumberSetting s : m.settings) {
                        this.textRenderer.drawWithShadow(ms, s.name, px + 10, y + 3,
                                Render2D.withAlpha(0xFFC8C8D4, e));
                        String val = s.display();
                        this.textRenderer.drawWithShadow(ms, val,
                                px + PANEL_W - 10 - this.textRenderer.getWidth(val), y + 3,
                                Render2D.withAlpha(accent, e));

                        int sx = px + 10;
                        int sw = PANEL_W - 20;
                        int sy = y + 14;
                        Render2D.rounded(ms, sx, sy, sx + sw, sy + 4, 2, Render2D.withAlpha(0xFF2A2A36, e));
                        int filled = Math.max(4, (int) (sw * s.progress()));
                        Render2D.rounded(ms, sx, sy, sx + filled, sy + 4, 2, Render2D.withAlpha(accent, e));
                        Render2D.rounded(ms, sx + filled - 3, sy - 1, sx + filled + 1, sy + 5, 2,
                                Render2D.withAlpha(0xFFFFFFFF, e));
                        y += SET_H;
                    }
                }
                index++;
            }
        }

        // Подсказка с описанием модуля
        if (hoveredDescription != null) {
            int w = this.textRenderer.getWidth(hoveredDescription) + 14;
            int x = this.width / 2 - w / 2;
            int y = this.height - 30;
            Render2D.rounded(ms, x, y, x + w, y + 16, 3, 0xD0101016);
            this.textRenderer.drawWithShadow(ms, hoveredDescription, x + 7, y + 4, 0xFFFFFFFF);
        }

        String hint = "ЛКМ - вкл/выкл   ПКМ - настройки   Right Shift - закрыть";
        this.textRenderer.drawWithShadow(ms, hint,
                this.width / 2f - this.textRenderer.getWidth(hint) / 2f, this.height - 12,
                Render2D.withAlpha(0xFF8888A0, e));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = PANELS.size() - 1; i >= 0; i--) {
            Panel p = PANELS.get(i);
            int px = p.x;
            int py = p.y;
            int ph = panelHeight(p);
            if (mx < px || mx >= px + PANEL_W || my < py || my >= py + ph) {
                continue;
            }

            // Заголовок: перетаскивание
            if (my < py + HEADER_H) {
                if (button == 0) {
                    draggingPanel = p;
                    dragOffX = (int) mx - p.x;
                    dragOffY = (int) my - p.y;
                    // панель наверх
                    PANELS.remove(p);
                    PANELS.add(p);
                }
                return true;
            }

            int y = py + HEADER_H + 2;
            for (Module m : ModuleManager.byCategory(p.category)) {
                if (my >= y && my < y + ROW_H) {
                    if (button == 0) {
                        m.toggle();
                    } else if (button == 1 && !m.settings.isEmpty()) {
                        m.expanded = !m.expanded;
                    }
                    return true;
                }
                y += ROW_H;

                if (m.expanded) {
                    for (NumberSetting s : m.settings) {
                        if (button == 0 && my >= y && my < y + SET_H) {
                            draggingSetting = s;
                            sliderX = px + 10;
                            sliderW = PANEL_W - 20;
                            updateSlider(mx);
                            return true;
                        }
                        y += SET_H;
                    }
                }
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private void updateSlider(double mx) {
        if (draggingSetting == null) return;
        double t = (mx - sliderX) / sliderW;
        t = Math.max(0.0, Math.min(1.0, t));
        draggingSetting.set(draggingSetting.min + t * (draggingSetting.max - draggingSetting.min));
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingPanel != null && button == 0) {
            draggingPanel.x = Math.max(0, Math.min(this.width - PANEL_W, (int) mx - dragOffX));
            draggingPanel.y = Math.max(0, Math.min(this.height - HEADER_H, (int) my - dragOffY));
            return true;
        }
        if (draggingSetting != null && button == 0) {
            updateSlider(mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingPanel = null;
        draggingSetting = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
