package com.malafi.app;

/** ألوان التطبيق (فاتح / داكن) */
public class Theme {
    public static boolean dark;
    public static int bg, card, card2, text, sub, line;
    public static final int PRIMARY = 0xFF6C5CE7;
    public static final int PRIMARY2 = 0xFF9B8CFF;
    public static final int ACCENT = 0xFF00C9B1;
    public static final int DANGER = 0xFFFF5C7A;
    public static final int WARN = 0xFFFFB84D;

    public static void apply(boolean d) {
        dark = d;
        if (d) {
            bg = 0xFF0E0F1F;
            card = 0xFF181A33;
            card2 = 0xFF23264D;
            text = 0xFFF2F2FF;
            sub = 0xFF9A9CC0;
            line = 0xFF2A2D55;
        } else {
            bg = 0xFFF5F4FF;
            card = 0xFFFFFFFF;
            card2 = 0xFFEEEBFF;
            text = 0xFF1B1B3A;
            sub = 0xFF7A7A99;
            line = 0xFFE6E4F5;
        }
    }

    static {
        apply(false);
    }
}
