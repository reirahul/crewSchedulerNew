package com.iconsolutions.menuhelper;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iconsolutions.crewschedular.R;
import com.iconsolutions.crewschedular.WorkOrderFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class JobMenuFragment extends Fragment implements View.OnClickListener {

    TextView pmFeedbackTab,controllerTab, jobImagesTab, lineItemsTab, planImageTab,poLineItemTab;

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

        controllerTab = (TextView) view.findViewById(R.id.tab1_img);
        jobImagesTab = (TextView) view.findViewById(R.id.tab2_img);
        lineItemsTab = (TextView) view.findViewById(R.id.tab3_img);
        planImageTab = (TextView) view.findViewById(R.id.tab4_img);
        pmFeedbackTab = (TextView) view.findViewById(R.id.tab5_img);
        poLineItemTab = (TextView) view.findViewById(R.id.tab6_img);

        pmFeedbackTab.setSelected(false);
        controllerTab.setSelected(true);
        jobImagesTab.setSelected(false);
        lineItemsTab.setSelected(false);
        planImageTab.setSelected(false);
        poLineItemTab.setSelected(false);


        pmFeedbackTab.setOnClickListener(this);
        controllerTab.setOnClickListener(this);
        jobImagesTab.setOnClickListener(this);
        lineItemsTab.setOnClickListener(this);
        planImageTab.setOnClickListener(this);
        poLineItemTab.setOnClickListener(this);

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        WorkOrderFragment wof = (WorkOrderFragment) getFragmentManager().findFragmentById(R.id.content_frame);
        pmFeedbackTab.setSelected(false);
        controllerTab.setSelected(false);
        jobImagesTab.setSelected(false);
        lineItemsTab.setSelected(false);
        planImageTab.setSelected(false);
        poLineItemTab.setSelected(false);
        switch (v.getId())
        {
            case R.id.tab1_img:
                wof.nextAction(1);
                break;
            case R.id.tab2_img:
                wof.nextAction(2);
                break;
            case R.id.tab3_img:
                wof.nextAction(3);
                break;
            case R.id.tab4_img:
                wof.nextAction(4);
                break;
            case R.id.tab5_img:
                wof.nextAction(5);
                break;
            case R.id.tab6_img:
                wof.nextAction(6);
                break;

        }
        v.setSelected(true);
    }
}
