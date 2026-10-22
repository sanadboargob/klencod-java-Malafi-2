package com.malafi.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/** أدوات بناء الواجهة برمجياً */
public class Ui {
    public static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable round(Context c, int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    public static GradientDrawable stroke(Context c, int fill, int strokeColor, float strokeDp, float radiusDp) {
        GradientDrawable g = round(c, fill, radiusDp);
        g.setStroke(dp(c, strokeDp), strokeColor);
        return g;
    }

    public static GradientDrawable grad(Context c, int c1, int c2, float radiusDp) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{c1, c2});
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    public static GradientDrawable circle(int color) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        return g;
    }

    public static Drawable ripple(Drawable content, int rippleColor) {
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), content, null);
    }

    public static int alpha(int color, int a) {
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static TextView tv(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setIncludeFontPadding(false);
        t.setLineSpacing(0, 1.15f);
        return t;
    }

    public static LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    public static LinearLayout.LayoutParams lp(Context c, int w, int h, float l, float t, float r, float b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(dp(c, l), dp(c, t), dp(c, r), dp(c, b));
        return p;
    }

    public static LinearLayout vbox(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static LinearLayout hbox(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    /** بطاقة بخلفية مدورة وتأثير لمس */
    public static LinearLayout card(Context c, boolean horizontal) {
        LinearLayout l = horizontal ? hbox(c) : vbox(c);
        int p = dp(c, 16);
        l.setPadding(p, p, p, p);
        l.setBackground(ripple(round(c, Theme.card, 22), alpha(Theme.PRIMARY, 40)));
        l.setElevation(Theme.dark ? 0 : dp(c, 2));
        return l;
    }

    /** زر متدرج كبير */
    public static TextView button(Context c, String label) {
        TextView b = tv(c, label, 16, Color.WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setBackground(ripple(grad(c, Theme.PRIMARY, 0xFF8E5CFF, 18), 0x55FFFFFF));
        b.setPadding(dp(c, 20), dp(c, 16), dp(c, 20), dp(c, 16));
        b.setClickable(true);
        return b;
    }

    public static View spacer(Context c, float h) {
        View v = new View(c);
        v.setLayoutParams(lp(MATCH, dp(c, h)));
        return v;
    }
}
