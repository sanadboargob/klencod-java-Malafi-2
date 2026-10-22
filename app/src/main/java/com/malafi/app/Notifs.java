package com.malafi.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;

public class Notifs {
    public static final String CH_ALARM = "malafi_alarm";
    public static final String CH_REMIND = "malafi_remind";
    public static final int REMINDER_BASE = 2000;

    public static void channels(Context c) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel alarm = new NotificationChannel(CH_ALARM, "المنبهات", NotificationManager.IMPORTANCE_HIGH);
        alarm.setDescription("إشعار المنبه أثناء الرنين");
        alarm.setSound(null, null);
        alarm.enableVibration(false);
        alarm.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(alarm);

        NotificationChannel rem = new NotificationChannel(CH_REMIND, "التذكيرات", NotificationManager.IMPORTANCE_HIGH);
        rem.setDescription("تذكير المهام ومواعيد العمل");
        rem.enableVibration(true);
        rem.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
        nm.createNotificationChannel(rem);
    }

    static PendingIntent action(Context c, String action, int id) {
        Intent i = new Intent(c, ActionReceiver.class);
        i.setAction(action);
        i.putExtra("id", id);
        int code = id * 10 + (action.equals(ActionReceiver.DONE) ? 1 : action.equals(ActionReceiver.SNOOZE) ? 2 : 3);
        return PendingIntent.getBroadcast(c, code, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static Notification.Action act(Context c, String label, PendingIntent pi) {
        return new Notification.Action.Builder(
                Icon.createWithResource(c, R.drawable.ic_stat_alarm), label, pi).build();
    }

    public static void postReminder(Context c, Item it) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        boolean work = it.type == Item.WORK;
        String title = work ? "💼 موعد العمل" : "✅ تذكير بمهمة";
        String text = it.title;
        if (work) {
            text = it.title + " — يبدأ " + Fmt.time(c, it.hour, it.minute)
                    + (it.lead > 0 ? " (بعد " + it.lead + " دقيقة)" : " الآن");
        }
        PendingIntent open = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = new Notification.Builder(c, CH_REMIND)
                .setSmallIcon(R.drawable.ic_stat_alarm)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setContentIntent(open)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setColor(0xFF6C5CE7)
                .setAutoCancel(true);
        if (!work) b.addAction(act(c, "تم ✓", action(c, ActionReceiver.DONE, it.id)));
        b.addAction(act(c, "لاحقاً", action(c, ActionReceiver.SNOOZE, it.id)));
        nm.notify(REMINDER_BASE + it.id, b.build());
    }

    public static Notification alarm(Context c, int id, String title) {
        PendingIntent full = PendingIntent.getActivity(c, 7, alarmIntent(c, id, title),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(c, CH_ALARM)
                .setSmallIcon(R.drawable.ic_stat_alarm)
                .setContentTitle("⏰ المنبه يرن")
                .setContentText(title)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setColor(0xFF6C5CE7)
                .setContentIntent(full)
                .setFullScreenIntent(full, true)
                .addAction(act(c, "غفوة", action(c, ActionReceiver.SNOOZE, id)))
                .addAction(act(c, "إيقاف", action(c, ActionReceiver.STOP, id)))
                .build();
    }

    public static Intent alarmIntent(Context c, int id, String title) {
        Intent i = new Intent(c, AlarmActivity.class);
        i.putExtra("id", id);
        i.putExtra("title", title);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }
}
