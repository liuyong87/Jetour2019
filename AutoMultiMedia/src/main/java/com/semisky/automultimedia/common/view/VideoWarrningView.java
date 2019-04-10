package com.semisky.automultimedia.common.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.semisky.automultimedia.R;
import com.semisky.automultimedia.activity.SwitchActivity;

/**
 * Created by liuyong on 18-2-26.
 */

public class VideoWarrningView extends LinearLayout {
    private Button btn_back;

    public VideoWarrningView(Context context) {
        this(context, null);
    }

    public VideoWarrningView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public VideoWarrningView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context, attrs, defStyle);
    }

    private void initViews(Context context, @Nullable AttributeSet attrs, int defStyle) {

        LayoutInflater.from(context).inflate(R.layout.custom_video_warning_view, this);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClassName(getContext().getPackageName(), SwitchActivity.class.getName());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("type",1);
                getContext().startActivity(i);
            }
        });
    }

}
