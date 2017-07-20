package com.iconsolutions.jobfragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.iconsolutions.crewschedular.JobDetailsHomeFragment;
import com.iconsolutions.crewschedular.MainActivity;
import com.iconsolutions.crewschedular.R;
import com.iconsolutions.helper.IMyActivity;
import com.iconsolutions.helper.UserPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import crewschedular.fragmentinterface.BaseBackPressedListener;
import rolustech.beans.Field;
import rolustech.beans.ModuleConfig;
import rolustech.beans.SugarBean;

import static com.iconsolutions.crewschedular.R.id.controller_layout;
//import java.lang.reflect.Field;

/**
 * Created by kashif on 4/8/16.
 */
public class ControllerFragment extends Fragment {
    View view;
    FragmentActivity fm;
    String salesOrderId;
    ProgressDialog p_bar;
    android.os.Handler handler = new android.os.Handler();
    SugarBean orders[] = null;
    SugarBean workOrder;
    Button nextButton,previousButton;
    String controllerSaveValue = new String();
    ArrayList<ArrayList<String[]>> records = null;
    Boolean updated = false;
    IMyActivity mainActivity;

    public ControllerFragment(){
        this.fm = this.getActivity();
    }

    public View onCreateView(LayoutInflater inflater,ViewGroup v,Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_controller, null);


        JobDetailsHomeFragment parentFragment = (JobDetailsHomeFragment) getParentFragment();
        String modName = "ro_crew_work_order";
           workOrder = parentFragment.getWorkOrder();
           Log.e("ControllerFragment","Crew_App Workorder -> "+workOrder.getNameArray(true));
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
                if(updated) {
                    workOrder.updateFieldValue("controller_multi_select", controllerSaveValue);
                    saveControllerValues(workOrder);
                }
                else
                    moveToTabView(2);
            }
        });

        UserPreferences.reLoadPrefernces(this.getContext());
        ModuleConfig config = UserPreferences.moduleConfiguration.get("ro_crew_work_order");
        Hashtable<String, Field> fields = config.fields;

        if(fields != null && fields.size() > 0) {

            Field field = fields.get("controller_multi_select");

            String[][] options = field.options;

            final String[] names = options[0];
            final String[] values = options[1];

            if (workOrder != null) {
                final String controllerValue = workOrder.getFieldValue("controller_multi_select");
                String[] parts = controllerValue.split("\\^");

                int counter = 0;

                LinearLayout mainLayout = (LinearLayout) view.findViewById(controller_layout);
                int size = names.length / 3 + names.length % 3;
                for (int i = 0; i < size; i++) {
                    RelativeLayout layout2 = new RelativeLayout(this.getContext());
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
                    );
//            params.setMargins(0, 50, 0, 50);
                    layout2.setLayoutParams(params);
                    layout2.setPadding(50, 10, 50, 10);
//            layout2.setOrientation(LinearLayout.HORIZONTAL);

                    for (int j = 0; j < 3; j++) {

                        if (counter >= names.length)
                            break;

                        String fieldName = names[counter];

                        final CheckBox checkBox = new CheckBox(this.getContext());
                        checkBox.setText(fieldName);
                        checkBox.setTextColor(Color.GRAY);
                        checkBox.setTextSize(18);
                        checkBox.setTag(counter);
                        checkBox.setWidth(300);
//                checkBox.setSingleLine();
                        RelativeLayout.LayoutParams checkbox_relativeParams = null;
                        if (j == 0) {
                            checkbox_relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            checkbox_relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                    checkBox.setPadding(50, 0, 0, 0);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                checkBox.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                            }
                        } else if (j == 1) {
                            checkbox_relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            checkbox_relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        } else if (j == 2) {
                            checkbox_relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            checkbox_relativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                    checkBox.setPadding(0, 0, 50, 0);
                        }

                        if (Arrays.asList(parts).contains(fieldName)) {
                            checkBox.setChecked(true);
                        }

                        checkBox.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updated = true;
                                String selected = values[(int) checkBox.getTag()];
                                if (checkBox.isChecked()) {
                                    if (controllerSaveValue.length() == 0)
                                        controllerSaveValue = "^" + selected + "^";
                                    else
                                        controllerSaveValue = controllerSaveValue +",^"+ selected + "^";
                                } else {
                                    String str = controllerSaveValue.replace(selected, "");
                                    String str1 = str.replace("^^", "^");
                                    controllerSaveValue = str1;
                                }
                            }

                        });

                        layout2.addView(checkBox, checkbox_relativeParams);
                        counter = counter + 1;
                    }
                    mainLayout.addView(layout2);
                }

            }
        }
    }

    public void saveControllerValues(SugarBean wo){
        p_bar = ProgressDialog.show(fm, getResources().getString(R.string.app_name), "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);

        final SugarBean bean = wo;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String id = bean.save(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing()) {
                            p_bar.hide();
                        }
                        updated = false;
                        moveToTabView(2);
                    }
                });
            }
        }).start();

    }

    public void moveToTabView(int tabNumber){
        JobDetailsHomeFragment parentFragment = (JobDetailsHomeFragment) this.getParentFragment();
        parentFragment.nextAction(tabNumber);
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
