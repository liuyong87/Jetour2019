package com.semisky.automultimedia.common.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.semisky.automultimedia.R;

/**
 * Created by Administrator on 2018/3/30 0030.
 */

public class SelfDialogHint extends Dialog{
    private Button bt_Yes;//确定按钮

    private Button bt_No;//取消按钮

    private TextView tv_Title;//消息标题文本

    private TextView tv_Message;//消息提示文本

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String title;//从外界设置的title文本

    private String message;//从外界设置的消息文本

    //确定文本和取消文本的显示内容
    private String yesStr, noStr;

    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器

    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器

    public SelfDialogHint(@NonNull Context context) {
        super(context, R.style.MyDialog);
    }

//    public SelfDialogHint(@NonNull Context context, int theme) {
//        super(context, theme);
//    }
//
//    protected SelfDialogHint(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
//        super(context, cancelable, cancelListener);
//    }

    /**
     * 设置取消按钮的显示内容和监听
     *
     * @param str
     * @param onNoOnclickListener
     */
    public void setNoOnclickListener(String str, onNoOnclickListener onNoOnclickListener) {
        if (str != null) {
            noStr = str;
        }
        this.noOnclickListener = onNoOnclickListener;
    }

    /**
     * 设置确定按钮的显示内容和监听
     *
     * @param str
     * @param onYesOnclickListener
     */
    public void setYesOnclickListener(String str, onYesOnclickListener onYesOnclickListener) {
        if (str != null) {
            yesStr = str;
        }
        this.yesOnclickListener = onYesOnclickListener;
    }
    /**
     * 设置确定按钮和取消被点击的接口
     */
    public interface onYesOnclickListener {
       void onYesClick();
    }

    public interface onNoOnclickListener {
         void onNoClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_dialog);

        setCanceledOnTouchOutside(false);

        //初始化
        initView();

        //初始化dialog显示信息
        initData();

        //初始化Button事件
        initEvent();

    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        bt_Yes = (Button) findViewById(R.id.yes);
        bt_No = (Button) findViewById(R.id.no);
        tv_Title = (TextView) findViewById(R.id.title);
        tv_Message = (TextView) findViewById(R.id.message);
    }
    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        //如果用户自定了title和message
        if (title != null) {
            tv_Title.setText(title);
        }
        if (message != null) {
            tv_Message.setText(message);
        }
        //如果设置按钮的文字
        if (yesStr != null) {
            bt_Yes.setText(yesStr);
        }
        if (noStr != null) {
            bt_No.setText(noStr);
        }
    }
    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        bt_Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesOnclickListener != null) {
                    yesOnclickListener.onYesClick();
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        bt_No.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noOnclickListener != null) {
                    noOnclickListener.onNoClick();
                }
            }
        });
    }
}
