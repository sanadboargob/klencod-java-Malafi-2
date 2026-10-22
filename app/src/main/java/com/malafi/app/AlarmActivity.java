package com.malafi.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

/** شاشة رنين المنبه (تظهر فوق شاشة القفل) */
public class AlarmActivity extends Activity {
    public static AlarmActivity current;
    private int id;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        current = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().setStatusBarColor(0xFF3A2C9E);
        getWindow().setNavigationBarColor(0xFF0E0F1F);

        id = getIntent().getIntExtra("id", 0);
        String title = getIntent().getStringExtra("title");
        if (title == null) title = "المنبه";

        FrameLayout root = new FrameLayout(this);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF3A2C9E, 0xFF1A1442, 0xFF0E0F1F}));

        LinearLayout col = Ui.vbox(this);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(Ui.dp(this, 28), Ui.dp(this, 56), Ui.dp(this, 28), Ui.dp(this, 40));

        LogoView logo = new LogoView(this, true);
        col.addView(logo, Ui.lp(Ui.dp(this, 96), Ui.dp(this, 96)));
        ObjectAnimator sx = ObjectAnimator.ofFloat(logo, "scaleX", 1f, 1.12f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(logo, "scaleY", 1f, 1.12f);
        for (ObjectAnimator a : new ObjectAnimator[]{sx, sy}) {
            a.setDuration(700);
            a.setRepeatMode(ValueAnimator.REVERSE);
            a.setRepeatCount(ValueAnimator.INFINITE);
            a.start();
        }

        TextClock clock = new TextClock(this);
        clock.setFormat12Hour("h:mm");
        clock.setFormat24Hour("HH:mm");
        clock.setTextColor(Color.WHITE);
        clock.setTextSize(78);
        clock.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        clock.setIncludeFontPadding(false);
        col.addView(clock, Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 36, 0, 0));

        TextView date = Ui.tv(this, Fmt.dateLine(Calendar.getInstance()), 16, 0xB3FFFFFF, false);
        col.addView(date, Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 8, 0, 0));

        TextView label = Ui.tv(this, title, 26, Color.WHITE, true);
        label.setGravity(Gravity.CENTER);
        col.addView(label, Ui.lp(this, Ui.MATCH, Ui.WRAP, 0, 28, 0, 0));

        View grow = new View(this);
        col.addView(grow, new LinearLayout.LayoutParams(1, 0, 1f));

        final int snoozeMin = Store.snooze(this);
        TextView snooze = Ui.tv(this, "💤  غفوة " + snoozeMin + " دقائق", 18, Color.WHITE, true);
        snooze.setGravity(Gravity.CENTER);
        snooze.setBackground(Ui.ripple(Ui.stroke(this, 0x22FFFFFF, 0x66FFFFFF, 1.5f, 20), 0x44FFFFFF));
        snooze.setPadding(0, Ui.dp(this, 18), 0, Ui.dp(this, 18));
        snooze.setClickable(true);
        snooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id > 0) {
                    Scheduler.snooze(AlarmActivity.this, id, snoozeMin);
                    Toast.makeText(AlarmActivity.this, "سيرن المنبه بعد " + snoozeMin + " دقائق", Toast.LENGTH_SHORT).show();
                }
                AlarmService.stop(AlarmActivity.this);
                finish();
            }
        });
        col.addView(snooze, Ui.lp(this, Ui.MATCH, Ui.WRAP, 0, 0, 0, 14));

        TextView stop = Ui.tv(this, "إيقاف المنبه", 20, 0xFF1B1B3A, true);
        stop.setGravity(Gravity.CENTER);
        stop.setBackground(Ui.ripple(Ui.round(this, Color.WHITE, 20), 0x33000000));
        stop.setPadding(0, Ui.dp(this, 20), 0, Ui.dp(this, 20));
        stop.setClickable(true);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmService.stop(AlarmActivity.this);
                finish();
            }
        });
        col.addView(stop, Ui.lp(Ui.MATCH, Ui.WRAP));

        root.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        setContentView(root);
    }

    @Override
    protected void onDestroy() {
        if (current == this) current = null;
        super.onDestroy();
    }
}
