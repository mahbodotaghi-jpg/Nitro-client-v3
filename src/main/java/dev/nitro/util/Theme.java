package dev.nitro.util;

import java.awt.Color;

public class Theme {
    /** Animated cyan -> purple accent colour (ARGB). */
    public static int accent(long timeMs, int offset) {
        float h = 0.62f + 0.13f * (float) Math.sin((timeMs + offset * 40L) / 600.0);
        return Color.HSBtoRGB(h, 0.65f, 1f);
    }

    public static int withAlpha(int argb, int alpha) {
        return (argb & 0x00FFFFFF) | (alpha << 24);
    }
}
