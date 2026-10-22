package com.malafi.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** يُستدعى عند حلول موعد منبه أو مهمة أو دوام */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        int id = i.getIntExtra("id", 0);
        boolean snooze = i.getBooleanExtra("snooze", false);
        Item it = Store.get(c, id);
        if (it == null) return;

        if (!snooze) {
            if (it.type == Item.ALARM && it.days == 0) {
                it.enabled = false; // منبه لمرة واحدة
                Store.save(c, it);
            } else {
                Scheduler.schedule(c, it); // جدولة الموعد التالي
            }
        }

        if (it.type == Item.ALARM) {
            Intent s = new Intent(c, AlarmService.class);
            s.putExtra("id", id);
            c.startForegroundService(s);
        } else {
            if (it.type == Item.TASK && Store.doneToday(it)) return;
            Notifs.postReminder(c, it);
        }
    }
}
