package com.iconsolutions.menuhelper;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iconsolutions.crewschedular.CalendarFragment;
import com.iconsolutions.crewschedular.JobDetailsHomeFragment;
import com.iconsolutions.crewschedular.MainActivity;
import com.iconsolutions.crewschedular.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class JobMenuFragment extends Fragment implements View.OnClickListener {

    TextView home, pmFeedbackTab, controllerTab, jobImagesTab, lineItemsTab, planImageTab, poLineItemTab, mainLandingTab, notesTab;

    private View view;

    public JobMenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_job_menu, container, false);
        initUI();
        return view;
    }

    private void initUI() {
        home = (TextView)view.findViewById(R.id.home);
        mainLandingTab = (TextView) view.findViewById(R.id.main_landing);
        pmFeedbackTab = (TextView) view.findViewById(R.id.feedback);
        planImageTab = (TextView) view.findViewById(R.id.plan_image);
        poLineItemTab = (TextView) view.findViewById(R.id.po_line_item);
        lineItemsTab = (TextView) view.findViewById(R.id.line_item);
        jobImagesTab= (TextView) view.findViewById(R.id.job_image);
        controllerTab = (TextView) view.findViewById(R.id.controller);
        notesTab = (TextView) view.findViewById(R.id.notes);

        home.setOnClickListener(this);
        mainLandingTab.setOnClickListener(this);
        pmFeedbackTab.setOnClickListener(this);
        planImageTab.setOnClickListener(this);
        poLineItemTab.setOnClickListener(this);
        lineItemsTab.setOnClickListener(this);
        jobImagesTab.setOnClickListener(this);
        controllerTab.setOnClickListener(this);
        notesTab.setOnClickListener(this);

        selectTab(R.id.main_landing);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        JobDetailsHomeFragment wof = (JobDetailsHomeFragment) getFragmentManager().findFragmentById(R.id.content_frame);
        switch (v.getId())
        {
            case R.id.home:
                MainActivity mainActivity = (MainActivity) JobMenuFragment.this.getActivity();
                Fragment fragment  = new CalendarFragment();
                mainActivity.setSampleListFragment();
                if (fragment != null)
                    mainActivity.switchContent(fragment);
                break;
            case R.id.po_line_item:
                wof.nextAction(1);
                break;
            case R.id.line_item:
                wof.nextAction(2);
                break;
            case R.id.plan_image:
                wof.nextAction(3);
                break;
            case R.id.notes:
                wof.nextAction(4);
                break;
            case R.id.job_image:
                wof.nextAction(5);
                break;
            case R.id.controller:
                wof.nextAction(6);
                break;
            case R.id.feedback:
                wof.nextAction(7);
                break;

        }
        selectTab(v.getId());
    }
    public void selectTab(int id){
        mainLandingTab.setSelected(false);
        pmFeedbackTab.setSelected(false);
        planImageTab.setSelected(false);
        poLineItemTab.setSelected(false);
        lineItemsTab.setSelected(false);
        jobImagesTab.setSelected(false);
        controllerTab.setSelected(false);
        notesTab.setSelected(false);
        view.findViewById(id).setSelected(true);
    }
}
