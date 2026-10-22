package com.malafi.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** الواجهة التعريفية عند أول تشغيل */
public class OnboardingActivity extends Activity {
    private static final String[] EMOJI = {"", "⏰", "✅", "💼", "👋"};
    private static final String[] TITLES = {
            "مرحباً بك في ملفي",
            "استيقظ بذكاء",
            "مهامك اليومية",
            "مواعيد العمل",
            "كيف نناديك؟"};
    private static final String[] BODIES = {
            "رفيقك اليومي لتنظيم وقتك: منبه للاستيقاظ، مهام يومية، ومواعيد عمل في تطبيق واحد أنيق.",
            "منبهات بصوت متدرج واهتزاز، تكرار حسب الأيام التي تختارها، وغفوة مرنة تناسب صباحك.",
            "أنشئ مهامك مع تذكير في وقتها، وتابع تقدّمك اليومي وحافظ على سلسلة إنجازاتك 🔥",
            "حدّد أيام ووقت دوامك وسنذكّرك قبل الموعد بالمدة التي تناسبك، فلا يفوتك شيء.",
            "اكتب اسمك لنخصّص لك التحية. سنطلب أيضاً إذن الإشعارات حتى تصلك التذكيرات."};
    private static final int[][] COLORS = {
            {0xFF6C5CE7, 0xFF00C9B1},
            {0xFF6C5CE7, 0xFFB06CFF},
            {0xFF00C9B1, 0xFF3FA2FF},
            {0xFFFF8A4C, 0xFFFF5C7A},
            {0xFFFFB84D, 0xFFFF8A4C}};
    private static final int N = 5;

    private int page = 0;
    private FrameLayout container;
    private LinearLayout dots;
    private TextView next, skip;
    private EditText nameInput;
    private boolean replay;
    private GestureDetector gd;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Theme.apply(Store.dark(this));
        replay = getIntent().getBooleanExtra("replay", false);
        getWindow().setStatusBarColor(Theme.bg);
        getWindow().setNavigationBarColor(Theme.bg);
        int flags = 0;
        if (!Theme.dark) flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        LinearLayout root = Ui.vbox(this);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(Theme.bg);
        root.setPadding(Ui.dp(this, 28), Ui.dp(this, 24), Ui.dp(this, 28), Ui.dp(this, 24));

        // شريط علوي: تخطي
        skip = Ui.tv(this, "تخطي", 15, Theme.sub, true);
        skip.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show(N - 1, -1);
            }
        });
        LinearLayout top = Ui.hbox(this);
        top.setGravity(Gravity.START);
        top.addView(skip);
        root.addView(top, Ui.lp(Ui.MATCH, Ui.WRAP));

        container = new FrameLayout(this);
        root.addView(container, new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));

        dots = Ui.hbox(this);
        dots.setGravity(Gravity.CENTER);
        root.addView(dots, Ui.lp(this, Ui.MATCH, Ui.WRAP, 0, 8, 0, 22));

        next = Ui.button(this, "التالي");
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page < N - 1) show(page + 1, -1);
                else finishOnboarding();
            }
        });
        root.addView(next, Ui.lp(Ui.MATCH, Ui.WRAP));
        setContentView(root);

        gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent a, MotionEvent b2, float vx, float vy) {
                if (Math.abs(vx) < 800 || Math.abs(vx) < Math.abs(vy)) return false;
                if (vx > 0 && page < N - 1) show(page + 1, -1); // سحب لليمين = التالي (RTL)
                else if (vx < 0 && page > 0) show(page - 1, 1);
                return true;
            }
        });
        show(0, 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gd.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private void show(int idx, int dir) {
        hideKeyboard();
        page = idx;
        container.removeAllViews();
        View v = buildPage(idx);
        container.addView(v, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        if (dir != 0) {
            v.setAlpha(0f);
            v.setTranslationX(dir * Ui.dp(this, 60));
            v.animate().alpha(1f).translationX(0).setDuration(300).start();
        }
        next.setText(idx == N - 1 ? "ابدأ الآن 🚀" : "التالي");
        skip.setVisibility(idx == N - 1 ? View.INVISIBLE : View.VISIBLE);
        updateDots();
    }

    private void updateDots() {
        dots.removeAllViews();
        for (int i = 0; i < N; i++) {
            View d = new View(this);
            boolean cur = i == page;
            d.setBackground(Ui.round(this, cur ? Theme.PRIMARY : Theme.line, 5));
            LinearLayout.LayoutParams p = Ui.lp(this, cur ? 26 : 9, 9, 4, 0, 4, 0);
            p.width = Ui.dp(this, cur ? 26 : 9);
            p.height = Ui.dp(this, 9);
            dots.addView(d, p);
        }
    }

    private View buildPage(int i) {
        LinearLayout l = Ui.vbox(this);
        l.setGravity(Gravity.CENTER);

        if (i == 0) {
            LogoView logo = new LogoView(this, true);
            logo.setElevation(Ui.dp(this, 10));
            l.addView(logo, Ui.lp(Ui.dp(this, 150), Ui.dp(this, 150)));
        } else {
            TextView e = Ui.tv(this, EMOJI[i], 64, Color.WHITE, false);
            e.setGravity(Gravity.CENTER);
            e.setBackground(Ui.grad(this, COLORS[i][0], COLORS[i][1], 40));
            e.setElevation(Ui.dp(this, 8));
            l.addView(e, Ui.lp(Ui.dp(this, 150), Ui.dp(this, 150)));
        }

        TextView t = Ui.tv(this, TITLES[i], 28, Theme.text, true);
        t.setGravity(Gravity.CENTER);
        l.addView(t, Ui.lp(this, Ui.MATCH, Ui.WRAP, 0, 40, 0, 0));

        TextView d = Ui.tv(this, BODIES[i], 16, Theme.sub, false);
        d.setGravity(Gravity.CENTER);
        d.setLineSpacing(0, 1.35f);
        l.addView(d, Ui.lp(this, Ui.MATCH, Ui.WRAP, 8, 14, 8, 0));

        if (i == N - 1) {
            nameInput = new EditText(this);
            nameInput.setHint("اسمك (اختياري)");
            nameInput.setHintTextColor(Theme.sub);
            nameInput.setTextColor(Theme.text);
            nameInput.setTextSize(18);
            nameInput.setGravity(Gravity.CENTER);
            nameInput.setSingleLine(true);
            nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            nameInput.setText(Store.name(this));
            nameInput.setBackground(Ui.stroke(this, Theme.card, Theme.line, 1.5f, 16));
            nameInput.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
            l.addView(nameInput, Ui.lp(this, Ui.MATCH, Ui.WRAP, 8, 28, 8, 0));
        }
        return l;
    }

    private void hideKeyboard() {
        if (nameInput != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(nameInput.getWindowToken(), 0);
        }
    }

    private void finishOnboarding() {
        if (nameInput != null) Store.setName(this, nameInput.getText().toString().trim());
        Store.setOnboarded(this, true);
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
        } else {
            leave();
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        leave();
    }

    private void leave() {
        if (!replay) startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
