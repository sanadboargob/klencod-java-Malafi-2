package com.malafi.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** تخزين البيانات والإعدادات محلياً (SharedPreferences) */
public class Store {
    static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences("malafi", Context.MODE_PRIVATE);
    }

    // ---------- العناصر ----------
    public static synchronized List<Item> all(Context c) {
        List<Item> l = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(sp(c).getString("items", "[]"));
            for (int i = 0; i < a.length(); i++) l.add(Item.from(a.getJSONObject(i)));
        } catch (Exception ignored) {
        }
        return l;
    }

    public static List<Item> ofType(Context c, int type) {
        List<Item> l = new ArrayList<>();
        for (Item i : all(c)) if (i.type == type) l.add(i);
        Collections.sort(l, new Comparator<Item>() {
            @Override
            public int compare(Item a, Item b) {
                return a.minutesOfDay() - b.minutesOfDay();
            }
        });
        return l;
    }

    public static Item get(Context c, int id) {
        for (Item i : all(c)) if (i.id == id) return i;
        return null;
    }

    private static void write(Context c, List<Item> l) {
        JSONArray a = new JSONArray();
        for (Item i : l) a.put(i.toJson());
        sp(c).edit().putString("items", a.toString()).apply();
    }

    public static synchronized void save(Context c, Item it) {
        List<Item> l = all(c);
        if (it.id == 0) {
            it.id = sp(c).getInt("nextId", 1);
            sp(c).edit().putInt("nextId", it.id + 1).apply();
            l.add(it);
        } else {
            boolean found = false;
            for (int i = 0; i < l.size(); i++) {
                if (l.get(i).id == it.id) {
                    l.set(i, it);
                    found = true;
                    break;
                }
            }
            if (!found) l.add(it);
        }
        write(c, l);
    }

    public static synchronized void delete(Context c, int id) {
        List<Item> l = all(c);
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).id == id) {
                l.remove(i);
                break;
            }
        }
        write(c, l);
        refreshActive(c);
    }

    // ---------- المهام والتقدم ----------
    public static String dateKey(Calendar cal) {
        return String.format(Locale.US, "%04d-%02d-%02d", cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    public static String today() {
        return dateKey(Calendar.getInstance());
    }

    public static boolean doneToday(Item it) {
        return today().equals(it.lastDone);
    }

    public static void setDone(Context c, Item it, boolean done) {
        it.lastDone = done ? today() : "";
        save(c, it);
        refreshActive(c);
    }

    static void refreshActive(Context c) {
        boolean any = false;
        for (Item i : all(c)) if (i.type == Item.TASK && doneToday(i)) any = true;
        Set<String> s = new HashSet<>(sp(c).getStringSet("active", new HashSet<String>()));
        if (any) s.add(today());
        else s.remove(today());
        sp(c).edit().putStringSet("active", s).apply();
    }

    /** عدد الأيام المتتالية التي أنجزت فيها مهمة واحدة على الأقل */
    public static int streak(Context c) {
        Set<String> s = sp(c).getStringSet("active", new HashSet<String>());
        Calendar cal = Calendar.getInstance();
        if (!s.contains(dateKey(cal))) cal.add(Calendar.DAY_OF_YEAR, -1);
        int n = 0;
        while (s.contains(dateKey(cal))) {
            n++;
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        return n;
    }

    // ---------- الإعدادات ----------
    public static boolean onboarded(Context c) {
        return sp(c).getBoolean("onboarded", false);
    }

    public static void setOnboarded(Context c, boolean v) {
        sp(c).edit().putBoolean("onboarded", v).apply();
    }

    public static String name(Context c) {
        return sp(c).getString("name", "");
    }

    public static void setName(Context c, String n) {
        sp(c).edit().putString("name", n).apply();
    }

    public static boolean dark(Context c) {
        SharedPreferences p = sp(c);
        if (p.contains("dark")) return p.getBoolean("dark", false);
        int ui = c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return ui == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void setDark(Context c, boolean v) {
        sp(c).edit().putBoolean("dark", v).apply();
    }

    public static int snooze(Context c) {
        return sp(c).getInt("snooze", 10);
    }

    public static void setSnooze(Context c, int m) {
        sp(c).edit().putInt("snooze", m).apply();
    }

    public static boolean vibrate(Context c) {
        return sp(c).getBoolean("vibrate", true);
    }

    public static void setVibrate(Context c, boolean v) {
        sp(c).edit().putBoolean("vibrate", v).apply();
    }

    public static boolean gradual(Context c) {
        return sp(c).getBoolean("gradual", true);
    }

    public static void setGradual(Context c, boolean v) {
        sp(c).edit().putBoolean("gradual", v).apply();
    }

    /** نص Uri النغمة، أو فارغ للنغمة الافتراضية */
    public static String ringtone(Context c) {
        return sp(c).getString("ringtone", "");
    }

    public static void setRingtone(Context c, String uri) {
        sp(c).edit().putString("ringtone", uri == null ? "" : uri).apply();
    }
}
