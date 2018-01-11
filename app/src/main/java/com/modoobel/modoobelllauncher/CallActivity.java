package com.modoobel.modoobelllauncher;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import static com.modoobel.modoobelllauncher.custom_obj.Common.DIALOG_ID;

public class CallActivity extends AppCompatActivity {

    public static boolean isRunning = false;

    private ImageView imageView;
    Vibrator vide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        isRunning = true;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        initUI();
    }


    public void initUI()
    {
        vide = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        imageView = (ImageView) findViewById(R.id.icon);
        AnimationDrawable frameAnimation = (AnimationDrawable) imageView.getBackground();
        frameAnimation.start();

        Animation anim = AnimationUtils.loadAnimation(
                getApplicationContext(), // 현재 화면의 제어권자
                R.anim.call_ani_rotate);    // 설정한 에니메이션 파일
        imageView.startAnimation(anim);

        long[] pattern = { 0, 1500, 500};
        vide.vibrate(pattern, 0);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN)
        {
            Intent intent = new Intent(CallActivity.this, MainActivity.class);
            intent.putExtra(DIALOG_ID,getIntent().getLongExtra(DIALOG_ID,0));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        return false;



    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vide.cancel();
        isRunning =false;
    }
}
