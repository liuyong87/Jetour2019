package com.usbscandemo.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.usbscandemo.R;
import com.usbscandemo.usb.MediaScannerManager;


public class MainActivity extends AppCompatActivity {
    private TextView tv_scan_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    private void init() {
        tv_scan_state = (TextView) findViewById(R.id.tv_scan_state);
        MediaScannerManager.getInstance().registerOnUSBScanStateListener(mOnUSBScanStateListener);
    }


    public void starScan(View v) {
        MediaScannerManager.getInstance().onUSBMounted("/storage/udisk/");
    }

    public void stopScan(View v) {
        MediaScannerManager.getInstance().onUSBUnMounted("/storage/udisk/");
    }


    private MediaScannerManager.OnUSBScanStateListener mOnUSBScanStateListener = new MediaScannerManager.OnUSBScanStateListener() {
        @Override
        public void onScanStart(int usbFlag) {
            setScanState("扫描开始...");
        }

        @Override
        public void onScanning(int usbFlag) {

        }

        @Override
        public void onScanStoped(int usbFlag) {
            setScanState("扫描强制停止...");
        }

        @Override
        public void onScanDone(int usbFlag) {
            setScanState("扫描完成...");
        }
    };


    private void setScanState(final String text) {
        tv_scan_state.post(new Runnable() {
            @Override
            public void run() {
                tv_scan_state.setText(text);
            }
        });
    }


}
