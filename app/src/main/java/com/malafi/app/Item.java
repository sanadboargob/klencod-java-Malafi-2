package com.malafi.app;

import org.json.JSONObject;

/** عنصر واحد: منبه أو مهمة أو موعد عمل */
public class Item {
    public static final int ALARM = 0, TASK = 1, WORK = 2;

    public int id, type, hour, minute;
    /** أيام التكرار: البت (dayOfWeek-1) — الأحد=البت0 ... السبت=البت6. 0 = مرة واحدة */
    public int days = 127;
    /** للعمل: التذكير قبل الموعد بعدد دقائق */
    public int lead = 0;
    public String title = "";
    public String lastDone = "";
    public boolean enabled = true;

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("type", type);
            o.put("hour", hour);
            o.put("minute", minute);
            o.put("days", days);
            o.put("lead", lead);
            o.put("title", title);
            o.put("lastDone", lastDone);
            o.put("enabled", enabled);
        } catch (Exception ignored) {
        }
        return o;
    }

    public static Item from(JSONObject o) {
        Item it = new Item();
        it.id = o.optInt("id");
        it.type = o.optInt("type");
        it.hour = o.optInt("hour");
        it.minute = o.optInt("minute");
        it.days = o.optInt("days", 127);
        it.lead = o.optInt("lead");
        it.title = o.optString("title", "");
        it.lastDone = o.optString("lastDone", "");
        it.enabled = o.optBoolean("enabled", true);
        return it;
    }

    public Item copy() {
        return from(toJson());
    }

    public boolean onDay(int calendarDow) {
        return days == 0 || (days & (1 << (calendarDow - 1))) != 0;
    }

    public int minutesOfDay() {
        return hour * 60 + minute;
    }
}
