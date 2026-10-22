package com.malafi.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** الشاشة الرئيسية: الرئيسية / المنبهات / المهام / العمل / الإعدادات */
public class MainActivity extends Activity {
    private static final String[] NAV_ICON = {"🏠", "⏰", "✅", "💼", "⚙️"};
    private static final String[] NAV_LABEL = {"الرئيسية", "المنبهات", "المهام", "العمل", "الإعدادات"};
    private static final String[] QUOTES = {
            "ابدأ يومك بخطوة صغيرة، فالخطوات الصغيرة تصنع الإنجازات الكبيرة.",
            "الوقت كالسيف، إن لم تقطعه قطعك.",
            "لا تؤجّل عمل اليوم إلى الغد.",
            "الانتظام أقوى من الحماس المؤقت.",
            "نظّم وقتك، تنظّم حياتك.",
            "كل إنجاز عظيم بدأ بقرار أن تحاول.",
            "استيقظ بنية جديدة، واصنع يوماً أفضل.",
            "أفضل وقت للبدء هو الآن."};

    private FrameLayout content;
    private TextView fab;
    private TextView[] navIcon = new TextView[5];
    private TextView[] navLabel = new TextView[5];
    private int tab = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (!Store.onboarded(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }
        buildRoot();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (content != null) render();
    }

    // ====================================================== الهيكل العام
    private void buildRoot() {
        Theme.apply(Store.dark(this));
        getWindow().setStatusBarColor(Theme.bg);
        getWindow().setNavigationBarColor(Theme.card);
        int flags = 0;
        if (!Theme.dark) flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        FrameLayout root = new FrameLayout(this);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(Theme.bg);

        LinearLayout col = Ui.vbox(this);
        content = new FrameLayout(this);
        col.addView(content, new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));
        col.addView(buildNav(), Ui.lp(Ui.MATCH, Ui.dp(this, 68)));
        root.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        fab = Ui.tv(this, "+", 32, Color.WHITE, false);
        fab.setGravity(Gravity.CENTER);
        fab.setBackground(Ui.ripple(Ui.grad(this, Theme.PRIMARY, 0xFF8E5CFF, 30), 0x55FFFFFF));
        fab.setElevation(Ui.dp(this, 8));
        fab.setClickable(true);
        fab.setOnClickListener(v -> openEditor(tab == 1 ? Item.ALARM : tab == 2 ? Item.TASK : Item.WORK, null));
        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(Ui.dp(this, 60), Ui.dp(this, 60));
        fp.gravity = Gravity.BOTTOM | Gravity.END;
        fp.setMargins(Ui.dp(this, 20), 0, Ui.dp(this, 20), Ui.dp(this, 68 + 18));
        root.addView(fab, fp);

        setContentView(root);
        render();
    }

    private LinearLayout buildNav() {
        LinearLayout n = Ui.hbox(this);
        n.setBackgroundColor(Theme.card);
        n.setElevation(Ui.dp(this, 10));
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            LinearLayout item = Ui.vbox(this);
            item.setGravity(Gravity.CENTER);
            item.setBackground(Ui.ripple(new ColorDrawable(Color.TRANSPARENT), Ui.alpha(Theme.PRIMARY, 40)));
            navIcon[i] = Ui.tv(this, NAV_ICON[i], 19, Theme.text, false);
            navIcon[i].setGravity(Gravity.CENTER);
            navIcon[i].setPadding(Ui.dp(this, 16), Ui.dp(this, 4), Ui.dp(this, 16), Ui.dp(this, 4));
            navLabel[i] = Ui.tv(this, NAV_LABEL[i], 10.5f, Theme.sub, false);
            item.addView(navIcon[i], Ui.lp(Ui.WRAP, Ui.WRAP));
            item.addView(navLabel[i], Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 3, 0, 0));
            item.setOnClickListener(v -> {
                tab = idx;
                render();
            });
            n.addView(item, new LinearLayout.LayoutParams(0, Ui.MATCH, 1f));
        }
        return n;
    }

    private void updateNav() {
        for (int i = 0; i < 5; i++) {
            boolean sel = i == tab;
            navIcon[i].setBackground(sel ? Ui.round(this, Ui.alpha(Theme.PRIMARY, 50), 16) : null);
            navLabel[i].setTextColor(sel ? Theme.PRIMARY : Theme.sub);
            navLabel[i].setTypeface(null, sel ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }

    private void render() {
        content.removeAllViews();
        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        sv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout col = Ui.vbox(this);
        col.setPadding(Ui.dp(this, 20), Ui.dp(this, 24), Ui.dp(this, 20), Ui.dp(this, 120));
        switch (tab) {
            case 0:
                home(col);
                break;
            case 1:
                alarmsTab(col);
                break;
            case 2:
                tasksTab(col);
                break;
            case 3:
                workTab(col);
                break;
            default:
                settingsTab(col);
        }
        sv.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(sv, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        col.setAlpha(0f);
        col.animate().alpha(1f).setDuration(220).start();
        updateNav();
        fab.setVisibility(tab == 0 || tab == 4 ? View.GONE : View.VISIBLE);
    }

    // ====================================================== مساعدات عامة
    private void pageTitle(LinearLayout col, String title, String sub) {
        col.addView(Ui.tv(this, title, 28, Theme.text, true));
        col.addView(Ui.tv(this, sub, 14, Theme.sub, false), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 6, 0, 20));
    }

    private void section(LinearLayout col, String title) {
        col.addView(Ui.tv(this, title, 17, Theme.text, true), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 22, 0, 12));
    }

    private void empty(LinearLayout col, String emoji, String title, String sub) {
        LinearLayout c = Ui.card(this, false);
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        c.setPadding(Ui.dp(this, 20), Ui.dp(this, 32), Ui.dp(this, 20), Ui.dp(this, 32));
        c.addView(Ui.tv(this, emoji, 46, Theme.text, false));
        TextView t = Ui.tv(this, title, 18, Theme.text, true);
        t.setGravity(Gravity.CENTER);
        c.addView(t, Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 14, 0, 0));
        TextView s = Ui.tv(this, sub, 14, Theme.sub, false);
        s.setGravity(Gravity.CENTER);
        c.addView(s, Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 6, 0, 0));
        col.addView(c, Ui.lp(Ui.MATCH, Ui.WRAP));
    }

    private LinearLayout.LayoutParams gap(int bottomDp) {
        return Ui.lp(this, Ui.MATCH, Ui.WRAP, 0, 0, 0, bottomDp);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private boolean notifDenied() {
        return Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
    }

    private void askNotif() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 21);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        render();
    }

    // ====================================================== الرئيسية
    private void home(LinearLayout col) {
        Calendar now = Calendar.getInstance();
        long nowMs = System.currentTimeMillis();
        int hr = now.get(Calendar.HOUR_OF_DAY);
        String greet = hr >= 5 && hr < 12 ? "صباح الخير" : hr >= 12 && hr < 17 ? "نهارك سعيد"
                : hr >= 17 && hr < 22 ? "مساء الخير" : "ليلة هادئة";
        String emoji = hr >= 5 && hr < 12 ? " ☀️" : hr >= 12 && hr < 17 ? " 🌤️" : hr >= 17 && hr < 22 ? " 🌙" : " ✨";
        String name = Store.name(this);

        // --- بطاقة الترحيب
        LinearLayout hero = Ui.vbox(this);
        hero.setBackground(Ui.grad(this, 0xFF6C5CE7, 0xFF4E3FD0, 28));
        hero.setElevation(Ui.dp(this, 6));
        hero.setPadding(Ui.dp(this, 22), Ui.dp(this, 22), Ui.dp(this, 22), Ui.dp(this, 22));
        hero.addView(Ui.tv(this, Fmt.dateLine(now), 13, 0xB3FFFFFF, false));
        hero.addView(Ui.tv(this, greet + (name.length() > 0 ? "، " + name : "") + emoji, 25, Color.WHITE, true),
                Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 8, 0, 18));

        Item best = null;
        long bestT = -1;
        for (Item it : Store.all(this)) {
            if (!it.enabled || it.type == Item.TASK) continue;
            long t = Scheduler.nextTrigger(it, nowMs);
            if (t > 0 && (bestT < 0 || t < bestT)) {
                bestT = t;
                best = it;
            }
        }
        LinearLayout nextBox = Ui.hbox(this);
        nextBox.setBackground(Ui.round(this, 0x26FFFFFF, 18));
        nextBox.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        LinearLayout nl = Ui.vbox(this);
        if (best != null) {
            long eventMs = bestT + best.lead * 60000L;
            nl.addView(Ui.tv(this, best.type == Item.WORK ? "موعد العمل القادم" : "المنبه القادم", 12, 0xB3FFFFFF, false));
            nl.addView(Ui.tv(this, Fmt.time(this, best.hour, best.minute), 30, Color.WHITE, true),
                    Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 4, 0, 0));
            String t = (best.title.length() > 0 ? best.title + " · " : "") + "بعد " + Fmt.countdown(eventMs - nowMs);
            nl.addView(Ui.tv(this, t, 13, 0xCCFFFFFF, false), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 4, 0, 0));
            nextBox.addView(nl, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
            nextBox.addView(Ui.tv(this, best.type == Item.WORK ? "💼" : "⏰", 34, Color.WHITE, false));
        } else {
            nl.addView(Ui.tv(this, "لا توجد منبهات مفعّلة", 16, Color.WHITE, true));
            nl.addView(Ui.tv(this, "اضغط هنا لإضافة منبه", 13, 0xCCFFFFFF, false), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 4, 0, 0));
            nextBox.addView(nl, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
            nextBox.addView(Ui.tv(this, "😴", 34, Color.WHITE, false));
            nextBox.setOnClickListener(v -> {
                tab = 1;
                render();
            });
        }
        hero.addView(nextBox, Ui.lp(Ui.MATCH, Ui.WRAP));
        col.addView(hero, gap(14));

        // --- تنبيه الإشعارات
        if (notifDenied()) {
            TextView w = Ui.tv(this, "🔔  فعّل إذن الإشعارات حتى تصلك التذكيرات — اضغط هنا", 14, 0xFF4A3200, true);
            w.setBackground(Ui.round(this, Theme.WARN, 16));
            w.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
            w.setOnClickListener(v -> askNotif());
            col.addView(w, gap(14));
        }

        // --- الإحصاءات
        List<Item> todayTasks = new ArrayList<>();
        for (Item t : Store.ofType(this, Item.TASK)) if (t.onDay(now.get(Calendar.DAY_OF_WEEK))) todayTasks.add(t);
        int done = 0;
        for (Item t : todayTasks) if (Store.doneToday(t)) done++;

        LinearLayout stats = Ui.hbox(this);
        LinearLayout c1 = Ui.card(this, true);
        float frac = todayTasks.isEmpty() ? 0 : done / (float) todayTasks.size();
        c1.addView(new ProgressRing(this, frac, done + "/" + todayTasks.size()), Ui.lp(Ui.dp(this, 78), Ui.dp(this, 78)));
        LinearLayout c1t = Ui.vbox(this);
        c1t.addView(Ui.tv(this, "مهام اليوم", 15, Theme.text, true));
        c1t.addView(Ui.tv(this, todayTasks.isEmpty() ? "لا مهام" : done == todayTasks.size() ? "أحسنت! 🎉" : "تابع الإنجاز", 12, Theme.sub, false),
                Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 4, 0, 0));
        LinearLayout.LayoutParams c1p = Ui.lp(Ui.WRAP, Ui.WRAP);
        c1p.setMarginStart(Ui.dp(this, 10));
        c1.addView(c1t, c1p);
        stats.addView(c1, new LinearLayout.LayoutParams(0, Ui.WRAP, 1.25f));

        LinearLayout c2 = Ui.card(this, false);
        c2.setGravity(Gravity.CENTER);
        c2.addView(Ui.tv(this, "🔥", 28, Theme.text, false));
        c2.addView(Ui.tv(this, String.valueOf(Store.streak(this)), 26, Theme.text, true), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 6, 0, 0));
        c2.addView(Ui.tv(this, "أيام متتالية", 12, Theme.sub, false), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 4, 0, 0));
        LinearLayout.LayoutParams c2p = new LinearLayout.LayoutParams(0, Ui.MATCH, 0.75f);
        c2p.setMarginStart(Ui.dp(this, 12));
        stats.addView(c2, c2p);
        col.addView(stats, Ui.lp(Ui.MATCH, Ui.WRAP));

        // --- مهام اليوم
        section(col, "مهام اليوم");
        if (todayTasks.isEmpty()) {
            empty(col, "📝", "لا توجد مهام لليوم", "أضف مهامك اليومية لتصلك تذكيراتها في وقتها");
        } else {
            for (Item t : todayTasks) col.addView(taskRow(t, false), gap(10));
        }

        // --- العمل اليوم
        List<Item> shifts = new ArrayList<>();
        for (Item w : Store.ofType(this, Item.WORK))
            if (w.enabled && w.onDay(now.get(Calendar.DAY_OF_WEEK))) shifts.add(w);
        if (!shifts.isEmpty()) {
            section(col, "العمل اليوم");
            for (Item w : shifts) col.addView(workCard(w, false), gap(10));
        }

        // --- اقتباس
        LinearLayout q = Ui.card(this, false);
        q.setBackground(Ui.round(this, Theme.card2, 22));
        q.addView(Ui.tv(this, "💡 فكرة اليوم", 13, Theme.PRIMARY, true));
        TextView qt = Ui.tv(this, QUOTES[now.get(Calendar.DAY_OF_YEAR) % QUOTES.length], 16, Theme.text, false);
        qt.setLineSpacing(0, 1.3f);
        q.addView(qt, Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 8, 0, 0));
        LinearLayout.LayoutParams qp = Ui.lp(this, Ui.MATCH, Ui.WRAP, 0, 12, 0, 0);
        col.addView(q, qp);
    }

    // ====================================================== المنبهات
    private void alarmsTab(LinearLayout col) {
        List<Item> l = Store.ofType(this, Item.ALARM);
        int on = 0;
        for (Item i : l) if (i.enabled) on++;
        pageTitle(col, "المنبهات", l.isEmpty() ? "استيقظ في الوقت المناسب" : on + " منبه مفعّل من أصل " + l.size());
        if (l.isEmpty()) {
            empty(col, "⏰", "لا توجد منبهات بعد", "اضغط على + لإضافة منبه الاستيقاظ الأول");
            return;
        }
        for (Item it : l) col.addView(alarmCard(it), gap(12));
    }

    private View alarmCard(final Item it) {
        LinearLayout c = Ui.card(this, true);
        final LinearLayout left = Ui.vbox(this);
        left.setAlpha(it.enabled ? 1f : 0.5f);
        left.addView(Ui.tv(this, Fmt.time(this, it.hour, it.minute), 32, Theme.text, true));
        left.addView(Ui.tv(this, it.title.length() > 0 ? it.title : "منبه", 15, Theme.text, false), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 6, 0, 0));
        left.addView(Ui.tv(this, Fmt.daysSummary(it.days), 13, Theme.sub, false), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 4, 0, 0));
        c.addView(left, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        c.addView(new ToggleView(this, it.enabled, on -> {
            it.enabled = on;
            Store.save(this, it);
            if (on) {
                long t = Scheduler.schedule(this, it);
                if (t > 0) toast("سيرن المنبه بعد " + Fmt.countdown(t - System.currentTimeMillis()));
            } else {
                Scheduler.cancel(this, it);
            }
            left.setAlpha(on ? 1f : 0.5f);
        }));
        c.setOnClickListener(v -> openEditor(Item.ALARM, it));
        return c;
    }

    // ====================================================== المهام
    private void tasksTab(LinearLayout col) {
        List<Item> l = Store.ofType(this, Item.TASK);
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int total = 0, done = 0;
        for (Item i : l) {
            if (i.onDay(dow)) {
                total++;
                if (Store.doneToday(i)) done++;
            }
        }
        pageTitle(col, "المهام", l.isEmpty() ? "نظّم يومك بمهام صغيرة" : "أنجزت " + done + " من " + total + " مهام اليوم");
        if (l.isEmpty()) {
            empty(col, "✅", "لا توجد مهام بعد", "اضغط على + لإضافة مهمة يومية مع تذكير");
            return;
        }
        for (Item it : l) col.addView(taskRow(it, true), gap(10));
    }

    private View taskRow(final Item it, boolean full) {
        final boolean todayTask = it.onDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        final boolean done = Store.doneToday(it);
        LinearLayout c = Ui.card(this, true);
        c.setAlpha(todayTask ? 1f : 0.55f);

        TextView chk = Ui.tv(this, done ? "✓" : "", 17, Color.WHITE, true);
        chk.setGravity(Gravity.CENTER);
        if (done) {
            chk.setBackground(Ui.circle(Theme.ACCENT));
        } else {
            GradientDrawable g = Ui.circle(Color.TRANSPARENT);
            g.setStroke(Ui.dp(this, 2), Ui.alpha(Theme.sub, 150));
            chk.setBackground(g);
        }
        chk.setOnClickListener(v -> {
            if (!todayTask) {
                toast("هذه المهمة ليست مجدولة لليوم");
                return;
            }
            Store.setDone(this, it, !done);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(Notifs.REMINDER_BASE + it.id);
            render();
        });
        c.addView(chk, Ui.lp(Ui.dp(this, 32), Ui.dp(this, 32)));

        LinearLayout mid = Ui.vbox(this);
        TextView title = Ui.tv(this, it.title, 16, done ? Theme.sub : Theme.text, true);
        if (done) title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mid.addView(title);
        String sub = "⏰ " + Fmt.time(this, it.hour, it.minute) + (full ? "  ·  " + Fmt.daysSummary(it.days) : "");
        mid.addView(Ui.tv(this, sub, 13, Theme.sub, false), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 5, 0, 0));
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, Ui.WRAP, 1f);
        mp.setMarginStart(Ui.dp(this, 14));
        mp.setMarginEnd(Ui.dp(this, 8));
        c.addView(mid, mp);

        if (full) {
            c.addView(new ToggleView(this, it.enabled, on -> {
                it.enabled = on;
                Store.save(this, it);
                if (on) Scheduler.schedule(this, it);
                else Scheduler.cancel(this, it);
            }));
        }
        c.setOnClickListener(v -> openEditor(Item.TASK, it));
        return c;
    }

    // ====================================================== العمل
    private void workTab(LinearLayout col) {
        List<Item> l = Store.ofType(this, Item.WORK);
        pageTitle(col, "مواعيد العمل", "نذكّرك قبل الدوام بالمدة التي تختارها");
        if (l.isEmpty()) {
            empty(col, "💼", "لا توجد مواعيد عمل", "اضغط على + لإضافة موعد دوامك وأيامه");
            return;
        }
        for (Item it : l) col.addView(workCard(it, true), gap(12));
    }

    private View workCard(final Item it, boolean withToggle) {
        LinearLayout c = Ui.card(this, true);
        final LinearLayout left = Ui.vbox(this);
        left.setAlpha(it.enabled ? 1f : 0.5f);
        left.addView(Ui.tv(this, "💼  " + (it.title.length() > 0 ? it.title : "الدوام"), 17, Theme.text, true));
        left.addView(Ui.tv(this, "يبدأ " + Fmt.time(this, it.hour, it.minute), 26, Theme.PRIMARY, true), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 8, 0, 0));
        left.addView(Ui.tv(this, Fmt.daysSummary(it.days) + "  ·  تذكير " + Fmt.lead(it.lead), 13, Theme.sub, false),
                Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 6, 0, 0));
        c.addView(left, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        if (withToggle) {
            c.addView(new ToggleView(this, it.enabled, on -> {
                it.enabled = on;
                Store.save(this, it);
                if (on) Scheduler.schedule(this, it);
                else Scheduler.cancel(this, it);
                left.setAlpha(on ? 1f : 0.5f);
            }));
        }
        c.setOnClickListener(v -> openEditor(Item.WORK, it));
        return c;
    }

    // ====================================================== الإعدادات
    private LinearLayout group() {
        LinearLayout g = Ui.card(this, false);
        g.setPadding(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
        return g;
    }

    private void addRow(LinearLayout g, String icon, String title, String sub, View control, View.OnClickListener click, boolean divider) {
        LinearLayout r = Ui.hbox(this);
        r.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
        if (click != null) {
            r.setBackground(Ui.ripple(new ColorDrawable(Color.TRANSPARENT), Ui.alpha(Theme.PRIMARY, 40)));
            r.setOnClickListener(click);
        }
        TextView ic = Ui.tv(this, icon, 20, Theme.text, false);
        ic.setGravity(Gravity.CENTER);
        ic.setBackground(Ui.round(this, Theme.card2, 12));
        r.addView(ic, Ui.lp(Ui.dp(this, 40), Ui.dp(this, 40)));
        LinearLayout t = Ui.vbox(this);
        t.addView(Ui.tv(this, title, 15, Theme.text, true));
        if (sub != null) t.addView(Ui.tv(this, sub, 12.5f, Theme.sub, false), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 4, 0, 0));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, Ui.WRAP, 1f);
        tp.setMarginStart(Ui.dp(this, 12));
        tp.setMarginEnd(Ui.dp(this, 8));
        r.addView(t, tp);
        if (control != null) r.addView(control);
        g.addView(r, Ui.lp(Ui.MATCH, Ui.WRAP));
        if (divider) {
            View d = new View(this);
            d.setBackgroundColor(Theme.line);
            g.addView(d, Ui.lp(this, Ui.MATCH, 1, 14, 0, 14, 0));
        }
    }

    private String ringtoneName() {
        String s = Store.ringtone(this);
        if (s.length() == 0) return "الافتراضية";
        try {
            Ringtone r = RingtoneManager.getRingtone(this, Uri.parse(s));
            if (r != null) return r.getTitle(this);
        } catch (Exception ignored) {
        }
        return "مخصّصة";
    }

    private void settingsTab(LinearLayout col) {
        pageTitle(col, "الإعدادات", "خصّص ملفي على ذوقك");

        LinearLayout g1 = group();
        String name = Store.name(this);
        addRow(g1, "👤", "الاسم", name.length() > 0 ? name : "غير محدد", null, v -> nameDialog(), true);
        addRow(g1, "🌙", "الوضع الداكن", "مريح للعين ليلاً",
                new ToggleView(this, Theme.dark, on -> {
                    Store.setDark(this, on);
                    buildRoot();
                }), null, false);
        col.addView(g1, gap(8));

        section(col, "الصوت والتنبيه");
        LinearLayout g2 = group();
        addRow(g2, "🎵", "نغمة المنبه", ringtoneName(), null, v -> {
            Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            String cur = Store.ringtone(this);
            if (cur.length() > 0) i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(cur));
            startActivityForResult(i, 7);
        }, true);

        // مدة الغفوة
        LinearLayout sn = Ui.vbox(this);
        sn.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
        sn.addView(Ui.tv(this, "💤  مدة الغفوة", 15, Theme.text, true));
        LinearLayout chips = Ui.hbox(this);
        final int[] opts = {5, 10, 15, 20};
        for (final int m : opts) {
            TextView ch = chip(m + " د", Store.snooze(this) == m);
            ch.setOnClickListener(v -> {
                Store.setSnooze(this, m);
                render();
            });
            chips.addView(ch, Ui.lp(this, 0, Ui.dp(this, 42), 0, 0, 8, 0));
            ((LinearLayout.LayoutParams) ch.getLayoutParams()).weight = 1f;
        }
        sn.addView(chips, Ui.lp(this, Ui.MATCH, Ui.WRAP, 0, 12, 0, 0));
        g2.addView(sn);
        View d = new View(this);
        d.setBackgroundColor(Theme.line);
        g2.addView(d, Ui.lp(this, Ui.MATCH, 1, 14, 0, 14, 0));

        addRow(g2, "📳", "الاهتزاز", "مع رنين المنبه",
                new ToggleView(this, Store.vibrate(this), on -> Store.setVibrate(this, on)), null, true);
        addRow(g2, "📈", "رفع الصوت تدريجياً", "يبدأ هادئاً ثم يعلو",
                new ToggleView(this, Store.gradual(this), on -> Store.setGradual(this, on)), null, true);
        addRow(g2, "🧪", "تجربة المنبه", "اسمع كيف سيبدو", null, v -> {
            Intent s = new Intent(this, AlarmService.class);
            s.putExtra("id", -1);
            s.putExtra("title", "هذا اختبار للمنبه ✨");
            startForegroundService(s);
        }, false);
        col.addView(g2, gap(8));

        section(col, "النظام");
        LinearLayout g3 = group();
        addRow(g3, "🔔", "إشعارات التذكير", notifDenied() ? "غير مفعّلة — اضغط للتفعيل" : "مفعّلة ✓", null,
                notifDenied() ? v -> askNotif() : null, true);
        if (Build.VERSION.SDK_INT >= 34) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (!nm.canUseFullScreenIntent()) {
                addRow(g3, "📱", "الظهور فوق شاشة القفل", "غير مسموح — اضغط للسماح", null, v -> {
                    try {
                        Intent i = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                                Uri.parse("package:" + getPackageName()));
                        startActivity(i);
                    } catch (Exception e) {
                        toast("افتح إعدادات التطبيق وفعّل الظهور فوق شاشة القفل");
                    }
                }, true);
            }
        }
        addRow(g3, "🎓", "إعادة عرض التعريف", "شاهد مقدمة التطبيق مرة أخرى", null, v -> {
            Intent i = new Intent(this, OnboardingActivity.class);
            i.putExtra("replay", true);
            startActivity(i);
        }, false);
        col.addView(g3, gap(8));

        // عن التطبيق
        LinearLayout about = Ui.card(this, true);
        about.addView(new LogoView(this, true), Ui.lp(Ui.dp(this, 56), Ui.dp(this, 56)));
        LinearLayout at = Ui.vbox(this);
        at.addView(Ui.tv(this, "ملفي", 18, Theme.text, true));
        at.addView(Ui.tv(this, "الإصدار 1.0 · منبه ومهام ومواعيد عمل", 12.5f, Theme.sub, false), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 4, 0, 0));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(0, Ui.WRAP, 1f);
        ap.setMarginStart(Ui.dp(this, 14));
        about.addView(at, ap);
        col.addView(about, Ui.lp(this, Ui.MATCH, Ui.WRAP, 0, 14, 0, 0));
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 7 && res == RESULT_OK && data != null) {
            Uri u = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Store.setRingtone(this, u == null ? "" : u.toString());
            render();
        }
    }

    private void nameDialog() {
        final EditText et = new EditText(this);
        et.setText(Store.name(this));
        et.setSingleLine(true);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        FrameLayout box = new FrameLayout(this);
        box.setPadding(Ui.dp(this, 22), Ui.dp(this, 8), Ui.dp(this, 22), 0);
        box.addView(et);
        new AlertDialog.Builder(this, Theme.dark ? R.style.PickerDark : R.style.PickerLight)
                .setTitle("اسمك")
                .setView(box)
                .setPositiveButton("حفظ", (dlg, w) -> {
                    Store.setName(this, et.getText().toString().trim());
                    render();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    // ====================================================== نافذة الإضافة / التعديل
    private TextView chip(String label, boolean selected) {
        TextView t = Ui.tv(this, label, 14, selected ? Color.WHITE : Theme.sub, true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(Ui.round(this, selected ? Theme.PRIMARY : Theme.card2, 14));
        t.setClickable(true);
        return t;
    }

    private void styleChip(TextView t, boolean selected) {
        t.setTextColor(selected ? Color.WHITE : Theme.sub);
        t.setBackground(Ui.round(this, selected ? Theme.PRIMARY : Theme.card2, 14));
    }

    private Item newItem(int type) {
        Item it = new Item();
        it.type = type;
        it.hour = type == Item.ALARM ? 7 : type == Item.TASK ? 9 : 8;
        it.minute = 0;
        it.days = 127;
        it.lead = type == Item.WORK ? 15 : 0;
        it.enabled = true;
        return it;
    }

    private void openEditor(final int type, final Item existing) {
        final Item it = existing != null ? existing.copy() : newItem(type);
        final Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = Ui.vbox(this);
        sheet.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        float r = Ui.dp(this, 28);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Theme.card);
        bg.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        sheet.setBackground(bg);
        sheet.setPadding(Ui.dp(this, 22), Ui.dp(this, 12), Ui.dp(this, 22), Ui.dp(this, 22));

        View handle = new View(this);
        handle.setBackground(Ui.round(this, Theme.line, 3));
        LinearLayout.LayoutParams hp = Ui.lp(this, 40, 5, 0, 0, 0, 14);
        hp.width = Ui.dp(this, 40);
        hp.height = Ui.dp(this, 5);
        hp.gravity = Gravity.CENTER_HORIZONTAL;
        sheet.addView(handle, hp);

        String typeName = type == Item.ALARM ? "منبه" : type == Item.TASK ? "مهمة" : "موعد عمل";
        sheet.addView(Ui.tv(this, (existing != null ? "تعديل " : "إضافة ") + typeName, 22, Theme.text, true));

        final EditText titleEt = new EditText(this);
        titleEt.setHint(type == Item.ALARM ? "اسم المنبه (مثال: الاستيقاظ)"
                : type == Item.TASK ? "ما هي المهمة؟" : "اسم الدوام (مثال: الشركة)");
        titleEt.setHintTextColor(Theme.sub);
        titleEt.setTextColor(Theme.text);
        titleEt.setTextSize(16);
        titleEt.setSingleLine(true);
        titleEt.setText(it.title);
        titleEt.setBackground(Ui.round(this, Theme.card2, 16));
        titleEt.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        sheet.addView(titleEt, Ui.lp(this, Ui.MATCH, Ui.WRAP, 0, 16, 0, 0));

        // الوقت
        final int[] hm = {it.hour, it.minute};
        final TextView timeTv = Ui.tv(this, Fmt.time(this, hm[0], hm[1]), 44, Theme.PRIMARY, true);
        timeTv.setGravity(Gravity.CENTER);
        timeTv.setBackground(Ui.ripple(Ui.round(this, Theme.card2, 20), Ui.alpha(Theme.PRIMARY, 50)));
        timeTv.setPadding(0, Ui.dp(this, 16), 0, Ui.dp(this, 16));
        timeTv.setClickable(true);
        timeTv.setOnClickListener(v -> new TimePickerDialog(this,
                Theme.dark ? R.style.PickerDark : R.style.PickerLight,
                (view, h, m) -> {
                    hm[0] = h;
                    hm[1] = m;
                    timeTv.setText(Fmt.time(this, h, m));
                }, hm[0], hm[1], DateFormat.is24HourFormat(this)).show());
        sheet.addView(timeTv, Ui.lp(this, Ui.MATCH, Ui.WRAP, 0, 14, 0, 0));
        sheet.addView(Ui.tv(this, type == Item.WORK ? "وقت بدء الدوام — اضغط للتغيير" : "اضغط على الوقت لتغييره", 12, Theme.sub, false),
                Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 8, 0, 0));

        // الأيام
        sheet.addView(Ui.tv(this, "التكرار", 15, Theme.text, true), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 18, 0, 10));
        final int[] days = {it.days};
        final TextView sum = Ui.tv(this, "", 12.5f, Theme.sub, false);
        final Runnable updSum = () -> sum.setText(days[0] == 0 && type != Item.ALARM
                ? "اختر يوماً واحداً على الأقل" : Fmt.daysSummary(days[0]));
        LinearLayout row = Ui.hbox(this);
        for (final int idx : Fmt.WEEK_ORDER) {
            final TextView ch = chip(Fmt.DAYS_SHORT[idx], (days[0] & (1 << idx)) != 0);
            ch.setOnClickListener(v -> {
                days[0] ^= (1 << idx);
                styleChip(ch, (days[0] & (1 << idx)) != 0);
                updSum.run();
            });
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, Ui.dp(this, 42), 1f);
            cp.setMargins(Ui.dp(this, 3), 0, Ui.dp(this, 3), 0);
            row.addView(ch, cp);
        }
        sheet.addView(row, Ui.lp(Ui.MATCH, Ui.WRAP));
        updSum.run();
        sheet.addView(sum, Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 10, 0, 0));

        // التذكير قبل الموعد (للعمل)
        final int[] lead = {it.lead};
        if (type == Item.WORK) {
            sheet.addView(Ui.tv(this, "التذكير قبل الموعد", 15, Theme.text, true), Ui.lp(this, Ui.WRAP, Ui.WRAP, 0, 18, 0, 10));
            final int[] leads = {0, 5, 10, 15, 30, 60};
            final TextView[] lc = new TextView[leads.length];
            LinearLayout lr = Ui.hbox(this);
            for (int k = 0; k < leads.length; k++) {
                final int kk = k;
                lc[k] = chip(leads[k] == 0 ? "الآن" : leads[k] + "د", leads[k] == lead[0]);
                lc[k].setOnClickListener(v -> {
                    lead[0] = leads[kk];
                    for (int j = 0; j < lc.length; j++) styleChip(lc[j], leads[j] == lead[0]);
                });
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, Ui.dp(this, 42), 1f);
                cp.setMargins(Ui.dp(this, 3), 0, Ui.dp(this, 3), 0);
                lr.addView(lc[k], cp);
            }
            sheet.addView(lr, Ui.lp(Ui.MATCH, Ui.WRAP));
        }

        // الأزرار
        TextView save = Ui.button(this, "حفظ");
        save.setOnClickListener(v -> {
            String ttl = titleEt.getText().toString().trim();
            if (ttl.length() == 0) ttl = type == Item.ALARM ? "منبه" : type == Item.TASK ? "مهمة" : "الدوام";
            it.title = ttl;
            it.hour = hm[0];
            it.minute = hm[1];
            it.days = days[0];
            if (type != Item.ALARM && it.days == 0) it.days = 127;
            it.lead = lead[0];
            it.enabled = true;
            Store.save(this, it);
            long t = Scheduler.schedule(this, it);
            if (type == Item.ALARM && t > 0) toast("سيرن المنبه بعد " + Fmt.countdown(t - System.currentTimeMillis()));
            else toast("تم الحفظ ✓");
            d.dismiss();
            render();
        });
        sheet.addView(save, Ui.lp(this, Ui.MATCH, Ui.WRAP, 0, 24, 0, 0));

        LinearLayout bottom = Ui.hbox(this);
        bottom.setGravity(Gravity.CENTER);
        TextView cancel = Ui.tv(this, "إلغاء", 15, Theme.sub, true);
        cancel.setPadding(Ui.dp(this, 20), Ui.dp(this, 14), Ui.dp(this, 20), Ui.dp(this, 6));
        cancel.setOnClickListener(v -> d.dismiss());
        bottom.addView(cancel);
        if (existing != null) {
            TextView del = Ui.tv(this, "حذف", 15, Theme.DANGER, true);
            del.setPadding(Ui.dp(this, 20), Ui.dp(this, 14), Ui.dp(this, 20), Ui.dp(this, 6));
            del.setOnClickListener(v -> {
                Scheduler.cancel(this, existing);
                Store.delete(this, existing.id);
                toast("تم الحذف");
                d.dismiss();
                render();
            });
            bottom.addView(del);
        }
        sheet.addView(bottom, Ui.lp(Ui.MATCH, Ui.WRAP));

        ScrollView sc = new ScrollView(this);
        sc.setVerticalScrollBarEnabled(false);
        sc.addView(sheet);
        d.setContentView(sc);
        Window w = d.getWindow();
        w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        w.setLayout(Ui.MATCH, Ui.WRAP);
        w.setGravity(Gravity.BOTTOM);
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        WindowManager.LayoutParams a = w.getAttributes();
        a.windowAnimations = android.R.style.Animation_InputMethod;
        w.setAttributes(a);
        d.show();
    }
}