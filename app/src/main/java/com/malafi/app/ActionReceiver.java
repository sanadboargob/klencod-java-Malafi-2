package com.malafi.app;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** أزرار الإشعارات: تم / غفوة / إيقاف */
public class ActionReceiver extends BroadcastReceiver {
    public static final String DONE = "com.malafi.app.DONE";
    public static final String SNOOZE = "com.malafi.app.SNOOZE";
    public static final String STOP = "com.malafi.app.STOP";

    @Override
    public void onReceive(Context c, Intent i) {
        int id = i.getIntExtra("id", 0);
        String a = i.getAction();
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (DONE.equals(a)) {
            Item it = Store.get(c, id);
            if (it != null) Store.setDone(c, it, true);
            nm.cancel(Notifs.REMINDER_BASE + id);
        } else if (SNOOZE.equals(a)) {
            Scheduler.snooze(c, id, Store.snooze(c));
            nm.cancel(Notifs.REMINDER_BASE + id);
            AlarmService.stop(c);
        } else if (STOP.equals(a)) {
            nm.cancel(Notifs.REMINDER_BASE + id);
            AlarmService.stop(c);
        }
    }
}
