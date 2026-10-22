package com.malafi.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/** جدولة المنبهات والتذكيرات عبر AlarmManager.setAlarmClock (دقيق ولا يحتاج إذناً خاصاً) */
public class Scheduler {
    static final int SNOOZE_BASE = 1000000;

    /** وقت التشغيل القادم (بالميلي ثانية) بعد now، أو -1 */
    public static long nextTrigger(Item it, long now) {
        for (int off = 0; off <= 8; off++) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(now);
            c.add(Calendar.DAY_OF_YEAR, off);
            c.set(Calendar.HOUR_OF_DAY, it.hour);
            c.set(Calendar.MINUTE, it.minute);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            if (!it.onDay(c.get(Calendar.DAY_OF_WEEK))) continue;
            long t = c.getTimeInMillis() - it.lead * 60000L;
            if (t > now) return t;
        }
        return -1;
    }

    private static PendingIntent firePi(Context c, int requestCode, int id, boolean snooze) {
        Intent i = new Intent(c, AlarmReceiver.class);
        i.setAction("com.malafi.app.FIRE");
        i.putExtra("id", id);
        i.putExtra("snooze", snooze);
        return PendingIntent.getBroadcast(c, requestCode, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static PendingIntent showPi(Context c) {
        return PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void cancel(Context c, Item it) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(firePi(c, it.id, it.id, false));
    }

    /** يعيد جدولة العنصر. يرجع وقت التشغيل القادم أو -1 */
    public static long schedule(Context c, Item it) {
        cancel(c, it);
        if (!it.enabled) return -1;
        long t = nextTrigger(it, System.currentTimeMillis());
        if (t < 0) return -1;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.setAlarmClock(new AlarmManager.AlarmClockInfo(t, showPi(c)), firePi(c, it.id, it.id, false));
        return t;
    }

    public static void snooze(Context c, int id, int minutes) {
        if (id <= 0) return;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        long t = System.currentTimeMillis() + minutes * 60000L;
        am.setAlarmClock(new AlarmManager.AlarmClockInfo(t, showPi(c)),
                firePi(c, SNOOZE_BASE + id, id, true));
    }

    public static void rescheduleAll(Context c) {
        for (Item it : Store.all(c)) schedule(c, it);
    }
}
