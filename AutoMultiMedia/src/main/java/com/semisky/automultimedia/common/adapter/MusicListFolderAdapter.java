package com.semisky.automultimedia.common.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.semisky.automultimedia.R;
import com.semisky.automultimedia.databinding.ItemMusicFolderBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyong on 18-3-20.
 */

public class MusicListFolderAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mFolderList;
    private LayoutInflater inflater;
    private ItemMusicFolderBinding dataBinding;

    public MusicListFolderAdapter(Context mContext) {
        this.mContext = mContext;
        this.mFolderList = new ArrayList<String>();
        this.inflater = LayoutInflater.from(this.mContext);
    }

    public void updateList(@Nullable List<String> forlderList) {
        if (null != mFolderList) {
            this.mFolderList.clear();
            this.mFolderList.addAll(forlderList);
            notifyDataSetChanged();
        }
    }

    public List<String> getData() {
        return this.mFolderList;
    }

    @Override
    public int getCount() {
        return mFolderList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFolderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            dataBinding = DataBindingUtil.inflate(inflater, R.layout.item_musiclist_folder, parent, false);
            convertView = dataBinding.getRoot();
            convertView.setTag(dataBinding);
        } else {
            dataBinding = (ItemMusicFolderBinding) convertView.getTag();
        }

        if(mFolderList.get(position).equals("udisk")){
            dataBinding.tvFolderName.setText("Root");
        }else{
            if(mFolderList.get(position).equals("All Songs")){
                dataBinding.tvFolderName.setText(R.string.all_music);
            }else {
                dataBinding.tvFolderName.setText(mFolderList.get(position));
            }
        }
        return dataBinding.getRoot();
    }
}
