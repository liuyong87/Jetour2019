package com.semisky.automultimedia.common.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.semisky.automultimedia.R;
import com.semisky.automultimedia.common.bean.MusicInfo;
import com.semisky.automultimedia.common.data.MusicDataModule;
import com.semisky.automultimedia.databinding.ItemMusiclistAllSongsBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有歌曲列表适配器
 * Created by liuyong on 18-3-20.
 */

public class MusicListAllSongsAdapter extends BaseAdapter {

    private Context mContext;
    private List<MusicInfo > mMusicList;
    private LayoutInflater inflater;
    private ItemMusiclistAllSongsBinding dataBinding;

    public MusicListAllSongsAdapter(Context mContext) {
        this.mContext = mContext;
        mMusicList = new ArrayList<MusicInfo>();
        inflater = LayoutInflater.from(this.mContext);
    }

    public void updateList(@Nullable List<MusicInfo> musicList){
        if(null != mMusicList){
            this.mMusicList.clear();
            this.mMusicList.addAll(musicList);
            notifyDataSetChanged();
        }
    }

    public List<MusicInfo> getData(){
        return this.mMusicList;
    }

    @Override
    public int getCount() {
        return mMusicList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMusicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(null == convertView){
            dataBinding = DataBindingUtil.inflate(inflater,R.layout.item_musiclist_all_songs,parent,false);
            convertView = dataBinding.getRoot();
            convertView.setTag(dataBinding);
        }else{
            dataBinding = (ItemMusiclistAllSongsBinding)convertView.getTag();
        }
        dataBinding.tvFileName.setText(mMusicList.get(position).getDisplayName());
        if(mMusicList.get(position).getUrl().equals(MusicDataModule.getInstance().getCurrentPlayMusicUrl())){
            dataBinding.tvFileName.setTextColor(mContext.getResources().getColor(R.color.color_music_playing));
            dataBinding.ivPlayingIcon.setVisibility(View.VISIBLE);
            dataBinding.llSongItem.setBackgroundResource(R.drawable.background_playing);
        }else {
            dataBinding.tvFileName.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            dataBinding.ivPlayingIcon.setVisibility(View.GONE);
            dataBinding.llSongItem.setBackgroundResource(R.drawable.selector_common_item_bg);
        }
        return dataBinding.getRoot();
    }
}
