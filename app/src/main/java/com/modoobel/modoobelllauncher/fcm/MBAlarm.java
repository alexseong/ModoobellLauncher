package com.modoobel.modoobelllauncher.fcm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.modoobel.modoobelllauncher.custom_obj.Common.DIALOG_ID;

/**
 * Created by luckyleeis on 2017. 1. 10..
 */

public class MBAlarm {
    private Context context;
    public MBAlarm(Context context) {
        this.context=context;
    }
    public void Alarm(long dialogId) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BroadcastAlarm.class);

        intent.putExtra(DIALOG_ID,dialogId);

        Log.d("Tag","DIALOG_ID : " + dialogId);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1,  sender);
    }
}


