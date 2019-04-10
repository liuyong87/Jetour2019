package com.semisky.automultimedia.common.view;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.semisky.automultimedia.R;
import com.semisky.automultimedia.activity.symbol.DeviceMountSymbol;
import com.semisky.automultimedia.databinding.DeviceMount;

public class USBMountDialog extends Dialog {
    private Context mContext;
    private DeviceMount deviceMountData;
    private DeviceMountSymbol deviceMountSymbol;

    public interface StateMode {
        /**
         * U盘加载中
         */
        final int STATE_LOADDING = 20;
        /**
         * U盘加载成功
         */
        final int STATE_LOAD_SUCCESS = 21;
        /**
         * U盘加载失败
         */
        final int STATE_LOAD_FAIL = 22;
        /**
         * 没有有效媒体文件！
         */
        final int STATE_LOAD_NO_MEDIA = 23;
        /**
         * 请插入USB设备
         */
        final int STATE_LOAD_NO_USB = 24;
    }

    public USBMountDialog(@NonNull Context context) {
        super(context, R.style.DialogStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutParams lParams = getWindow().getAttributes();
        lParams.width = LayoutParams.MATCH_PARENT;
        lParams.height = LayoutParams.MATCH_PARENT;
        lParams.gravity = Gravity.CENTER;
        // lParams.dimAmount = 0.9f;
        getWindow().setAttributes(lParams);
        getWindow().addFlags(LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setType(LayoutParams.TYPE_SYSTEM_ALERT);
        deviceMountSymbol = new DeviceMountSymbol();
//        this.setCanceledOnTouchOutside(true);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        deviceMountData = DataBindingUtil.inflate(inflater, R.layout.dialog_usb_mount, null, false);
        setContentView(deviceMountData.getRoot());
        deviceMountData.setMountstate(deviceMountSymbol);
    }

    public void updateState(final int condition) {
        if (StateMode.STATE_LOADDING != condition) {
            deviceMountData.imgLoadAnim.clearAnimation();
            deviceMountData.imgLoadAnim.setVisibility(View.GONE);
        }

        if (StateMode.STATE_LOADDING == condition) {
            deviceMountSymbol.setState(mContext.getString(R.string.on_loading));
            deviceMountData.imgLoadAnim.setVisibility(View.VISIBLE);
            Animation loadAnimation = AnimationUtils.loadAnimation(mContext, R.anim.loading_anim);
            deviceMountData.imgLoadAnim.startAnimation(loadAnimation);
        } else if (StateMode.STATE_LOAD_FAIL == condition /*== -1*/) {
            deviceMountSymbol.setState(mContext.getString(R.string.load_failure));
        } else if (StateMode.STATE_LOAD_NO_MEDIA == condition /*4*/) {
            deviceMountSymbol.setState(mContext.getString(R.string.no_file));
        } else if (StateMode.STATE_LOAD_NO_USB == condition /*== 5*/) {
            deviceMountSymbol.setState(mContext.getString(R.string.hint_no_usb));
        } else if (StateMode.STATE_LOAD_SUCCESS == condition) {
            deviceMountSymbol.setState(mContext.getString(R.string.load_success));
        }

    }

}
