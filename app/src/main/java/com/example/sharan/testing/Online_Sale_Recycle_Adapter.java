package com.example.sharan.testing;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class Online_Sale_Recycle_Adapter extends Adapter<Online_Sale_Recycle_Adapter.CustomViewHolder>
{
    Context           con;
    HelperS           helperS;
    ArrayList<String> image_video_path_list;
    int width=0;

    public static class CustomViewHolder extends ViewHolder
    {
//        protected TextView  frame;
        protected ImageView imgv_thumbnail;

        public CustomViewHolder(View view)
        {
            super(view);
//            this.frame = (TextView) view.findViewById(R.id.frame);
            this.imgv_thumbnail = (ImageView) view.findViewById(R.id.imgv_thumbnail);



        }
    }

    public Online_Sale_Recycle_Adapter(Context con, ArrayList<String> image_video_path_list)
    {
        this.helperS = new HelperS();
        this.con = con;
        this.image_video_path_list = image_video_path_list;

        WindowManager wm = (WindowManager) con.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point   size    = new Point();
        display.getSize(size);
        width = size.x;


    }

    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new CustomViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_gallery_images_video_adapter, parent, false));
    }

    public void onBindViewHolder(CustomViewHolder customViewHolder, int position)
    {

        customViewHolder.imgv_thumbnail.getLayoutParams().height = width/4;
        customViewHolder.imgv_thumbnail.getLayoutParams().width = width/4;



        this.helperS.setImageGlit(this.con, customViewHolder.imgv_thumbnail, (String) this.image_video_path_list.get(position));
//        customViewHolder.frame.setText(BuildConfig.FLAVOR + position);
    }

    public int getItemCount()
    {
        return this.image_video_path_list.size();
    }
}