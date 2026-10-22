package com.malafi.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** يعيد جدولة كل المنبهات بعد إعادة تشغيل الهاتف أو تغيير الوقت */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        Scheduler.rescheduleAll(c);
    }
}
