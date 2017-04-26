package com.iconsolutions.menuhelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iconsolutions.crewschedular.CrewJobsListFragment;
import com.iconsolutions.crewschedular.R;

//import com.refractive.fragments.EventSelectionFragment;
//import com.refractive.fragments.SettingsFragment;

public class MenuListFragment extends Fragment implements View.OnClickListener {

    //	FragmentActivity fm;

    TextView todayJobs, previousJobs;
    TextView completedJobs, unCompletedJobs;

    View view;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.left_side_menu_list, null);
//		this.fm = getActivity();
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        todayJobs = (TextView) getActivity().findViewById(R.id.today_jobs);
        previousJobs = (TextView) getActivity().findViewById(R.id.previous_jobs);
        completedJobs = (TextView) getActivity().findViewById(R.id.completed_jobs);
        unCompletedJobs = (TextView) getActivity().findViewById(R.id.uncompleted_jobs);
        todayJobs.setSelected(true);
        previousJobs.setSelected(false);
        completedJobs.setSelected(false);
        unCompletedJobs.setSelected(false);

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
        CrewJobsListFragment cjlFrag = (CrewJobsListFragment)getFragmentManager().findFragmentById(R.id.content_frame);
        todayJobs.setSelected(false);
        previousJobs.setSelected(false);
        completedJobs.setSelected(false);
        unCompletedJobs.setSelected(false);
        switch (v.getId())
        {
            case R.id.today_jobs:
                cjlFrag.onTodayJob();
                break;
            case R.id.previous_jobs:
                cjlFrag.onPreviousJob();
                break;
            case R.id.completed_jobs:
                cjlFrag.onCompletedJob();
                break;
            case R.id.uncompleted_jobs:
                cjlFrag.onInCompletedJob();
                break;
        }
        view.findViewById(v.getId()).setSelected(true);
    }
}
