package com.iconsolutions.crewschedular;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.iconsolutions.adapter.JobImagesAdapter;
import com.iconsolutions.helper.UserPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import crewschedular.fragmentinterface.BaseBackPressedListener;
import rolustech.beans.Field;
import rolustech.beans.ModuleConfig;
import rolustech.beans.SugarBean;
import rolustech.communication.soap.SOAPClient;
import rolustech.helper.NetworkHelper;

import static com.iconsolutions.helper.UserPreferences.imageVarify;


/**
 * A simple {@link Fragment} subclass.
 */
public class PMFeedbackFragment extends Fragment {

    View view;
    FragmentActivity fm;
    String salesOrderId;
    ProgressDialog p_bar;
    android.os.Handler handler = new android.os.Handler();
    SugarBean orders[] = null;
    SugarBean workOrder;
    Button nextButton;
    String controllerSaveValue = new String();
    ArrayList<ArrayList<String[]>> records = null;
    Boolean updated = false;

    public PMFeedbackFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_pmfeedback, container, false);
        WorkOrderFragment parentFragment = (WorkOrderFragment) this.getParentFragment();
        String modName = "ro_crew_work_order";
        if(NetworkHelper.isAvailable(fm)) {
            if (records == null) {
//                records = parentFragment.getWorkOrder();
//                if (records != null && records.size() > 0) {
//                    orders = new SugarBean[records.size()];
//
//                    for (int i = 0; i < records.size(); i++) {
//                        ArrayList<String[]> record = records.get(i);
//                        orders[i] = new SugarBean(fm, modName);
//                        for (int j = 0; j < record.size(); j++) {
//                            String name = record.get(j)[0];
//
//                            if (record.get(j)[1] != null) {
//                                orders[i].setFieldValue(name, record.get(j)[1]);
//                            }
//                        }
//
//                    }
//
//                    workOrder = orders[0];
//                }
//            }
                workOrder = parentFragment.getWorkOrder();
            }
        }
        else{
            workOrder = parentFragment.getWorkOrder();
        }

        initUI();

        return view;
    }

    private void initUI() {

        if (workOrder != null && controllerSaveValue.length() == 0) {
            controllerSaveValue = workOrder.getFieldValue("controller_multi_select");
        }

        nextButton = (Button) view.findViewById(R.id.controller_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNextView();
            }
        });

        UserPreferences.reLoadPrefernces(this.getContext());
        ModuleConfig config = UserPreferences.moduleConfiguration.get("ro_crew_work_line_items");
        Hashtable<String, Field> fields = config.fields;
        Log.d("Crew_App"," Length of Fields => "+ fields.size()) ;
    }



     public void moveToNextView(){
        WorkOrderFragment parentFragment = (WorkOrderFragment) this.getParentFragment();
        parentFragment.nextAction(2);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.fm = getActivity();
        if(this.fm != null)
        {
            ((MainActivity) this.fm).setOnBackPressedListener(new BaseBackPressedListener(this.fm));
        }
    }

    @Override
    public void onResume() {
        MainActivity mainActivity = (MainActivity) this.fm;

        super.onResume();
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    @Override
    public void onDetach() {
        super.onDetach();

        try {
            java.lang.reflect.Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
