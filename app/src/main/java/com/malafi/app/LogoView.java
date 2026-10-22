package com.malafi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/** شعار «ملفي»: ساعة منبه مع علامة صح — يُرسم برمجياً */
public class LogoView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final boolean withBackground;

    public LogoView(Context c, boolean withBackground) {
        super(c);
        this.withBackground = withBackground;
    }

    @Override
    protected void onDraw(Canvas cv) {
        float s = Math.min(getWidth(), getHeight()) / 108f;
        cv.save();
        cv.scale(s, s);
        if (withBackground) {
            p.setShader(new LinearGradient(0, 0, 108, 108, 0xFF6C5CE7, 0xFF00C9B1, Shader.TileMode.CLAMP));
            p.setStyle(Paint.Style.FILL);
            cv.drawRoundRect(new RectF(0, 0, 108, 108), 28, 28, p);
            p.setShader(null);
        }
        // وجه الساعة
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x33FFFFFF);
        cv.drawCircle(54, 58, 20, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(5);
        p.setColor(0xFFFFFFFF);
        cv.drawCircle(54, 58, 20, p);
        // الجرسان
        p.setStyle(Paint.Style.FILL);
        cv.drawCircle(38, 40, 7, p);
        cv.drawCircle(70, 40, 7, p);
        // القائمتان
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4);
        p.setStrokeCap(Paint.Cap.ROUND);
        cv.drawLine(42, 78, 37, 84, p);
        cv.drawLine(66, 78, 71, 84, p);
        // علامة الصح
        p.setStrokeWidth(5.5f);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setColor(0xFF7CF5E0);
        Path path = new Path();
        path.moveTo(45, 58);
        path.lineTo(52, 65);
        path.lineTo(64, 51);
        cv.drawPath(path, p);
        cv.restore();
    }
}
