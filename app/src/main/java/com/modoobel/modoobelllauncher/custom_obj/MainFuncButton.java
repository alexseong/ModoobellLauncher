package com.modoobel.modoobelllauncher.custom_obj;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.modoobel.modoobelllauncher.R;

/**
 * Created by luckyleeis on 2017. 1. 7..
 */

public class MainFuncButton extends RelativeLayout {

    RelativeLayout bg;
    ImageView icon;
    TextView title;
    TextView status;
    public boolean isOn;
    Context mContext;

    public MainFuncButton(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public MainFuncButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
        getAttrs(attrs);
    }

    public MainFuncButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        getAttrs(attrs, defStyleAttr);
    }

    private void initView() {

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.main_func_button, this, false);
        addView(v);

        bg = (RelativeLayout) findViewById(R.id.bg);
        icon = (ImageView) findViewById(R.id.icon);
        title = (TextView) findViewById(R.id.title);
        status = (TextView) findViewById(R.id.status);
        isOn = true;

    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.main_func_button);

        setTypeArray(typedArray);
    }


    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.main_func_button, defStyle, 0);
        setTypeArray(typedArray);

    }

    private void setTypeArray(TypedArray typedArray) {

        int bg_resID = typedArray.getResourceId(R.styleable.main_func_button_main_btn_icon, R.drawable.icon_phone_01);
        icon.setBackgroundResource(bg_resID);
        String text_string = typedArray.getString(R.styleable.main_func_button_main_btn_title);
        title.setText(text_string);
        typedArray.recycle();

    }

    public void setON(boolean isOn) {

        int bg_resID;
        String strStatus;
        int color;

        if (isOn) {
            strStatus = "on";
            color = R.color.color_func_btn_option_selected;
            bg.setBackgroundResource(R.drawable.btn_main_func_selected);
        }
        else {
            strStatus = "off";
            color = R.color.color_func_btn_option_not_selected;
            bg.setBackgroundResource(R.drawable.btn_main_func_no_selected);
        }

        if (this.getId() == R.id.btn_mic) {

            if (isOn) bg_resID = R.drawable.icon_phone_01;
            else bg_resID = R.drawable.icon_phone_01_;

        }else if (this.getId() == R.id.btn_speaker) {

            if (isOn) bg_resID = R.drawable.icon_phone_02;
            else bg_resID = R.drawable.icon_phone_02_;

        }else {

            if (isOn) bg_resID = R.drawable.icon_phone_03;
            else bg_resID = R.drawable.icon_phone_03_;
        }

        icon.setBackgroundResource(bg_resID);
        status.setText(strStatus);
        status.setTextColor(mContext.getResources().getColor(color));

        this.isOn = isOn;
    }

}
