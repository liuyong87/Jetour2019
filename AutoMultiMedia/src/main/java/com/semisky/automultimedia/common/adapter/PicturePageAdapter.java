package com.semisky.automultimedia.common.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.semisky.automultimedia.R;
import com.semisky.automultimedia.common.bean.PictureInfo;
import com.semisky.automultimedia.common.pictureplay.PhotoView;
import com.semisky.automultimedia.databinding.ItemPicturePlayBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 熊俊 on 2018/1/17.
 */

public class PicturePageAdapter extends PagerAdapter implements OnClickListener, View.OnTouchListener {
    private Context mContext;
    private LayoutInflater inflater;
    private List<PictureInfo> mPictureList;
    private ItemPicturePlayBinding dataBinding;
    private PhotoView[] photoViews;
    private View.OnTouchListener mOnTouchListener;

    public PicturePageAdapter(Context mContext) {
        this.mContext = mContext;
        this.inflater = LayoutInflater.from(this.mContext);
        mPictureList = new ArrayList<PictureInfo>();
    }

    public void setmPictureList(List<PictureInfo> mPictureList) {
        this.mPictureList.clear();
        this.mPictureList.addAll(mPictureList);
        photoViews = new PhotoView[this.mPictureList.size()];
        notifyDataSetChanged();
    }

    public void registerOnTochListener(View.OnTouchListener listener) {
        this.mOnTouchListener = listener;
    }

    @Override
    public int getCount() {
        return mPictureList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.item_picture_play, container, false);
        dataBinding.setPicture(mPictureList.get(position));
        dataBinding.setAdapter(this);
        container.addView(dataBinding.getRoot());
        photoViews[position] = dataBinding.photoView;
        dataBinding.photoView.setOnClickListener(this);// 设置点击事件
        dataBinding.photoView.setOnTouchListener(this);

        return dataBinding.getRoot();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        photoViews[position] = null;
    }

    public void rotate(int position) {
        photoViews[position].handRotate(90);
    }

    public void scale(int position, float scaleFactor) {
        photoViews[position].handScale(scaleFactor);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(mOnTouchListener != null){
            mOnTouchListener.onTouch(v,event);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (null != mOnClickListener) {
            mOnClickListener.onClick(v);
        }
    }

    private OnClickListener mOnClickListener;

    public void registerOnClickListener(OnClickListener listener) {
        this.mOnClickListener = listener;
    }
    public float getScaleMaxValue(int position){

       return photoViews[position].getmScale();
    }
    public void reduction(int position){
        if (photoViews[position]!=null){
            photoViews[position].reduction();
        }
    }
}
