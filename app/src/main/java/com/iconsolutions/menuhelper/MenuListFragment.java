package com.iconsolutions.menuhelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iconsolutions.crewschedular.CalendarFragment;
import com.iconsolutions.crewschedular.CrewJobsListFragmentHome;
import com.iconsolutions.crewschedular.FullMap;
import com.iconsolutions.crewschedular.MainActivity;
import com.iconsolutions.crewschedular.R;

//import com.refractive.fragments.EventSelectionFragment;
//import com.refractive.fragments.SettingsFragment;

public class MenuListFragment extends Fragment implements View.OnClickListener {

    //	FragmentActivity fm;

    TextView home, todayJobs, previousJobs, completedJobs, unCompletedJobs;
    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		this.fm = view;
        view =  inflater.inflate(R.layout.left_side_menu_list, null);
         init();
         return view;
    }

    public void init(){
        home = (TextView) view.findViewById(R.id.home);
        todayJobs = (TextView) view.findViewById(R.id.today_jobs);
        previousJobs = (TextView) view.findViewById(R.id.previous_jobs);
        completedJobs = (TextView) view.findViewById(R.id.completed_jobs);
        unCompletedJobs = (TextView) view.findViewById(R.id.uncompleted_jobs);

        todayJobs.setSelected(true);
        previousJobs.setSelected(false);
        completedJobs.setSelected(false);
        unCompletedJobs.setSelected(false);

        home.setOnClickListener(this);
        todayJobs.setOnClickListener(this);
        previousJobs.setOnClickListener(this);
        completedJobs.setOnClickListener(this);
        unCompletedJobs.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Fragment frag = getFragmentManager().findFragmentById(R.id.content_frame);
        if (frag instanceof CrewJobsListFragmentHome) {
            CrewJobsListFragmentHome cjlFrag = (CrewJobsListFragmentHome) frag;
            switch (v.getId()) {
                case R.id.home:
                    MainActivity mainActivity = (MainActivity) MenuListFragment.this.getActivity();
                    Fragment fragment  = new CalendarFragment();
                    mainActivity.setSampleListFragment();
                    if (fragment != null)
                        mainActivity.switchContent(fragment);
                    break;
                case R.id.today_jobs:
                    cjlFrag.nextAction(0);
                    break;
                case R.id.previous_jobs:
                    cjlFrag.nextAction(1);
                    break;
                case R.id.completed_jobs:
                    cjlFrag.nextAction(2);
                    break;
                case R.id.uncompleted_jobs:
                    cjlFrag.nextAction(3);
                    break;
            }
            selectTab(v.getId());
        }
        else if (frag instanceof CalendarFragment || frag instanceof FullMap) {
            CalendarFragment clFrag = (CalendarFragment) frag;
            switch (v.getId()) {
                case R.id.today_jobs:
                    clFrag.TodayJob();
                    break;
                case R.id.previous_jobs:
                    clFrag.PreviousJob();
                    break;
                case R.id.completed_jobs:
                    clFrag.CompletedJob();
                    break;
                case R.id.uncompleted_jobs:
                    clFrag.InCompletedJob();
                    break;
            }
            selectTab(v.getId());
        }
    }
    public void selectTab(int id){
        todayJobs.setSelected(false);
        previousJobs.setSelected(false);
        completedJobs.setSelected(false);
        unCompletedJobs.setSelected(false);
        view.findViewById(id).setSelected(true);
    }
}
