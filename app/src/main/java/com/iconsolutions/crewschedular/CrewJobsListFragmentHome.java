package com.iconsolutions.crewschedular;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.iconsolutions.crewJobsPage.CrewJobsListFragmentComplited;
import com.iconsolutions.crewJobsPage.CrewJobsListFragmentInComplited;
import com.iconsolutions.crewJobsPage.CrewJobsListFragmentPrevious;
import com.iconsolutions.crewJobsPage.CrewJobsListFragmentToday;
import com.iconsolutions.helper.VerticalViewPager;
import com.iconsolutions.menuhelper.MenuListFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.text.SimpleDateFormat;

import rolustech.beans.SugarBean;

/**
 * Created by kashif on 3/22/16.
 */
public class CrewJobsListFragmentHome extends Fragment implements View.OnClickListener {
    View view;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    VerticalViewPager mViewPager;
    private View focus;

    private SlidingMenu leftMenu;

    private LinearLayout left_menu_btn;
    private MenuListFragment menuListFragment;
    private SugarBean bean;

    public CrewJobsListFragmentHome() {
    }
    public static Fragment newInstance(int position, String s) {
        return new CrewJobsListFragmentHome();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup v, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.job_list_pager, null);

        bean = new SugarBean(getActivity(), "ro_crew_work_order");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (VerticalViewPager)view.findViewById(R.id.pager);
 //       mViewPager.setPageTransformer(true, new DepthPageTransformer());

        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setCurrentItem(0);

//        mViewPager.setOffscreenPageLimit(4);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position)
                {
                    case 0:
                        ((MenuListFragment) getFragmentManager().findFragmentById(R.id.left_menu_frame)).selectTab(R.id.today_jobs);
                        break;
                    case 1:
                        ((MenuListFragment) getFragmentManager().findFragmentById(R.id.left_menu_frame)).selectTab(R.id.previous_jobs);
                        break;
                    case 2:
                        ((MenuListFragment) getFragmentManager().findFragmentById(R.id.left_menu_frame)).selectTab(R.id.completed_jobs);
                        break;
                    case 3:
                        ((MenuListFragment) getFragmentManager().findFragmentById(R.id.left_menu_frame)).selectTab(R.id.uncompleted_jobs);
                        break;

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        left_menu_btn = (LinearLayout)getActivity().findViewById(R.id.left_menu_btn);
        left_menu_btn.setOnClickListener(this);
        
        focus = getActivity().getCurrentFocus();
        setLeftSlideMenu();
        toggleLeftSlideMenu();
        return view;

    }

    private void setLeftSlideMenu() {
//        getActionBar().hide();
        leftMenu = new SlidingMenu(getActivity());
        leftMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
//      leftMenu.setShadowWidthRes(R.dimen.bottom_margin);
//		menu.setShadowDrawable(R.drawable.shadow);
//      leftMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset_left);
        leftMenu.setBehindWidth(110);
        leftMenu.setFadeDegree(0.35f);
        leftMenu.attachToActivity(getActivity(), SlidingMenu.SLIDING_CONTENT);
        leftMenu.setMenu(R.layout.left_menu_frame);
        leftMenu.setMode(SlidingMenu.LEFT);
        setLeftMenuListFragment();
    }
    public void setLeftMenuListFragment() {
        menuListFragment = new MenuListFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.left_menu_frame, menuListFragment)
                .commit();
    }

    private void toggleLeftSlideMenu() {
        if (leftMenu.isMenuShowing()) {
            leftMenu.showContent();
        } else {
            leftMenu.showMenu();
        }
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_menu_btn:
                if (focus != null) {
                    hiddenKeyboard(v);
                }
                toggleLeftSlideMenu();
                break;
        }
    }

    private void hiddenKeyboard(View v) {
        InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void nextAction(int position)
    {
        mViewPager.setCurrentItem(position,true);
    }
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            long currentDate = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateString = sdf.format(currentDate);
            String userWhere = "";
            switch (position)
            {
                case 0:
                       return new CrewJobsListFragmentToday();
                case 1:
                       return new CrewJobsListFragmentPrevious();
                case 2:
                       return new CrewJobsListFragmentComplited();
                case 3:
                       return new CrewJobsListFragmentInComplited();
                default: return null;
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (leftMenu.isMenuShowing()) {
            leftMenu.showContent();
        }
    }
}


