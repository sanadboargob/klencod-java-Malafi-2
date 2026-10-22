package com.malafi.app;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;

/** خدمة تشغيل صوت المنبه والاهتزاز */
public class AlarmService extends Service {
    static final int NOTIF_ID = 1001;
    private MediaPlayer mp;
    private Vibrator vib;
    private PowerManager.WakeLock wl;
    private final Handler h = new Handler(Looper.getMainLooper());
    private float vol = 1f;

    private final Runnable ramp = new Runnable() {
        @Override
        public void run() {
            if (mp == null) return;
            vol = Math.min(1f, vol + 0.06f);
            try {
                mp.setVolume(vol, vol);
            } catch (Exception ignored) {
            }
            if (vol < 1f) h.postDelayed(this, 1000);
        }
    };
    private final Runnable autoStop = new Runnable() {
        @Override
        public void run() {
            stopSelf();
        }
    };

    public static void stop(Context c) {
        c.stopService(new Intent(c, AlarmService.class));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        int id = i != null ? i.getIntExtra("id", 0) : 0;
        String title = i != null ? i.getStringExtra("title") : null;
        if (title == null) {
            Item it = Store.get(this, id);
            title = it != null && it.title.length() > 0 ? it.title : "المنبه";
        }
        Notification n = Notifs.alarm(this, id, title);
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIF_ID, n);
        }
        release();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "malafi:alarm");
        wl.acquire(4 * 60 * 1000L);
        startSound();
        startVibration();
        h.removeCallbacks(autoStop);
        h.postDelayed(autoStop, 3 * 60 * 1000L); // يتوقف تلقائياً بعد 3 دقائق
        try {
            startActivity(Notifs.alarmIntent(this, id, title));
        } catch (Exception ignored) {
            // إن منع النظام فتح الشاشة، يظهر الإشعار بملء الشاشة
        }
        return START_NOT_STICKY;
    }

    private void startSound() {
        Uri uri = null;
        String saved = Store.ringtone(this);
        if (saved.length() > 0) uri = Uri.parse(saved);
        if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        if (!play(uri)) {
            play(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        }
    }

    private boolean play(Uri uri) {
        try {
            if (mp != null) {
                mp.release();
                mp = null;
            }
            mp = new MediaPlayer();
            mp.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
            mp.setDataSource(this, uri);
            mp.setLooping(true);
            mp.prepare();
            vol = Store.gradual(this) ? 0.1f : 1f;
            mp.setVolume(vol, vol);
            mp.start();
            h.removeCallbacks(ramp);
            if (vol < 1f) h.postDelayed(ramp, 1000);
            return true;
        } catch (Exception e) {
            if (mp != null) {
                mp.release();
                mp = null;
            }
            return false;
        }
    }

    private void startVibration() {
        if (!Store.vibrate(this)) return;
        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vib != null) {
            vib.vibrate(VibrationEffect.createWaveform(new long[]{0, 700, 500, 700, 900}, 0));
        }
    }

    private void release() {
        h.removeCallbacks(ramp);
        if (mp != null) {
            try {
                mp.stop();
            } catch (Exception ignored) {
            }
            mp.release();
            mp = null;
        }
        if (vib != null) vib.cancel();
        if (wl != null && wl.isHeld()) wl.release();
    }

    @Override
    public void onDestroy() {
        h.removeCallbacks(autoStop);
        release();
        stopForeground(STOP_FOREGROUND_REMOVE);
        if (AlarmActivity.current != null) AlarmActivity.current.finish();
        super.onDestroy();
    }
}
