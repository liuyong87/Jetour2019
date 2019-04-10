package com.semisky.automultimedia.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.semisky.automultimedia.R;


/**
 * Created by liuyong on 18-1-29.
 */

public class CustomSingleMessageView extends LinearLayout {

    private TextView tv_show_message;

    public CustomSingleMessageView(Context context) {
        this(context, null);
    }

    public CustomSingleMessageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews(context, attrs);
    }


    private void initViews(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.custom_single_message_view, this);
        TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.CustomSingleMessageView);
        int attr_show_msg = attrArray.getResourceId(R.styleable.CustomSingleMessageView_csmv_text, -1);

        tv_show_message = (TextView) findViewById(R.id.tv_show_message);
        setShowMessageText(attr_show_msg);
        attrArray.recycle();
    }


    public void setShowMessageText(int resId) {
        if (resId != -1) {
            tv_show_message.setText(resId);
        }

    }

    public void setShowMessageText(@NonNull String resText) {
        tv_show_message.setText(resText);
    }


    public void show() {
        if (!this.isShown()) {
            this.setVisibility(VISIBLE);
        }
    }

    public void dismiss() {
        if (this.isShown()) {
            this.setVisibility(GONE);
        }
    }


}
