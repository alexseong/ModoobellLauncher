package com.modoobel.modoobelllauncher;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.modoobel.modoobelllauncher.custom_obj.Common;
import com.modoobel.modoobelllauncher.fcm.MyFirebaseMessagingService;

import static com.modoobel.modoobelllauncher.custom_obj.Common.NOTIFICATION_KEY;

public class MenuActivity extends Activity {

    ProgressDialog mProgressDialog;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
            } else {

                String deviceName = android.os.Build.MODEL;
                Common.sendDeviceInfo(result.getContents(), deviceName,new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        mProgressDialog = ProgressDialog.show(MenuActivity.this,"", getString(R.string.wait_plz),true);
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        registerReceiver(mMessageReceiver, new IntentFilter(MyFirebaseMessagingService.INTENT_FILTER_REG));

    }


    public void clkMenuButton(View v)
    {
        int id = v.getId();

        if (id == R.id.btn_close) {

            finish();
            overridePendingTransition(0,0);


        }else if (id == R.id.btn_notice) {

            Intent i = new Intent(MenuActivity.this,NoticeActivity.class);
            startActivity(i);


        }else if (id == R.id.btn_auto_response) {

            Intent i = new Intent(MenuActivity.this,AutoResponseSettingActivity.class);
            startActivity(i);

        }else if (id == R.id.btn_history) {

            Intent i = new Intent(MenuActivity.this,VisitHistoryActivity.class);
            startActivity(i);

        }else if (id == R.id.btn_add_device) {

            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setOrientationLocked(false);
            integrator.setBeepEnabled(false);
            integrator.initiateScan();


        }

//        else if (id == R.id.btn_manage_device) {
//
//
//            String deviceName = android.os.Build.MODEL;
//            Common.sendDeviceInfo("cUgCJQksDt0:APA91bGEFtCvvw7GrXMSbx_7YVSYK5km3XR-Jml7MxcG9dZINHYuxsIwmM-6inzN2tdUXFEFmUHYqlYeDkC5MXYRCGK7FxHIMjs61-CVdHTUhf3dY-f6qGzf-PoDggLWLJklU1qdiWrf", deviceName,new Handler(){
//                @Override
//                public void handleMessage(Message msg) {
//                    super.handleMessage(msg);
//                    mProgressDialog = ProgressDialog.show(MenuActivity.this,"", getString(R.string.wait_plz),true);
//                }
//            });
//        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(Common.DEVICE_REG_OK))
            {
                if (mProgressDialog != null)
                    mProgressDialog.dismiss();

                Common.saveNotificationKey(getBaseContext(),intent.getStringExtra(NOTIFICATION_KEY));
                Common.showMessage(MenuActivity.this,getString(R.string.reg_ok));

                return;
            }
        }
    };


    @Override
    protected void onDestroy() {
        this.unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


}
