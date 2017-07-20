package com.iconsolutions.jobfragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iconsolutions.crewschedular.JobDetailsHomeFragment;
import com.iconsolutions.crewschedular.R;
import com.iconsolutions.helper.UserPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import rolustech.beans.SugarBean;
import rolustech.communication.soap.SOAPClient;
import rolustech.helper.AlertHelper;
import rolustech.helper.NetworkHelper;

/**
 * Created by kashif on 3/30/16.
 */
public class WorkOrderFragment extends Fragment{

    View view;
    FragmentActivity fm;

    ArrayList<SugarBean> workOrder;
    String jobID;
    ProgressDialog p_bar;
    android.os.Handler handler = new android.os.Handler();
    ArrayList<SugarBean> lItems = null;
    LinearLayout ll;
    TextView job_Name,workOrderNo,workLocation, notes,timer,manualy_time,requestHrsButton;

    SwitchCompat startTimer;

    EditText requestHours;
    ImageView mapsButton;

    private long startTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    boolean start = true;

    public WorkOrderFragment() {
        this.fm = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup v, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_workorder, null);
        initUI();
        return view;
    }

    private void initUI() {

//        headerView = (RelativeLayout) view.findViewById(R.id.header_card);
//        purchageOrder = (RelativeLayout) view.findViewById(R.id.purchase_orders);
//        pOItems = (RelativeLayout) view.findViewById(R.id.po_line_items);
//        workOrderNo = (TextView) view.findViewById(R.id.work_order);
        timer = (TextView) view.findViewById(R.id.timer);
//        status = (TextView) view.findViewById(R.id.status);
//        startDate = (TextView) view.findViewById(R.id.start_date);
//        endDate = (TextView) view.findViewById(R.id.end_date);
        requestHours = (EditText) view.findViewById(R.id.additional_hrs_text);
//        job_Name = (TextView) view.findViewById(R.id.job_name);

        manualy_time = (TextView) view.findViewById(R.id.manualy_time);
        workLocation = (TextView) view.findViewById(R.id.work_location);
        notes = (TextView) view.findViewById(R.id.notes);
        notes.setEnabled(false);

        startTimer = (SwitchCompat) view.findViewById(R.id.toggle);

        /// Detect touched area

        requestHours.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestHours.getText().toString().equals("0"))
                    requestHours.setText("");
            }
        });

        requestHrsButton = (TextView) view.findViewById(R.id.request_hrs_button);
        requestHrsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog diag = new android.app.AlertDialog.Builder(getActivity()).create();
                diag.setCancelable(true);
                diag.setMessage("Are you sure to request for additional hours?");
                diag.setTitle(UserPreferences.APP_NAME);
                diag.setInverseBackgroundForced(true);
                diag.setButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestHours();
                        dialog.dismiss();
                    }
                });
                diag.setButton2("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                diag.show();
            }
        });

        mapsButton = (ImageView) view.findViewById(R.id.maps_button);
        mapsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                MainActivity mainActivity = (MainActivity) getActivity();
//                Fragment fragment = new FullMap();
//                Bundle args = new Bundle();
//                args.putString("WorkLocation", workLocation.getText().toString());
//                fragment.setArguments(args);
//                mainActivity.addContent(fragment);

                if (workLocation != null && workLocation.getText().toString().length() > 0) {
                    String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%s", workLocation.getText().toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                }
            }
        });

        getWorkOrder();

        manualy_time.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
// ...Irrelevant code for customizing the buttons and title
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.timer_dialog, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
                TextView submit = (TextView)dialogView.findViewById(R.id.submit);
                submit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(),"Successfully Submit Data",Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();

                    }
                });

                Toast.makeText(getContext(),"Dialog Successfully Open",Toast.LENGTH_LONG).show();
            }
        });

        startTimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    start=!isChecked;
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    startTimer.setBackgroundResource(R.drawable.stop_toggle_style);
                    startTimer.setText("STOP");

                }
                else
                {
                    start= !isChecked;
                    timeSwapBuff += timeInMilliseconds;
                    customHandler.removeCallbacks(updateTimerThread);
                    startTimer.setBackgroundResource(R.drawable.start_toggle_style);
                    startTimer.setText("START");

                }

            }
        });

    }
    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
             updatedTime = timeSwapBuff + timeInMilliseconds;
             int secs = (int) (updatedTime / 1000);
             int mins = secs / 60;
                 secs = secs % 60;
             int hour = mins / 60;
             int milliseconds = (int) (updatedTime % 1000);

           timer.setText("" +String.format("%02d",hour) + " : "+ String.format("%02d",mins) + " : "+ String.format("%02d", secs));

            Calendar c = Calendar.getInstance();

//            timer.setText("" + String.format("%02d",c.get(Calendar.HOUR)) + " : "+ String.format("%02d",c.get(Calendar.MINUTE)) + " : "+String.format("%02d",c.get(Calendar.SECOND)));
            customHandler.postDelayed(this, 0);
        }

    };


    public void requestHours() {
        p_bar = ProgressDialog.show(this.fm, getResources().getString(R.string.app_name), "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);
        final SugarBean wOrder = workOrder.get(0);
        final String hours = requestHours.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Boolean success = false;
                    if (NetworkHelper.isAvailable(fm)) {
                        SOAPClient com = new SOAPClient(UserPreferences.url);
                        String response = com.setValueEntry("ro_crew_work_order", hours, wOrder.getFieldValue("id"));
                        if (response != "-1") {
                            success = true;
                            wOrder.updateFieldValue("additional_hrs", hours);
                            SugarBean wo_bean = new SugarBean(fm, "ro_crew_work_order");
                            wo_bean.loadCom(fm, false, true);
                            wo_bean.updateFieldValue("id", wOrder.getFieldValue("id"));
                            wo_bean.updateFieldValue("additional_hrs", hours);
                            wo_bean.save(false);
                        } else {
                        }
                    } else {
                        SugarBean wo_bean = new SugarBean(fm, "ro_crew_work_order");
                        wo_bean.updateFieldValue("id", wOrder.getFieldValue("id"));
                        wo_bean.updateFieldValue("additional_hrs", hours);
                        wOrder.updateFieldValue("additional_hrs", hours);
                        wo_bean.save(false);

                        success = true;

                        if (UserPreferences.WorkOrderHoursRequest == null)
                            UserPreferences.WorkOrderHoursRequest = new HashMap<String, String>();

                        UserPreferences.WorkOrderHoursRequest.put(wOrder.getFieldValue("id"), hours);
                        UserPreferences.save(fm);
                    }

                } catch (Exception e) {
//                    Log.d("ERROR", e.getMessage());
//                    AlertHelper.showAlert(fm, "Error", e.getMessage());
                    wOrder.updateFieldValue("additional_hrs", hours);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing()) {
                            p_bar.hide();
                        }
                        Toast.makeText(fm, "Successfully Requested", Toast.LENGTH_SHORT).show();

                        populateView();
                    }
                });

            }
        }).start();
    }

//    public ArrayList<ArrayList<String[]>> getWorkOrder(){
//        if(listRecords != null && listRecords.size() > 0) {
//            ArrayList<ArrayList<String[]>> records = (ArrayList<ArrayList<String[]>>) listRecords.get(1);
//            return records;
//        }
//
//        return null;
//    }


    public void getWorkOrder() {
        JobDetailsHomeFragment parentFragment = new JobDetailsHomeFragment();
        workOrder = new ArrayList<>();
        workOrder.add(parentFragment.getWorkOrder());
        populateView();
    }
/*
    private void switchTabContent(Fragment fragment) {
        if (fragment != null) {
            this.getChildFragmentManager().beginTransaction()
                    .replace(R.id.tab_layout, fragment, "MY_FRAGMENT").commit();
        }
    }
*/
    public void populateView() {

        SugarBean wOrder = null;
        SugarBean lItems = null;

        if (workOrder != null && workOrder.size() > 0) {
            wOrder = workOrder.get(0);
            AlertHelper.printBeans(wOrder);
            jobID = wOrder.getFieldValue("rt_jobs_id");
//            job_Name.setText(wOrder.getFieldValue("name"));
//            workOrderNo.setText(wOrder.getFieldValue("number"));
            requestHours.setText(wOrder.getFieldValue("additional_hrs"));
            notes.setText(wOrder.getFieldValue("description"));
//            status.setText(wOrder.getFieldValue("status"));
//            endDate.setText(wOrder.getFieldValue("date_closed"));
//            startDate.setText(wOrder.getFieldValue("date_start"));
//            status.setText(wOrder.getFieldValue("status"));
//           orderType.setText(salesOrderType);
            if (wOrder.getFieldValue("work_location").length() > 0)
                workLocation.setText(wOrder.getFieldValue("work_location"));
            else
                workLocation.setText(wOrder.getFieldValue("name"));

        }
         //       nextAction(1);

    }
/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                Fragment fragment = getChildFragmentManager().findFragmentById(R.id.tab_layout);
                if (fragment instanceof JobImagesFragment) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
                break;
            case 100:
                Fragment fragment1 = getChildFragmentManager().findFragmentById(R.id.tab_layout);
                if (fragment1 instanceof JobImagesFragment) {
                    fragment1.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }
    }
*/
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



