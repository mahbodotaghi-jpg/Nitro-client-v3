package dev.nitro.gui;

import dev.nitro.NitroClient;
import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.setting.BooleanSetting;
import dev.nitro.setting.NumberSetting;
import dev.nitro.setting.Setting;
import dev.nitro.util.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * Left click module = toggle | right click = settings | middle click = bind key
 * Drag headers to move panels, right click a header to collapse.
 */
public class ClickGuiScreen extends Screen {
    private enum Kind { HEADER, MODULE, SETTING }
    private record Row(Kind kind, Category cat, Module module, Setting<?> setting, int x, int y, int w, int h) {}

    private static final int W = 112, HEAD = 18, ROW = 14, SROW = 13;
    private static final Map<Category, int[]> POS = new EnumMap<>(Category.class);
    private static final Set<Category> COLLAPSED = EnumSet.noneOf(Category.class);
    private static final Set<Module> EXPANDED = new HashSet<>();

    private final List<Row> rows = new ArrayList<>();
    private Category dragCat;
    private int dragDX, dragDY;
    private Row dragSlider;
    private Module listening;

    public ClickGuiScreen() {
        super(Text.literal("NITRO"));
        if (POS.isEmpty()) {
            int i = 0;
            for (Category c : Category.values()) POS.put(c, new int[]{14 + i++ * (W + 8), 14});
        }
    }

    @Override public boolean shouldPause() { return false; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long t = System.currentTimeMillis();
        ctx.fill(0, 0, width, height, 0x70000000);
        ctx.fillGradient(0, height - 90, width, height, 0x00000000, Theme.withAlpha(Theme.accent(t, 0), 0x55));

        rows.clear();
        Module hovered = null;

        for (Category c : Category.values()) {
            int[] p = POS.get(c);
            int x = p[0], y = p[1];

            rows.add(new Row(Kind.HEADER, c, null, null, x, y, W, HEAD));
            ctx.fill(x, y, x + W, y + HEAD, 0xF00D0D14);
            ctx.fill(x, y + HEAD - 1, x + W, y + HEAD, Theme.accent(t, x));
            ctx.drawTextWithShadow(textRenderer, c.display, x + 6, y + 5, 0xFFFFFFFF);
            ctx.drawTextWithShadow(textRenderer, COLLAPSED.contains(c) ? "+" : "-", x + W - 11, y + 5, 0xFF888888);
            y += HEAD;
            if (COLLAPSED.contains(c)) continue;

            int idx = 0;
            for (Module m : NitroClient.modules.getByCategory(c)) {
                rows.add(new Row(Kind.MODULE, c, m, null, x, y, W, ROW));
                boolean hov = mx >= x && mx < x + W && my >= y && my < y + ROW;
                if (hov) hovered = m;

                int bg = m.isEnabled() ? Theme.withAlpha(Theme.accent(t, idx * 8), 0x90) : (hov ? 0xF0222230 : 0xF0151520);
                ctx.fill(x, y, x + W, y + ROW, bg);
                ctx.drawTextWithShadow(textRenderer, m.getName(), x + 6, y + 3, m.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA);

                String bind = listening == m ? "..." : m.getKey() >= 0 ? "[" + keyName(m.getKey()) + "]" : "";
                if (!bind.isEmpty()) {
                    ctx.drawTextWithShadow(textRenderer, bind, x + W - textRenderer.getWidth(bind) - 5, y + 3, 0xFF999999);
                }
                y += ROW; idx++;

                if (EXPANDED.contains(m)) {
                    for (Setting<?> s : m.getSettings()) {
                        rows.add(new Row(Kind.SETTING, c, m, s, x, y, W, SROW));
                        ctx.fill(x, y, x + W, y + SROW, 0xF00A0A10);
                        ctx.fill(x + 2, y, x + 3, y + SROW, Theme.accent(t, y));
                        if (s instanceof BooleanSetting b) {
                            ctx.drawTextWithShadow(textRenderer, s.name, x + 7, y + 3, 0xFFCCCCCC);
                            String v = b.get() ? "ON" : "OFF";
                            ctx.drawTextWithShadow(textRenderer, v, x + W - textRenderer.getWidth(v) - 5, y + 3,
                                    b.get() ? 0xFF55FF88 : 0xFFFF5555);
                        } else if (s instanceof NumberSetting n) {
                            double f = (n.get() - n.min) / (n.max - n.min);
                            ctx.fill(x + 3, y, x + 3 + (int) ((W - 3) * f), y + SROW, Theme.withAlpha(Theme.accent(t, y), 0x55));
                            ctx.drawTextWithShadow(textRenderer, s.name, x + 7, y + 3, 0xFFCCCCCC);
                            String v = String.format("%.1f", n.get());
                            ctx.drawTextWithShadow(textRenderer, v, x + W - textRenderer.getWidth(v) - 5, y + 3, 0xFFFFFFFF);
                        }
                        y += SROW;
                    }
                }
            }
            ctx.fill(x, y, x + W, y + 2, 0xF00D0D14);
        }

        if (hovered != null) {
            String d = hovered.getDescription();
            int tw = textRenderer.getWidth(d);
            ctx.fill(mx + 8, my + 8, mx + 14 + tw, my + 21, 0xF0000000);
            ctx.drawTextWithShadow(textRenderer, d, mx + 11, my + 11, 0xFFFFFFFF);
        }
        String hint = "LMB toggle  |  RMB settings  |  MMB bind  |  drag headers";
        ctx.drawCenteredTextWithShadow(textRenderer, hint, width / 2, height - 14, 0xFFAAAAAA);
    }

    private boolean hit(double mx, double my, Row r) {
        return mx >= r.x() && mx < r.x() + r.w() && my >= r.y() && my < r.y() + r.h();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        for (Row r : rows) {
            if (!hit(mx, my, r)) continue;
            switch (r.kind()) {
                case HEADER -> {
                    if (btn == 0) { dragCat = r.cat(); dragDX = (int) mx - r.x(); dragDY = (int) my - r.y(); }
                    else if (btn == 1 && !COLLAPSED.remove(r.cat())) COLLAPSED.add(r.cat());
                }
                case MODULE -> {
                    Module m = r.module();
                    if (btn == 0) m.toggle();
                    else if (btn == 1 && !m.getSettings().isEmpty() && !EXPANDED.remove(m)) EXPANDED.add(m);
                    else if (btn == 2) listening = m;
                }
                case SETTING -> {
                    if (btn != 0) break;
                    if (r.setting() instanceof BooleanSetting b) b.set(!b.get());
                    else if (r.setting() instanceof NumberSetting) { dragSlider = r; slide(mx); }
                }
            }
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    private void slide(double mx) {
        if (dragSlider == null || !(dragSlider.setting() instanceof NumberSetting n)) return;
        double f = Math.max(0, Math.min(1, (mx - dragSlider.x()) / dragSlider.w()));
        n.set(n.min + f * (n.max - n.min));
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (dragCat != null) {
            int[] p = POS.get(dragCat);
            p[0] = (int) mx - dragDX;
            p[1] = (int) my - dragDY;
            return true;
        }
        if (dragSlider != null) { slide(mx); return true; }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        dragCat = null;
        dragSlider = null;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (listening != null) {
            listening.setKey(key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_BACKSPACE ? -1 : key);
            listening = null;
            return true;
        }
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(key, scan, mods);
    }

    private static String keyName(int key) {
        return InputUtil.fromKeyCode(key, 0).getLocalizedText().getString().toUpperCase();
    }
}
