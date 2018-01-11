package com.modoobel.modoobelllauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.modoobel.modoobelllauncher.custom_obj.Common;
import com.modoobel.modoobelllauncher.custom_obj.MBResponseArray;

import static com.modoobel.modoobelllauncher.fcm.MyFirebaseMessagingService.INTENT_FILTER_SEND_AUTO_RESPONSE;

public class AutoResponseSettingActivity extends AppCompatActivity {

    Bundle bunSettingData;
    AutoResAdapter adapter;
    MBResponseArray mbResponseArray;

    public static boolean isDoingSettingAutoResponse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_response_setting);

        isDoingSettingAutoResponse = true;

        getSupportActionBar().setTitle("자동응답 모드 설정");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (Common.getNotificationKey(getBaseContext()).equals(""))
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();     //닫기
                    finish();
                }
            });
            alert.setMessage("벨을 먼저 등록 하세요.");
            alert.show();
        }else {

            bunSettingData = Common.getAutoResponseSetting(this);
            mbResponseArray = MBResponseArray.initMBMessageData(this);
            mbResponseArray.setAutoResponse();

            adapter = new AutoResAdapter(this);

            RecyclerView listView = (RecyclerView)findViewById(R.id.list_view);
            listView.setVerticalScrollBarEnabled(true);
            listView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
            listView.setAdapter(adapter);
        }

        registerReceiver(mMessageReceiver, new IntentFilter(INTENT_FILTER_SEND_AUTO_RESPONSE));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon action bar is clicked; go to parent activity

                this.finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        try {
            this.unregisterReceiver(mMessageReceiver);
        }catch (IllegalArgumentException e) {}


        Common.sendAutoResponseSettingData(getBaseContext(),null);
        isDoingSettingAutoResponse = false;

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            this.unregisterReceiver(mMessageReceiver);
        }catch (IllegalArgumentException e) {}

        isDoingSettingAutoResponse = false;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getExtras() != null)
            {
                bunSettingData = Common.getAutoResponseSetting(getBaseContext());
                adapter.notifyDataSetChanged();
                return;
            }
        }
    };




    //*************************************************************************
    // 리스트뷰 아답터
    //*************************************************************************


    public class AutoResAdapter extends RecyclerView.Adapter {

        Context mContext;

        public AutoResAdapter(Context context)
        {
            this.mContext = context;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_auto_response, parent, false);
            return new ViewHolderSetting(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            String id = mbResponseArray.getId(position);

            ViewHolderSetting v = (ViewHolderSetting)holder;

            Log.d("Tag","IDDD : " + id);

            v.tvTitle.setText(mbResponseArray.getResponseTitle(id));
            v.swSet.setChecked(bunSettingData.getBoolean(id,false));
            v.swSet.setTag(id);
            v.swSet.setOnCheckedChangeListener(checkedChangeListener);

        }

        private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Common.setAutoResponse(getBaseContext(), (String) buttonView.getTag(),isChecked);

            }
        };

        @Override
        public int getItemCount() {
            return mbResponseArray.getSize();
        }

    }



    public class ViewHolderSetting extends RecyclerView.ViewHolder {

        TextView tvTitle;
        Switch swSet;

        public ViewHolderSetting(View itemView) {
            super(itemView);

            tvTitle = (TextView)itemView.findViewById(R.id.tvTitle);
            swSet = (Switch) itemView.findViewById(R.id.swSet);
        }
    }

















}
