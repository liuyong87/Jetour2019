package com.semisky.automultimedia.common.utils;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Environment;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.semisky.automultimedia.R;

import java.io.File;
import java.util.Locale;

import static com.semisky.automultimedia.common.constant.Definition.MusicPlayModel.MODE_CIRCLE_ALL;
import static com.semisky.automultimedia.common.constant.Definition.MusicPlayModel.MODE_CIRCLE_SINGL;
import static com.semisky.automultimedia.common.constant.Definition.MusicPlayModel.MODE_RANDOM;

/**
 * Created on 2017/12/15.
 * Author: xiongjun
 * About:
 */

public class DataBindingUtils {

    @BindingAdapter({"imagesrc"})
    public static void loadImage(ImageView imageView,Drawable id){
        imageView.setImageDrawable(id);
    }

    @BindingAdapter({"imageurl"})
    public static void loadImage(ImageView imageView,String url){
        Glide.with(imageView.getContext()).load(url).placeholder(R.drawable.icon_picture_small).error(R.drawable.icon_invalid_m).into(imageView);
    }

    @BindingAdapter({"bigimageurl"})
    public static void loadBigImage(ImageView imageView,String url){
        //icon_invalid_small
        Glide.with(imageView.getContext()).load(url).error(R.drawable.icon_invalid_small).into(imageView);
    }

    @BindingAdapter({"imagebytes"})
    public static void loadByteImage(ImageView imageView,byte[] bytes){
        if(null != bytes && bytes.length > 0){
              Glide.with(imageView.getContext())
                .load(bytes)
                .error(R.drawable.background_song_album_thumb)
                .override(337,350)
                .centerCrop()
                .into(imageView);
        }else {
            imageView.setImageResource(R.drawable.background_song_album_thumb);
        }

    }


    @BindingAdapter("playmodel")
    public static void loadImage(ImageView imageView,int mode){
        if (mode == MODE_RANDOM){
            imageView.setImageResource(R.drawable.music_playmode_random);
        }else if (mode == MODE_CIRCLE_ALL){
            imageView.setImageResource(R.drawable.music_playmode_all);
        }else if (mode == MODE_CIRCLE_SINGL){
            imageView.setImageResource(R.drawable.music_playmode_one);
        }
    }

    @BindingAdapter("time")
    public static void loadText(TextView textView,int progress){
        textView.setText(getVideoFormatTime(progress));
    }

    @BindingAdapter("setvisiable")
    public static void setVisiable(View view,boolean visiable){
        if (visiable)
        view.setVisibility(View.VISIBLE);
        else
            view.setVisibility(View.GONE);
    }

    public static String getVideoFormatTime(int time) {
        time /= 1000;
        long min = time / 60 % 60;
        long hour = time / 60 / 60;
        long second = time % 60;
        if (time < 3600) {
            return String.format(Locale.getDefault(), "%02d:%02d", min, second).toString();
        }
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, min, second).toString();
    }

}
