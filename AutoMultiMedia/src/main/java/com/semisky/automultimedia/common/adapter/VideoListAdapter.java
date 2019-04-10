package com.semisky.automultimedia.common.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.semisky.automultimedia.R;
import com.semisky.automultimedia.common.bean.VideoInfo;
import com.semisky.automultimedia.databinding.ItemVideolistBinding;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by 熊俊 on 2018/1/8.
 */

public class VideoListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private List<VideoInfo> mVedioList;
    private ItemVideolistBinding dataBinding;

    public VideoListAdapter(Context mContext) {
        this.mContext = mContext;
        inflater = LayoutInflater.from(this.mContext);
        mVedioList = new ArrayList<VideoInfo>();
    }

    public void setmVedioList(List<VideoInfo> vedioList) {
        this.mVedioList.clear();
        this.mVedioList.addAll(vedioList);
        notifyDataSetChanged();
    }

    public List<VideoInfo> getmVedioList() {
        return mVedioList;
    }

    @Override
    public int getCount() {
        return mVedioList == null? 0: mVedioList.size();
    }

    @Override
    public Object getItem(int position) {
        return mVedioList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            dataBinding = DataBindingUtil.inflate(inflater, R.layout.item_videolist, parent, false);
            convertView = dataBinding.getRoot();
            convertView.setTag(dataBinding);
        } else {
            dataBinding = (ItemVideolistBinding) convertView.getTag();
        }
        dataBinding.setVideoItem( mVedioList.get(position));
        dataBinding.setVideoListAdapter(this);
        return convertView;
    }

}
