package com.semisky.automultimedia.common.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.semisky.automultimedia.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created on 2017/12/14.
 * Author: xiongjun
 * About: USB 挂载时弹出的dialog
 */

public class DeviceMountDialog extends Dialog {
    private Context mContext;
    private View view;
    private ImageView iv_anim;
    private TextView tv_state;

    public DeviceMountDialog(Context context) {
        super(context,R.style.DialogStyle);
        this.mContext = context;
        init();
    }

    private void init() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_usb_mount, null);
        setContentView(view);

        WindowManager.LayoutParams lParams = getWindow().getAttributes();
        lParams.width = 350;
        lParams.height = 250;
        lParams.gravity = Gravity.CENTER;

        // lParams.dimAmount = 0.9f;
        getWindow().setAttributes(lParams);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        // setCanceledOnTouchOutside(true);//触摸Dialog外部消失

        iv_anim = (ImageView) view.findViewById(R.id.img_load_anim);
        tv_state = (TextView) view.findViewById(R.id.tv_load_state);

        iv_anim.setVisibility(View.VISIBLE);
        Animation loadAnimation = AnimationUtils.loadAnimation(mContext, R.anim.loading_anim);
        iv_anim.startAnimation(loadAnimation);
    }

    public void updateState(String text){
        iv_anim.clearAnimation();
        iv_anim.setVisibility(View.GONE);
        tv_state.setText(text);

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                DeviceMountDialog.this.dismiss();
            }
        };
        timer.schedule(task,2000);
    }


}
