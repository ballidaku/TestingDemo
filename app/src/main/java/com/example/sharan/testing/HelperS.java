package com.example.sharan.testing;

/**
 Created by sharan on 7/4/16. */

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class HelperS
{
    public  ArrayList<String> getAllShownImagesPath(Activity activity)
    {
        ArrayList<String> listOfAllImages          = new ArrayList();
        Cursor            cursor                   = activity.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{"_data", "bucket_display_name"}, null, null, null);
        int               column_index_data        = cursor.getColumnIndexOrThrow("_data");
        int               column_index_folder_name = cursor.getColumnIndexOrThrow("bucket_display_name");
        while (cursor.moveToNext())
        {
            listOfAllImages.add(cursor.getString(column_index_data));
        }
        return listOfAllImages;
    }

    public void setImageGlit(Context con, ImageView imageView, String url)
    {
        Glide.with(con).load(new File(url)).centerCrop().override(180, 180).into(imageView);
    }

    public void setImageGlit2(Context con, ImageView imageView, String url)
    {
        Glide.with(con).load(new File(url)).centerCrop().into(imageView);
    }





    public static boolean checkFolder(File filepath, String string)
    {
        boolean checkf = false;
        try
        {
            File dbfile = new File(filepath.getAbsolutePath() + "/" + string + "/");
            checkf = dbfile.exists();
            Log.e("info", "Folder  exist");
        }
        catch (Exception e)
        {
            Log.e("info", "Folder doesn't exist");
            e.printStackTrace();
        }
        return checkf;
    }



}