package com.example.sharan.testing.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.sharan.testing.GIV;
import com.example.sharan.testing.HelperS;
import com.example.sharan.testing.Online_Sale_Recycle_Adapter;
import com.example.sharan.testing.R;
import com.example.sharan.testing.RecyclerItemClickListener;
import com.naver.android.helloyako.imagecrop.view.ImageCropView;

import java.util.ArrayList;

public class GalleryFragment extends Fragment
{

    AppBarLayout               app_bar_layout;
    CoordinatorLayout.Behavior behavior;
    CollapsingToolbarLayout    collapsingToolbar;
    Context                    con;
    CoordinatorLayout          coordinatorLayout;
    HelperS           helperS               = new HelperS();
    ArrayList<String> image_video_path_list = new ArrayList();

    //    ImageView imgv_product_banner;
    //    FrameLayout               lay_font;
    RecyclerView recycler_grid_view;
    Toolbar      toolbar;

    ImageCropView imageCropView;
    //    AppBarLayout.LayoutParams p;

    float y1, y2;

    public GalleryFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.con = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_gallery, container, false);

        setToolbar(v);
        setUpIDS(v);

        return v;
    }

    void setToolbar(View v)
    {
        this.toolbar = (Toolbar) v.findViewById(R.id.toolbar_col);
        ((AppCompatActivity) getActivity()).setSupportActionBar(this.toolbar);
        this.toolbar.setNavigationIcon((int) R.mipmap.ic_launcher);

    }

    public void setUpIDS(View v)
    {

        imageCropView = (ImageCropView) v.findViewById(R.id.image);

        imageCropView.setAspectRatio(1, 1);
        //        this.lay_font = (FrameLayout)v. findViewById(R.id.lay_font);
        this.coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.coordinatorLayout);
        this.app_bar_layout = (AppBarLayout) v.findViewById(R.id.app_bar_layout);
        this.collapsingToolbar = (CollapsingToolbarLayout) v.findViewById(R.id.collapsing_toolbar);
        this.recycler_grid_view = (RecyclerView) v.findViewById(R.id.recycler_grid_view);
        //        this.imgv_product_banner = (ImageView) v.findViewById(R.id.imgv_product_banner);

        //        p = (AppBarLayout.LayoutParams) collapsingToolbar.getLayoutParams();

        this.recycler_grid_view.setLayoutManager(new GridLayoutManager(recycler_grid_view.getContext(), 4));

        this.image_video_path_list = helperS.getAllShownImagesPath(getActivity());

        this.recycler_grid_view.setAdapter(new Online_Sale_Recycle_Adapter(recycler_grid_view.getContext(), this.image_video_path_list));

        recycler_grid_view.addOnItemTouchListener(new RecyclerItemClickListener(getActivity().getApplicationContext(), recycler_grid_view, new RecyclerItemClickListener.OnItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {

                imageCropView.setImageFilePath(image_video_path_list.get(position));

                //                helperS.setImageGlit2(con, imgv_product_banner, image_video_path_list.get(position));
            }

            @Override
            public void onItemLongClick(View view, int position)
            {
                // ...
            }
        }));

        imageCropView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                Log.e("hello", "imageCropViewTouch");

                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    ((GIV) getActivity()).halt_view_pager(false);
//                    disable_scroll_toolbar();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    ((GIV) getActivity()).halt_view_pager(true);
                }
                return false;
            }
        });

     /*   recycler_grid_view.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {

                return false;
            }
        });*/

        toolbar.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {

                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    y1 = event.getY();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    y2 = event.getY();

                    //                    Log.e("Y1", "" + y1);
                    //                    Log.e("Y2", "" + y2);

                    if (y2 > y1)
                    {
                        expandToolbar();
                    }

                }

                return false;
            }
        });
    }

    public void disable_scroll_toolbar()
    {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbar.getLayoutParams();
        params.setScrollFlags(0);  // clear all scroll flags
    }

    public void collapseToolbar()
    {
        this.behavior = ((CoordinatorLayout.LayoutParams) this.app_bar_layout.getLayoutParams()).getBehavior();
        if (this.behavior != null)
        {
            this.behavior.onNestedFling(this.coordinatorLayout, this.app_bar_layout, null, 0.0f, 10000.0f, true);
        }
    }

    public void expandToolbar()
    {
        this.behavior = ((CoordinatorLayout.LayoutParams) this.app_bar_layout.getLayoutParams()).getBehavior();
        if (this.behavior != null)
        {
            this.behavior.onNestedFling(this.coordinatorLayout, this.app_bar_layout, null, 0.0f, -10000.0f, false);
        }
    }

    public void enable_scroll(boolean b)
    {
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) this.app_bar_layout.getLayoutParams();
        p.setScrollFlags(b ? AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED | AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL : AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        this.app_bar_layout.setLayoutParams(p);
    }

}
