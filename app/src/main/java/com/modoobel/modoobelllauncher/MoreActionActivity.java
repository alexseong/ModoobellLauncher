package com.modoobel.modoobelllauncher;

import android.app.Activity;
import android.os.Bundle;

public class MoreActionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_more_action);
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0,0);
    }
}
