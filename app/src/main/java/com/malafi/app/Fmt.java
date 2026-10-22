package com.malafi.app;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

/** تنسيقات عربية للوقت والتاريخ */
public class Fmt {
    public static final String[] DAYS = {"الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"};
    public static final String[] DAYS_SHORT = {"ح", "ن", "ث", "ر", "خ", "ج", "س"};
    /** ترتيب الأسبوع العربي: السبت أولاً (فهارس البت) */
    public static final int[] WEEK_ORDER = {6, 0, 1, 2, 3, 4, 5};
    public static final String[] MONTHS = {"يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"};

    public static String time(Context c, int h, int m) {
        if (DateFormat.is24HourFormat(c)) {
            return String.format(Locale.US, "%02d:%02d", h, m);
        }
        int h12 = h % 12 == 0 ? 12 : h % 12;
        return String.format(Locale.US, "%d:%02d %s", h12, m, h < 12 ? "ص" : "م");
    }

    public static String daysSummary(int days) {
        if (days == 0) return "مرة واحدة";
        if (days == 127) return "كل يوم";
        if (days == 31) return "الأحد – الخميس";
        if (days == 62) return "الاثنين – الجمعة";
        StringBuilder sb = new StringBuilder();
        for (int idx : WEEK_ORDER) {
            if ((days & (1 << idx)) != 0) {
                if (sb.length() > 0) sb.append("، ");
                sb.append(DAYS[idx]);
            }
        }
        return sb.toString();
    }

    public static String dateLine(Calendar c) {
        return DAYS[c.get(Calendar.DAY_OF_WEEK) - 1] + "، " + c.get(Calendar.DAY_OF_MONTH) + " "
                + MONTHS[c.get(Calendar.MONTH)];
    }

    public static String countdown(long ms) {
        long mins = ms / 60000;
        if (mins < 1) return "أقل من دقيقة";
        long d = mins / 1440, h = (mins % 1440) / 60, m = mins % 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append(d == 1 ? " يوم" : " أيام");
        if (h > 0) {
            if (sb.length() > 0) sb.append(" و");
            sb.append(h).append(" ساعة");
        }
        if (m > 0 && d == 0) {
            if (sb.length() > 0) sb.append(" و");
            sb.append(m).append(" دقيقة");
        }
        return sb.toString();
    }

    public static String lead(int m) {
        if (m == 0) return "في الموعد تماماً";
        if (m == 60) return "قبل ساعة";
        return "قبل " + m + " دقيقة";
    }
}
