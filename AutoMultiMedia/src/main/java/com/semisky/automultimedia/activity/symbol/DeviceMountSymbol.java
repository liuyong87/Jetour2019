package com.semisky.automultimedia.activity.symbol;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.semisky.automultimedia.BR;

/**
 * Created on 2017/12/14.
 * Author: xiongjun
 * About:
 */

public class DeviceMountSymbol extends BaseObservable{

    @Bindable
    private String state;


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
        notifyPropertyChanged(BR.state);
    }

}
