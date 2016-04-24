package com.example.sharan.testing;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.sharan.testing.fragments.GalleryFragment;
import com.example.sharan.testing.fragments.PhotoFragment;
import com.example.sharan.testing.fragments.VideoFragment;
import com.example.sharan.testing.fragments.ideoFragment2;

import java.util.ArrayList;
import java.util.List;


public class GIV extends AppCompatActivity
{

//    private Toolbar toolbar;
    private TabLayout tabLayout;
    private CustomViewPager viewPager;
    ideoFragment2 videoFragment;

    public static String whichFragment="";



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giv);


      /*  toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (CustomViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment(), "Gallery");
        adapter.addFragment(videoFragment=new ideoFragment2(), "Video");
//        adapter.addFragment(new VideoFragment(), "Video");
        adapter.addFragment(new PhotoFragment(), "Photo");



        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager)
        {
            super(manager);
        }

        @Override
        public Fragment getItem(int position)
        {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount()
        {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title)
        {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return mFragmentTitleList.get(position);
        }
    }


    public void halt_view_pager(boolean b)
    {
        viewPager.setPagingEnabled(b);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();


        if(whichFragment.equals("VideoFragment"))
        {
//            videoFragment.finish_on_spot();
        }

        finish();


    }
}
