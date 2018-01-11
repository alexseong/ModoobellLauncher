package com.modoobel.modoobelllauncher.fcm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.modoobel.modoobelllauncher.CallActivity;

import static com.modoobel.modoobelllauncher.custom_obj.Common.DIALOG_ID;

/**
 * Created by luckyleeis on 2017. 1. 10..
 */

public class BroadcastAlarm extends BroadcastReceiver {
    String INTENT_ACTION = Intent.ACTION_BOOT_COMPLETED;

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent( context, CallActivity.class );
        i.putExtra(DIALOG_ID,intent.getLongExtra(DIALOG_ID,0));
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT);

        try {
            pi.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

        Log.d("Tag","onReceive");

    }

    
}


