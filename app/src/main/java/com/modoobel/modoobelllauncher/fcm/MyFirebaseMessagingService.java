/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.modoobel.modoobelllauncher.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.modoobel.modoobelllauncher.AutoResponseSettingActivity;
import com.modoobel.modoobelllauncher.CallActivity;
import com.modoobel.modoobelllauncher.MainActivity;
import com.modoobel.modoobelllauncher.R;
import com.modoobel.modoobelllauncher.custom_obj.Common;
import com.modoobel.modoobelllauncher.custom_obj.MBDialogData;
import com.modoobel.modoobelllauncher.custom_obj.MBResponseArray;

import static com.modoobel.modoobelllauncher.custom_obj.Common.DEVICE_REG_OK;
import static com.modoobel.modoobelllauncher.custom_obj.Common.DIALOG_ID;
import static com.modoobel.modoobelllauncher.custom_obj.Common.FROM;
import static com.modoobel.modoobelllauncher.custom_obj.Common.FROM_BELL;
import static com.modoobel.modoobelllauncher.custom_obj.Common.IS_CLOSE_DIALOG;
import static com.modoobel.modoobelllauncher.custom_obj.Common.IS_ID_DATA;
import static com.modoobel.modoobelllauncher.custom_obj.Common.IS_START;
import static com.modoobel.modoobelllauncher.custom_obj.Common.MESSAGE_ID;
import static com.modoobel.modoobelllauncher.custom_obj.Common.NOTIFICATION_KEY;
import static com.modoobel.modoobelllauncher.custom_obj.Common.RESPONSE_ID;
import static com.modoobel.modoobelllauncher.custom_obj.Common.SEND_AUTO_RESPONSE_DATA;
import static com.modoobel.modoobelllauncher.custom_obj.Common.VIDEO_CALL_CHANEL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "Tag";
    public static final String INTENT_FILTER = "INTENT_FILTER";
    public static final String INTENT_FILTER_REG = "Register_device";
    public static final String INTENT_FILTER_SEND_AUTO_RESPONSE = "send_auto_reponse";
    public static final String INTENT_FILTER_REQUEST_AUTO_RESPONSE = "request_auto_response";



    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, new Notification());
    }


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // 내가보낸 메시지의 경우 리턴한다.

        if (remoteMessage.getData().containsKey(FROM)) {
            if (remoteMessage.getData().get(FROM).equals(FirebaseInstanceId.getInstance().getToken()))
            {
                return;
            }
        }

        // 디바이스 등록 모드일때
        if (remoteMessage.getData().containsKey(DEVICE_REG_OK))
        {
            Intent intent = new Intent(INTENT_FILTER_REG);
            intent.putExtra(DEVICE_REG_OK,remoteMessage.getData().get(DEVICE_REG_OK));
            intent.putExtra(NOTIFICATION_KEY,remoteMessage.getData().get(NOTIFICATION_KEY));
            sendBroadcast(intent);
            return;
        }

        // 자동응답 세팅데이터를 받아올때

        if (remoteMessage.getData().containsKey(SEND_AUTO_RESPONSE_DATA))
        {
            Bundle bundle = new Bundle();

            for (String key : remoteMessage.getData().keySet())
            {
                bundle.putBoolean(key,Boolean.parseBoolean(remoteMessage.getData().get(key)));
            }

            Common.setAutoResponseSetting(getBaseContext(),bundle);


            if (AutoResponseSettingActivity.isDoingSettingAutoResponse) {
                Intent intent = new Intent(INTENT_FILTER_SEND_AUTO_RESPONSE);
                intent.putExtras(bundle);
                sendBroadcast(intent);
            }

            return;
        }


        Intent intent = new Intent(INTENT_FILTER);
        long dialogId = 0;

        // 다이아로그 아이디가 있을때 (대화 일때)
        if (remoteMessage.getData().containsKey(DIALOG_ID)) {
            dialogId = Long.parseLong(remoteMessage.getData().get(DIALOG_ID));
        }

        // 대화 종료 메시지를 받을때
        if (remoteMessage.getData().containsKey(IS_CLOSE_DIALOG))
        {
            MBDialogData mbDialogData = new MBDialogData();
            Common.addDialog(getBaseContext(),String.valueOf(dialogId),mbDialogData,remoteMessage.getData().get(RESPONSE_ID));

            intent.putExtra(IS_CLOSE_DIALOG,remoteMessage.getData().get(IS_CLOSE_DIALOG));
        }

        // 영상통화 채널 아이디를 받을때
        else if (remoteMessage.getData().containsKey(VIDEO_CALL_CHANEL)) {
            Common.setPlayRtcChanelId(getBaseContext(),remoteMessage.getData().get(VIDEO_CALL_CHANEL));
            intent.putExtra(VIDEO_CALL_CHANEL,remoteMessage.getData().get(VIDEO_CALL_CHANEL));
        }

        //대화일때
        else {

            String id = remoteMessage.getData().get(RESPONSE_ID);
            String msgId = remoteMessage.getData().get(MESSAGE_ID);
            boolean isIdData = Boolean.parseBoolean(remoteMessage.getData().get(IS_ID_DATA));
            boolean isFromBell = Boolean.parseBoolean(remoteMessage.getData().get(FROM_BELL));
            boolean isStart = Boolean.parseBoolean(remoteMessage.getData().get(IS_START));

            MBResponseArray arr = MBResponseArray.initMBMessageData(this);
            String content;

            if (isIdData) {
                content = arr.getMBResponse(id).getMessageData(msgId).msg;
            }else {
                content = msgId;
            }

            MBDialogData mbDialogData = new MBDialogData(msgId,content,!isFromBell,isIdData);
            Common.addDialog(getBaseContext(),String.valueOf(dialogId),mbDialogData,id);

            intent.putExtra(FROM_BELL,isFromBell);
            intent.putExtra(IS_ID_DATA,isIdData);
            intent.putExtra(IS_START,isStart);
            intent.putExtra(RESPONSE_ID,id);
        }


        if (MainActivity.isRunning) {

            Log.d("Tag","isRunning");
            intent.putExtra(DIALOG_ID,dialogId);
            sendBroadcast(intent);
        }else {
            if (!CallActivity.isRunning && remoteMessage.getData().containsKey(IS_START))
            {
                Log.d("Tag","alarm");
                new MBAlarm(getApplicationContext()).Alarm(dialogId);
            }
        }



        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
