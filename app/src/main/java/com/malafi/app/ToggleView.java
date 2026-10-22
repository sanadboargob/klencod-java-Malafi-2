package com.malafi.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

/** مفتاح تبديل عصري مرسوم يدوياً */
public class ToggleView extends View {
    public interface OnChange {
        void onChange(boolean on);
    }

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean on;
    private float prog;
    private OnChange listener;

    public ToggleView(Context c, boolean on, OnChange l) {
        super(c);
        this.on = on;
        this.prog = on ? 1f : 0f;
        this.listener = l;
        setClickable(true);
    }

    public void setOn(boolean v) {
        on = v;
        prog = v ? 1f : 0f;
        invalidate();
    }

    @Override
    protected void onMeasure(int w, int h) {
        setMeasuredDimension(Ui.dp(getContext(), 52), Ui.dp(getContext(), 30));
    }

    private static int mix(int a, int b, float t) {
        int r = (int) (Color.red(a) + (Color.red(b) - Color.red(a)) * t);
        int g = (int) (Color.green(a) + (Color.green(b) - Color.green(a)) * t);
        int bl = (int) (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t);
        return Color.rgb(r, g, bl);
    }

    @Override
    protected void onDraw(Canvas cv) {
        float w = getWidth(), h = getHeight();
        p.setColor(mix(Theme.dark ? 0xFF3A3D6B : 0xFFD6D3EA, Theme.PRIMARY, prog));
        cv.drawRoundRect(new RectF(0, 0, w, h), h / 2, h / 2, p);
        float r = h / 2 - Ui.dp(getContext(), 3);
        float cx = h / 2 + (w - h) * prog;
        p.setColor(Color.WHITE);
        cv.drawCircle(cx, h / 2, r, p);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            on = !on;
            ValueAnimator a = ValueAnimator.ofFloat(prog, on ? 1f : 0f);
            a.setDuration(160);
            a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator v) {
                    prog = (float) v.getAnimatedValue();
                    invalidate();
                }
            });
            a.start();
            if (listener != null) listener.onChange(on);
            performClick();
        }
        return true;
    }
}
