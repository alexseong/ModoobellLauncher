package com.modoobel.modoobelllauncher;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.modoobel.modoobelllauncher.adapter.CommAdapter;
import com.modoobel.modoobelllauncher.custom_obj.Common;
import com.modoobel.modoobelllauncher.custom_obj.InputButtonSet;
import com.modoobel.modoobelllauncher.custom_obj.MBDialogData;
import com.modoobel.modoobelllauncher.custom_obj.MBResponse;
import com.modoobel.modoobelllauncher.custom_obj.MBResponseArray;
import com.modoobel.modoobelllauncher.custom_obj.MainFuncButton;
import com.modoobel.modoobelllauncher.fcm.MyFirebaseMessagingService;
import com.sktelecom.playrtc.PlayRTC;
import com.sktelecom.playrtc.PlayRTCFactory;
import com.sktelecom.playrtc.config.PlayRTCConfig;
import com.sktelecom.playrtc.config.PlayRTCVideoConfig;
import com.sktelecom.playrtc.exception.RequiredConfigMissingException;
import com.sktelecom.playrtc.exception.RequiredParameterMissingException;
import com.sktelecom.playrtc.exception.UnsupportedPlatformVersionException;
import com.sktelecom.playrtc.observer.PlayRTCObserver;
import com.sktelecom.playrtc.stream.PlayRTCMedia;
import com.sktelecom.playrtc.util.ui.PlayRTCVideoView;

import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

import static com.modoobel.modoobelllauncher.custom_obj.Common.DIALOG_ID;
import static com.modoobel.modoobelllauncher.custom_obj.Common.FROM_BELL;
import static com.modoobel.modoobelllauncher.custom_obj.Common.IS_ID_DATA;
import static com.modoobel.modoobelllauncher.custom_obj.Common.IS_START;
import static com.modoobel.modoobelllauncher.custom_obj.Common.RESPONSE_ID;

public class MainActivity extends AppCompatActivity {

    public static boolean isRunning = false;

    InputButtonSet inputButtonSet;
    MBResponseArray mbResponseArray;
    MBResponse mbResponse;
    CommAdapter adapter;
    RecyclerView recyclerView;
    Context mContext;
    boolean registerOK = false;

    private PlayRTCObserver playrtcObserver;
    private PlayRTC playrtc = null;
    private PlayRTCVideoView remoteView;
    private PlayRTCMedia remoteMedia;
    private PlayRTCMedia localMedia;
    private AlertDialog closeDialog;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
            } else {

                registerOK = false;

                String deviceName = android.os.Build.MODEL;

                Common.sendDeviceInfo(result.getContents(), deviceName,new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        final ProgressDialog mProgressDialog = ProgressDialog.show(MainActivity.this,"", getString(R.string.wait_plz),true);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                mProgressDialog.dismiss();

                                if (registerOK)
                                {
                                    Common.showMessage(MainActivity.this,getString(R.string.reg_ok));
                                }else {
                                    Common.showMessage(MainActivity.this,getString(R.string.reg_fail));
                                }
                            }
                        },3000);
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
        Fabric.with(this, new Crashlytics());

        isRunning = true;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        registerReceiver(mMessageReceiver, new IntentFilter(MyFirebaseMessagingService.INTENT_FILTER));
        mbResponseArray = MBResponseArray.initMBMessageData(this);

        initUI();

    }

    public void clkMenu(View v)
    {
        Intent i = new Intent(this,MenuActivity.class);
        startActivity(i);
    }


    private void initUI()
    {
        mContext = this;
        inputButtonSet = (InputButtonSet) findViewById(R.id.input_btn_set);
        inputButtonSet.setOnClickInputButtonListner(mClickInputButtonListner);
        inputButtonSet.setEmptyInputButton();
        //*********************************************************************
        // 비디오뷰 16:9로 설정

        RelativeLayout videoLayout = (RelativeLayout)findViewById(R.id.video_layout);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);

        float imageWidth = metrics.widthPixels;
        float imageHeight = imageWidth/16 * 9;

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) videoLayout.getLayoutParams();
        lp.width = (int) imageWidth;
        lp.height = (int) imageHeight;
        videoLayout.setLayoutParams(lp);

        //*********************************************************************

        long dialogId = getIntent().getLongExtra(DIALOG_ID,0);


        if (dialogId == 0) {
            dialogId = Common.getCurrentStatus(this);
        }


        adapter = new CommAdapter(this);
        if (dialogId > 0)
            adapter.reloadAdapter(dialogId);

        recyclerView = (RecyclerView) findViewById(R.id.list_view);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adapter);


        //*******************************
        // 하단 메시지 선택 구성
        mbResponse = mbResponseArray.getMBResponse(adapter.responseId);

        if (mbResponse == null)
        {
            mbResponse = new MBResponse("",adapter.getLastDialogMessageId());
        }

        Log.d("Tag","" + mbResponse);

        if (adapter.getLastDialogMessageId() == null || mbResponse == null) {
            inputButtonSet.setEmptyInputButton();
        }else {
            inputButtonSet.setInputButton(mbResponse, adapter.getLastDialogMessageId());
        }


        createVideoView();

        if (!Common.getPlayRtcChanelId(this).equals(""))
        {
            createPlayRTCInstance();
        }

    }


    private void createPlayRTCInstance() {

        createPlayRTCObserverInstance();


        try {
            PlayRTCConfig setting = setPlayRTCConfiguration();
            playrtc = PlayRTCFactory.createPlayRTC(setting, playrtcObserver);
            connectChannel();


        } catch (UnsupportedPlatformVersionException e) {
            e.printStackTrace();

        } catch (RequiredParameterMissingException e) {
            e.printStackTrace();
        }

        Handler handler = new Handler();
        handler.postDelayed(saveVisitorPicture,500);
    }

    private PlayRTCConfig setPlayRTCConfiguration() {

        PlayRTCConfig settings = PlayRTCFactory.createConfig();
        // PlayRTC instance have to get the application context.
        settings.setAndroidContext(getApplicationContext());

        // T Developers Project Key.
        settings.setProjectId(Common.playRtcKey);
        // video는 기본 640x480 30 frame
        settings.video.setEnable(true);
        settings.video.setCameraType(PlayRTCVideoConfig.CameraType.Front);
        settings.audio.setEnable(true);
        settings.audio.setAudioManagerEnable(true); //음성 출력 장치 자동 선택 기눙 활성화
        settings.data.setEnable(false);

        return settings;
    }


    private void connectChannel() {
        try {

            playrtc.connectChannel(Common.getPlayRtcChanelId(this), new JSONObject());
            playrtc.createChannel(new JSONObject());
        } catch (RequiredConfigMissingException e) {
            e.printStackTrace();
        }
    }

    private void createVideoView() {
        // Set the videoViewGroup which is contained local and remote video views.
        RelativeLayout myVideoViewGroup = (RelativeLayout) findViewById(R.id.video_layout);

        if (remoteView != null) {
            return;
        }

        // Give my screen size to child view.
        Point myViewDimensions = new Point();
        myViewDimensions.x = myVideoViewGroup.getWidth();
        myViewDimensions.y = myVideoViewGroup.getHeight();

        if (remoteView == null) {
            createRemoteVideoView(myViewDimensions, myVideoViewGroup);
        }

    }

    private void createRemoteVideoView(final Point parentViewDimensions, RelativeLayout parentVideoViewGroup) {
        if (remoteView == null) {

            // Create the view parameters.
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            remoteView = new PlayRTCVideoView(parentVideoViewGroup.getContext());
            remoteView.setBgClearColor(200, 200, 200, 255);
            remoteView.setLayoutParams(param);
            remoteView.initRenderer();

            // Add the view to the videoViewGroup.

            parentVideoViewGroup.addView(remoteView,0);
        }
    }

    public void clkFunc(View v) {
        MainFuncButton btn = (MainFuncButton)v;
//        btn.setON(!btn.isOn);


        if (v.getId() == R.id.btn_mic)
        {
            setMicON(!btn.isOn);
        }

        if (v.getId() == R.id.btn_speaker) {
            setSpeakerON(!btn.isOn);
        }
    }


    public void setMicON(boolean isOn)
    {
        MainFuncButton btn = (MainFuncButton)findViewById(R.id.btn_mic);
        btn.setON(isOn);

        if (localMedia == null) return;

        if(isOn)
        {
            localMedia.setAudioMute(false);
        }
        else
        {
            localMedia.setAudioMute(true);
        }
    }

    public void setSpeakerON(boolean isOn)
    {
        MainFuncButton btn = (MainFuncButton)findViewById(R.id.btn_speaker);
        btn.setON(isOn);

        if (remoteMedia == null) return;

        if(isOn)
        {
            remoteMedia.setAudioMute(false);
        }
        else
        {
            remoteMedia.setAudioMute(true);
        }
    }

    public void setAutoResponse(boolean isOn)
    {

    }


    //register your activity onResume()
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter(MyFirebaseMessagingService.INTENT_FILTER));
        isRunning = true;

        if (adapter.getItemCount() == 0) {
            findViewById(R.id.iv_chat_bg).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.iv_chat_bg).setVisibility(View.GONE);
        }

    }

    //Must unregister onPause()
    @Override
    protected void onPause() {
        super.onPause();

        try {
            this.unregisterReceiver(mMessageReceiver);
        }catch (IllegalArgumentException e) {}

        isRunning = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
    }

    @Override
    protected void onDestroy() {

        if(playrtc != null) {
            playrtc.close();
            playrtc = null;
        }

        remoteMedia = null;
        localMedia = null;

        try {
            this.unregisterReceiver(mMessageReceiver);
        }catch (IllegalArgumentException e) {}


        isRunning = false;

        super.onDestroy();

    }


    private InputButtonSet.onClickInputButtonListner mClickInputButtonListner = new InputButtonSet.onClickInputButtonListner() {
        @Override
        public void onClick(final String msgId) {

            inputButtonSet.setEmptyInputButton();

            //*************************************************************************************
            // 메시지 입력
            //*************************************************************************************
            String content = mbResponse.getMessageData(msgId).msg;
            MBDialogData mbDialogData = new MBDialogData(msgId,content,true,true);
            Common.addDialog(getBaseContext(),String.valueOf(adapter.dialogId),mbDialogData,mbResponse.id);
            adapter.reloadAdapter(adapter.dialogId);
            //*************************************************************************************


            Common.sendToMessage(getBaseContext(),mbResponse.id,msgId,adapter.dialogId,true,false,null);
        }

        @Override
        public void onClickMore() {

//            Intent intent = new Intent(MainActivity.this,MoreActionActivity.class);
//            startActivity(intent);
//            overridePendingTransition(0,0);
            showCloseDialogMessage();
        }

        @Override
        public void onClickSendMessage(final String message) {

            inputButtonSet.setEmptyInputButton();

            //*************************************************************************************
            // 메시지 입력
            //*************************************************************************************
            MBDialogData mbDialogData = new MBDialogData(message,message,true,true);
            Common.addDialog(getBaseContext(),String.valueOf(adapter.dialogId),mbDialogData,mbResponse.id);
            adapter.reloadAdapter(adapter.dialogId);
            //*************************************************************************************

            Common.sendToMessage(getBaseContext(), mbResponse.id,message,adapter.dialogId,false,false,null);

        }
    };



    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            if (intent.hasExtra(Common.IS_CLOSE_DIALOG))
            {
                adapter.reloadAdapter(adapter.dialogId);
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
                inputButtonSet.setDisEnable();
                Common.closeDialog(getBaseContext());
                return;
            }

            if (intent.hasExtra(Common.DEVICE_REG_OK))
            {
                registerOK = true;
                return;
            }

            if (intent.hasExtra(Common.VIDEO_CALL_CHANEL))
            {
                createPlayRTCInstance();
                return;
            }


            long dialogId = intent.getLongExtra(DIALOG_ID,0);

            if (dialogId != 0)
            {
                Common.startDialog(getBaseContext(),dialogId);

                findViewById(R.id.iv_chat_bg).setVisibility(View.GONE);

                adapter.reloadAdapter(dialogId);
                if (adapter.getItemCount() > 0)
                    recyclerView.scrollToPosition(adapter.getItemCount()-1);

                //*******************************
                // 하단 메시지 선택 구성
                mbResponse = mbResponseArray.getMBResponse(adapter.responseId);

                if (mbResponse == null)
                {
                    mbResponse = new MBResponse("",adapter.getLastDialogMessageId());
                }


                if (intent.getBooleanExtra(FROM_BELL,false))
                {
                    //*******************************
                    // 하단 메시지 선택 구성

                    boolean isIdData = intent.getBooleanExtra(IS_ID_DATA,false);

                    if (adapter.getLastDialogMessageId() == null || mbResponse == null || !isIdData) {
                        inputButtonSet.setEmptyInputButton();
                    }else {
                        inputButtonSet.setInputButton(mbResponse, adapter.getLastDialogMessageId());
                    }
                } else {

                    inputButtonSet.setEmptyInputButton();
                }

                if (intent.getBooleanExtra(IS_START,false))
                {
                    if (Common.isAutoResponse(getBaseContext(),intent.getStringExtra(RESPONSE_ID)))
                    {
                        setSpeakerON(false);
                        setAutoResponse(true);
                    }else {
                        setAutoResponse(false);
                    }
                }
            }
        }
    };



    private void showCloseDialogMessage()
    {

        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.alert_normal, null);
        TextView customTitle = (TextView)view.findViewById(R.id.title);
        customTitle.setText(getString(R.string.close_dialog));
        view.findViewById(R.id.message).setVisibility(View.GONE);

        final AlertDialog alert = new AlertDialog.Builder(this).setView(view).create();


        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.sendCloseDialogMessage(getBaseContext(), adapter.dialogId, adapter.responseId, new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        Common.closeDialog(getBaseContext());
                    }
                });

                alert.dismiss();     //닫기
            }
        });


        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        alert.show();

    }


    //***************************************************************
    // 화상통화 관련 옵저버

    private void createPlayRTCObserverInstance() {
        playrtcObserver = new PlayRTCObserver() {
            @Override
            public void onConnectChannel(final PlayRTC obj, final String channelId, final String channelCreateReason) {

                Log.d("Tag", "channelId : " + channelId + " channelCreateReason : " + channelCreateReason);

                findViewById(R.id.iv_movie_bg).setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAddLocalStream(final PlayRTC obj, final PlayRTCMedia playRTCMedia) {
                localMedia = playRTCMedia;
                MainFuncButton btn = (MainFuncButton)findViewById(R.id.btn_mic);
                if (btn.isOn) {
                    localMedia.setAudioMute(false);
                }else {
                    localMedia.setAudioMute(true);
                }
            }

            @Override
            public void onAddRemoteStream(final PlayRTC obj, final String peerId, final String peerUserId, final PlayRTCMedia playRTCMedia) {
                long delayTime = 0;

                remoteMedia = playRTCMedia;

                MainFuncButton btn = (MainFuncButton)findViewById(R.id.btn_speaker);
                if (btn.isOn) {
                    remoteMedia.setAudioMute(false);
                }else {
                    remoteMedia.setAudioMute(true);
                }


                remoteView.show(delayTime);
                // Link the media stream to the view.
                playRTCMedia.setVideoRenderer(remoteView.getVideoRenderer());

            }

            @Override
            public void onDisconnectChannel(final PlayRTC obj, final String disconnectReason) {
                findViewById(R.id.iv_movie_bg).setVisibility(View.VISIBLE);
            }

            @Override
            public void onOtherDisconnectChannel(final PlayRTC obj, final String peerId, final String peerUserId) {

            }
        };
    }

    /**
     * 이미지 저장 Runnable
     */

    private Runnable saveVisitorPicture = new Runnable() {
        @Override
        public void run() {

            if (remoteView == null)
            {
                Handler handler = new Handler();
                handler.postDelayed(saveVisitorPicture,500);
                return;
            }

            remoteView.snapshot(new PlayRTCVideoView.SnapshotObserver() {
                @Override
                public void onSnapshotImage(Bitmap bitmap) {
                    Common.saveVisitorPicture(MainActivity.this,bitmap,adapter.dialogId,saveVisitorPicture);
                }
            });
        }
    };
}
