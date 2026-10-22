package com.malafi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;

/** حلقة تقدم دائرية مع نص في المنتصف */
public class ProgressRing extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float fraction;
    private final String label;

    public ProgressRing(Context c, float fraction, String label) {
        super(c);
        this.fraction = Math.max(0, Math.min(1, fraction));
        this.label = label;
    }

    @Override
    protected void onDraw(Canvas cv) {
        float w = getWidth(), h = getHeight();
        float sw = Ui.dp(getContext(), 9);
        RectF r = new RectF(sw / 2, sw / 2, w - sw / 2, h - sw / 2);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(sw);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(Theme.card2);
        cv.drawArc(r, 0, 360, false, p);
        p.setColor(Theme.ACCENT);
        if (fraction > 0) cv.drawArc(r, -90, 360 * fraction, false, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Theme.text);
        p.setTextAlign(Paint.Align.CENTER);
        p.setFakeBoldText(true);
        p.setTextSize(Ui.dp(getContext(), 18));
        cv.drawText(label, w / 2, h / 2 + Ui.dp(getContext(), 6), p);
    }
}
