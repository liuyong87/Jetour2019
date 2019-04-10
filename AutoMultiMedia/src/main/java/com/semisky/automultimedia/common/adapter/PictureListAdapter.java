package com.semisky.automultimedia.common.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.semisky.automultimedia.BR;
import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.R;
import com.semisky.automultimedia.common.bean.PictureInfo;
import com.semisky.automultimedia.databinding.ItemPicturelistBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 熊俊 on 2017/12/28.
 */

public class PictureListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private List<PictureInfo> pictureInfos;

    private ItemPicturelistBinding dataBinding;

    public PictureListAdapter(Context mContext) {
        this.mContext = mContext;
        inflater = LayoutInflater.from(this.mContext);
        pictureInfos = new ArrayList<PictureInfo>();
    }

    public void setListUrl(List<PictureInfo> pictureInfos) {
        this.pictureInfos.clear();
        this.pictureInfos.addAll(pictureInfos);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return pictureInfos == null ? 0 : pictureInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return pictureInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            dataBinding = DataBindingUtil.inflate(inflater, R.layout.item_picturelist, parent, false);
            convertView = dataBinding.getRoot();
            convertView.setTag(dataBinding);
        } else {
            dataBinding = (ItemPicturelistBinding) convertView.getTag();
        }
        //todo 2018-4-11
        if (position==MediaApplication.getList_picturePosition()){
            dataBinding.frameLayout.setBackgroundResource(R.drawable.background_picture_item_pre);
        }else {
            dataBinding.frameLayout.setBackgroundResource(R.drawable.background_picture_list);
        }
        dataBinding.setVariable(BR.pictureinfo, pictureInfos.get(position));
        dataBinding.setAdapter(this);
        return convertView;
    }


}
