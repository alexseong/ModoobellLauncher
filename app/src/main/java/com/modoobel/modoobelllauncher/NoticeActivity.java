package com.modoobel.modoobelllauncher;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.modoobel.modoobelllauncher.custom_obj.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class NoticeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Notice> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        getSupportActionBar().setTitle("공지사항");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String noticeData = Common.getRawDataToString(this, R.raw.notice);

        try {
            JSONArray jsonArray = new JSONArray(noticeData);

            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jObj = jsonArray.getJSONObject(i);
                Notice notice = new Notice();
                notice.title = jObj.getString("title");
                notice.content = jObj.getString("content");

                arrayList.add(notice);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        recyclerView = (RecyclerView)findViewById(R.id.list_view);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(new NoticeAdapter(this));

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

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            boolean isClicked = !Boolean.parseBoolean(String.valueOf(v.getTag()));
            v.setTag(isClicked);

            ImageView ivArrow = (ImageView) v.findViewById(R.id.iv_arrow);
            TextView tvContent = (TextView) v.findViewById(R.id.content);

            if (isClicked)
            {
                ivArrow.setImageResource(R.drawable.ic_keyboard_arrow_up);
                tvContent.setVisibility(View.VISIBLE);
            }else {
                ivArrow.setImageResource(R.drawable.ic_keyboard_arrow_down);
                tvContent.setVisibility(View.GONE);
            }


        }
    };


    public class NoticeAdapter extends RecyclerView.Adapter {

        Context mContext;

        public NoticeAdapter(Context context)
        {
            mContext = context;
        }

        @Override
        public int getItemViewType(int position) {

            if (position == 0)
            {
                return 0;
            }else {
                return 1;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;

            if (viewType == 0)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_notice, parent, false);
            }else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_notice_2, parent, false);
            }

            view.setOnClickListener(mOnClickListener);

            return new ViewHolderNotice(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            ViewHolderNotice viewHolderNotice = (ViewHolderNotice)holder;
            Notice notice = arrayList.get(position);
            viewHolderNotice.tvTitle.setText(notice.title);
            viewHolderNotice.tvContent.setText(notice.content);

            if (position == 0)
            {
                LinearLayout bg = (LinearLayout) holder.itemView.findViewById(R.id.bg);

                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) bg.getLayoutParams();
                lp.topMargin = (int)mContext.getResources().getDimension(R.dimen.activity_vertical_margin);
                bg.setLayoutParams(lp);

            }

        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }


    public class ViewHolderNotice extends RecyclerView.ViewHolder {

        ImageView ivArrow;
        TextView tvTitle;
        TextView tvContent;

        public ViewHolderNotice(View itemView) {
            super(itemView);

            ivArrow = (ImageView) itemView.findViewById(R.id.iv_arrow);
            tvTitle = (TextView)itemView.findViewById(R.id.title);
            tvContent = (TextView)itemView.findViewById(R.id.content);

            itemView.setTag(false);
            itemView.setOnClickListener(mOnClickListener);
        }

    }

    public class Notice
    {
        public String title;
        public String content;
    }


}
