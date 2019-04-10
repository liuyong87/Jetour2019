package com.semisky.automultimedia.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.semisky.automultimedia.activity.symbol.BaseSymbol;

/**
 * Created by 熊俊 on 2018/1/5.
 */

public abstract class BaseFragment extends Fragment {
    Class<?> mServiceClass = null;

    public BaseSymbol mMySymbol = null;


    public abstract void createFragment(Bundle savedInstanceState);
    public abstract void startDataBinding();

    protected void setServiceClass(Class<?> serviceClass) {
        mServiceClass = serviceClass;
    }

    public void setmMySymbol(BaseSymbol mMySymbol) {
        this.mMySymbol = mMySymbol;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startDataBinding();
        createFragment(savedInstanceState);

        if (mServiceClass!=null){
            Intent bindIntent = new Intent(getActivity(), mServiceClass);
            getActivity().bindService(bindIntent, mMySymbol.mConn, Context.BIND_AUTO_CREATE);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //需要模式管理追加接口，在onDestroy调用前进行
        if (mServiceClass!=null){
            if (mMySymbol.mIsBound) {
                mMySymbol.unRegister();
            }
            getActivity().unbindService(mMySymbol.mConn);
            mMySymbol = null;
        }

    }

}
