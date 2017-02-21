package com.iconsolutions.crewschedular;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iconsolutions.helper.ViewAnimationUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import crewschedular.fragmentinterface.BaseBackPressedListener;
import rolustech.beans.SugarBean;
import rolustech.beans.UserPreferences;
import rolustech.communication.soap.SOAPClient;
import rolustech.helper.AlertHelper;
import rolustech.helper.NetworkHelper;

/**
 * Created by kashif on 3/30/16.
 */
public class WorkOrderFragment extends Fragment {

    View view;
    FragmentActivity fm;
    String title = "Work Order Update";
    SugarBean workOrder[] = null, lineItems[] = null;
    String salesOrderId, salesOrderNumber, salesOrderType, contractor, jobID, jobName;
    ProgressDialog p_bar;
    android.os.Handler handler = new android.os.Handler();
    ArrayList<SugarBean> lItems = null;
    RelativeLayout headerView,purchageOrder,pOItems;
    LinearLayout ll;
    TextView job_Name,workOrderNo,workLocation, notes,timer,manualy_time,more_text;
    EditText requestHours;
    ImageView controllerTab, jobImagesTab, lineItemsTab, planImageTab, mapsButton, requestHrsButton,startTimer,more_image;
    ArrayList listRecords;

    Fragment Cfragment, IFragment, Pfragment, LIFragment;
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

        Cfragment = new ControllerFragment();
        IFragment = new JobImagesFragment();
        LIFragment = new LineItemsFragment();
        Pfragment = new PlanImageFragment();

        MainActivity mainActivity = (MainActivity) this.fm;
        mainActivity.setTitle(title + " - " + getArguments().getString("JobName"));

        headerView = (RelativeLayout) view.findViewById(R.id.header_card);
//        purchageOrder = (RelativeLayout) view.findViewById(R.id.purchase_orders);
//        pOItems = (RelativeLayout) view.findViewById(R.id.po_line_items);
        workOrderNo = (TextView) view.findViewById(R.id.work_order);
        timer = (TextView) view.findViewById(R.id.timer);
//        status = (TextView) view.findViewById(R.id.status);
//        startDate = (TextView) view.findViewById(R.id.start_date);
//        endDate = (TextView) view.findViewById(R.id.end_date);
        requestHours = (EditText) view.findViewById(R.id.additional_hrs_text);
        job_Name = (TextView) view.findViewById(R.id.job_name);
        more_text = (TextView) view.findViewById(R.id.detils_text);
        manualy_time = (TextView) view.findViewById(R.id.manualy_time);
        workLocation = (TextView) view.findViewById(R.id.work_location);
        notes = (TextView) view.findViewById(R.id.notes);
        notes.setEnabled(false);
        controllerTab = (ImageView) view.findViewById(R.id.tab1_img);
        jobImagesTab = (ImageView) view.findViewById(R.id.tab2_img);
        startTimer = (ImageView) view.findViewById(R.id.start_button);
        lineItemsTab = (ImageView) view.findViewById(R.id.tab3_img);
        planImageTab = (ImageView) view.findViewById(R.id.tab4_img);
        more_image = (ImageView) view.findViewById(R.id.detils_image);
        ll=(LinearLayout)view.findViewById(R.id.more_item);
        OnClickListener click=new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ll.getVisibility()!=View.VISIBLE)
                {
                    ll.setVisibility(View.VISIBLE);
                    more_image.setImageResource(R.drawable.drop_list_btn);
                    more_text.setText("Less");
                }
                else {
                    ll.setVisibility(View.GONE);
                    more_image.setImageResource(R.drawable.up_drop_list_btn);
                    more_text.setText("More Details");
                }
            }
        };
        more_image.setOnClickListener(click);
        more_text.setOnClickListener(click);
        requestHours.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestHours.getText().toString().equals("0"))
                    requestHours.setText("");
            }
        });

        requestHrsButton = (ImageView) view.findViewById(R.id.request_hrs_button);
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

        controllerTab.setSelected(true);
        jobImagesTab.setSelected(false);
        lineItemsTab.setSelected(false);
        planImageTab.setSelected(false);

//        ViewAnimationUtils.expand(headerView);

        salesOrderId = getArguments().getString("SalesOrderId");
        salesOrderNumber = getArguments().getString("SalesOrderNumber");
        salesOrderType = getArguments().getString("SalesOrderType");
        contractor = getArguments().getString("Contractor");
//        jobID = getArguments().getString("JobID");
        jobID = "";
        jobName = getArguments().getString("JobName");

        createWorkOrderAndLineItems();

        controllerTab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Cfragment != null && !Cfragment.isVisible()) {
                    ViewAnimationUtils.expand(headerView);

                    controllerTab.setSelected(true);
                    jobImagesTab.setSelected(false);
                    lineItemsTab.setSelected(false);
                    planImageTab.setSelected(false);
                    switchTabContent(Cfragment);
                }
            }
        });

        jobImagesTab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IFragment != null && !IFragment.isVisible()) {
                    ViewAnimationUtils.collapse(headerView);

                    controllerTab.setSelected(false);
                    jobImagesTab.setSelected(true);
                    lineItemsTab.setSelected(false);
                    planImageTab.setSelected(false);
                    Bundle args = new Bundle();
                    args.putString("WorkOrderId", workOrder[0].getFieldValue("id"));
                    args.putString("WorkOrderName", workOrder[0].getFieldValue("name"));
                    IFragment.setArguments(args);
                    switchTabContent(IFragment);
                }
            }
        });

        lineItemsTab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LIFragment != null && !LIFragment.isVisible()) {
                    ViewAnimationUtils.collapse(headerView);

                    controllerTab.setSelected(false);
                    jobImagesTab.setSelected(false);
                    lineItemsTab.setSelected(true);
                    planImageTab.setSelected(false);
                    Bundle args1 = new Bundle();
                    args1.putString("SaleOrderId", salesOrderId);
                    args1.putString("WorkOrderId", workOrder[0].getFieldValue("id"));
                    args1.putString("WorkOrderName", workOrder[0].getFieldValue("name"));
                    args1.putString("StartTime", workOrder[0].getFieldValue("start_time"));
                    args1.putString("StopTime", workOrder[0].getFieldValue("stop_time"));
                    args1.putString("Status", workOrder[0].getFieldValue("status"));
                    args1.putString("Description", workOrder[0].getFieldValue("description"));
                    LIFragment.setArguments(args1);
                    switchTabContent(LIFragment);
                }
            }
        });

        planImageTab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Pfragment != null && !Pfragment.isVisible()) {
                    ViewAnimationUtils.collapse(headerView);

                    controllerTab.setSelected(false);
                    jobImagesTab.setSelected(false);
                    lineItemsTab.setSelected(false);
                    planImageTab.setSelected(true);
                    Bundle args2 = new Bundle();
                    args2.putString("JobID", jobID);
                    args2.putString("WorkOrderId", workOrder[0].getFieldValue("id"));
                    args2.putString("WorkOrderName", workOrder[0].getFieldValue("name"));
                    Pfragment.setArguments(args2);
                    switchTabContent(Pfragment);
                }
            }
        });
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
        startTimer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
              if(start)
              {
                  start=false;
                  startTime = SystemClock.uptimeMillis();
                  customHandler.postDelayed(updateTimerThread, 0);
                  startTimer.setImageResource(R.drawable.btn_stop);

              }
                else
              {
                  start=true;
                  timeSwapBuff += timeInMilliseconds;
                  customHandler.removeCallbacks(updateTimerThread);
                  startTimer.setImageResource(R.drawable.btn_start);

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
        final SugarBean wOrder = workOrder[0];
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

    public ArrayList<SugarBean> getLineItems() {
        if (lineItems != null && lineItems.length > 0) {
            lItems = new ArrayList();
            for (int i = 0; i < lineItems.length; i++) {
                lItems.add(lineItems[i]);
            }
        } else {
            lItems = new ArrayList();
        }

        return this.lItems;
    }

    public void setNotes(String description) {
        notes.setText(description);
    }

//    public ArrayList<ArrayList<String[]>> getWorkOrder(){
//        if(listRecords != null && listRecords.size() > 0) {
//            ArrayList<ArrayList<String[]>> records = (ArrayList<ArrayList<String[]>>) listRecords.get(1);
//            return records;
//        }
//
//        return null;
//    }

    public SugarBean getWorkOrder() {
        if (workOrder != null && workOrder.length > 0)
            return workOrder[0];

        return new SugarBean(this.fm, "ro_crew_work_order");
    }

    private void switchTabContent(Fragment fragment) {
        if (fragment != null) {
            this.getChildFragmentManager().beginTransaction()
                    .replace(R.id.tab_layout, fragment, "MY_FRAGMENT").commit();

        }
    }

    public void createWorkOrderAndLineItems() {
        p_bar = ProgressDialog.show(this.fm, getResources().getString(R.string.app_name), "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if (NetworkHelper.isAvailable(fm)) {
                        SOAPClient com = new SOAPClient(UserPreferences.url);
                        listRecords = com.getEntryWorkOrder(salesOrderId);

                        if (listRecords != null && listRecords.size() > 0) {
                            String modName = "AOS_Products_Quotes";
                            for (int k = 0; k < listRecords.size(); k++) {
                                ArrayList<ArrayList<String[]>> records = (ArrayList<ArrayList<String[]>>) listRecords.get(k);

                                if (records != null) {
                                    if (k == 0) {
                                        lineItems = new SugarBean[records.size()];
                                        for (int i = 0; i < records.size(); i++) {
                                            ArrayList<String[]> record = records.get(i);
                                            lineItems[i] = new SugarBean(fm, modName);
                                            SugarBean li_bean = new SugarBean(fm, modName);
                                            for (int j = 0; j < record.size(); j++) {
                                                String name = record.get(j)[0];

                                                if (record.get(j)[1] != null) {
                                                    lineItems[i].setFieldValue(name, record.get(j)[1]);
                                                    li_bean.updateFieldValue(name, record.get(j)[1]);
                                                }
                                            }

//                                            li_bean.loadCom(fm, false, true);
//                                            li_bean.save(true);
                                        }
                                    } else {
                                        modName = "ro_crew_work_order";
                                        workOrder = new SugarBean[records.size()];
                                        for (int i = 0; i < records.size(); i++) {
                                            ArrayList<String[]> record = records.get(i);
                                            workOrder[i] = new SugarBean(fm, modName);
                                            SugarBean bean = new SugarBean(fm, modName);
                                            for (int j = 0; j < record.size(); j++) {
                                                String name = record.get(j)[0];

                                                if (record.get(j)[1] != null) {
                                                    workOrder[i].setFieldValue(name, record.get(j)[1]);
                                                    bean.updateFieldValue(name, record.get(j)[1]);
                                                }
                                            }

                                            bean.loadCom(fm, false, true);
                                            bean.save(true);

                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        SugarBean[] newBeans;
                        workOrder = new SugarBean[0];

                        //////// WORK ORDER
                        SugarBean.loadCom(fm, false, true);
                        SugarBean bean = new SugarBean(fm, "ro_crew_work_order");
                        newBeans = bean.retrieveAll(" ro_crew_work_order.id = '" + salesOrderId + "' ", null, 0, 1, 0, null);
                        if (newBeans.length > 0) {
                            workOrder = newBeans;
                        }
// else {
//                            SugarBean jobBean = new SugarBean(fm, "rt_Jobs");
//                            SugarBean[] jobBeans = jobBean.retrieveAll(" rt_jobs.id = '" + jobID + "' ", null, 0, 1, 0, null);
//
//                            String accountName = "";
////                            if(jobBeans != null && jobBeans.length > 0) {
////                                SugarBean jBean = jobBeans[0];
////                                SugarBean accountBean = new SugarBean(fm, "Accounts");
////                                SugarBean[] accountsBeans = accountBean.retrieveAll(" accounts.id = '" + jBean.getFieldValue("jobs_account_builder_id") + "' ", null, 0, 1, 0, null);
////                                if(accountsBeans != null && accountsBeans.length > 0) {
////                                    SugarBean aBean = accountsBeans[0];
////                                    accountName = aBean.getFieldValue("name");
////                                }
////                            }
//
//
//                            newBeans = bean.retrieveAll("", "date_entered desc", 0, 1, 0, null);
//                            Integer number = (Integer.parseInt(newBeans[0].getFieldValue("number"))) + 1;
//
//                            SugarBean wo_bean = new SugarBean(fm, "ro_crew_work_order");
//                            wo_bean.updateFieldValue("name", "Created From Mobile");
//                            wo_bean.updateFieldValue("aos_quotes_id", salesOrderId);
//                            wo_bean.updateFieldValue("assigned_user_id", "1");
//                            wo_bean.updateFieldValue("additional_hrs", "0");
//                            wo_bean.updateFieldValue("sales_order_id", salesOrderNumber);
//                            wo_bean.updateFieldValue("number", number.toString());
//                            wo_bean.updateFieldValue("jobs_account_builder_name", accountName);
//                            wo_bean.updateFieldValue("work_location", jobName);
//                            wo_bean.save(false);
//
//                            newBeans = bean.retrieveAll(" ro_crew_work_order.id = '" + salesOrderId + "' ", null, 0, 1, 0, null);
//                            if (newBeans.length > 0)
//                                workOrder = newBeans;
//                            else
//                                workOrder = new SugarBean[0];
//                        }

                        //////// LINE ITEMS
                        SugarBean lbean = new SugarBean(fm, "AOS_Products_Quotes");
                        newBeans = lbean.retrieveAll(" aos_products_quotes.parent_id = '" + workOrder[0].getFieldValue("id") + "' ", null, 0, 100, 0, null);
                        if (newBeans.length > 0) {
                            lineItems = newBeans;
                        } else {
                            newBeans = lbean.retrieveAll(" aos_products_quotes.parent_id = '" + salesOrderId + "' ", null, 0, 100, 0, null);
                            for (int i = 0; i < newBeans.length; i++) {
                                SugarBean li_bean = newBeans[i];
                                Object[] fields = li_bean.fields.keySet().toArray();
                                for (int j = 0; j < fields.length; j++) {
                                    li_bean.updateFieldValue(fields[j].toString(), li_bean.getFieldValue(fields[j].toString()));
                                }

                                li_bean.updateFieldValue("parent_type", "ro_crew_work_order");
                                li_bean.updateFieldValue("parent_id", workOrder[0].getFieldValue("id"));
                                li_bean.updateFieldValue("id", "");
                                li_bean.save(false);
                            }
                            newBeans = lbean.retrieveAll(" aos_products_quotes.parent_id = '" + workOrder[0].getFieldValue("id") + "' ", null, 0, 100, 0, null);
                            if (newBeans.length > 0) {
                                lineItems = newBeans;
                            } else
                                lineItems = new SugarBean[0];
                        }

                    }


                } catch (Exception e) {
//                    Log.d("ERROR", e.getMessage());
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing()) {
                            p_bar.hide();
                        }

                        populateView();
                    }
                });

            }
        }).start();
    }

    public void populateView() {

        SugarBean wOrder = null;
        SugarBean lItems = null;

        if (workOrder != null && workOrder.length > 0) {
            wOrder = workOrder[0];
            AlertHelper.printBeans(wOrder);
            jobID = wOrder.getFieldValue("rt_jobs_id");
            job_Name.setText(wOrder.getFieldValue("name"));
            workOrderNo.setText(wOrder.getFieldValue("number"));
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
                workLocation.setText(jobName);

        }

        if (lineItems != null && lineItems.length > 0)
            lItems = lineItems[0];

        switchTabContent(Cfragment);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.fm = getActivity();
        if (this.fm != null) {
            ((MainActivity) this.fm).setOnBackPressedListener(new BaseBackPressedListener(this.fm));
        }
    }

    @Override
    public void onResume() {
        MainActivity mainActivity = (MainActivity) this.fm;

        mainActivity.setTitle(title + " - " + getArguments().getString("JobName"));
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

    public void nextAction(int index) {
        switch (index) {
            case 1:
                ViewAnimationUtils.expand(headerView);

                controllerTab.setSelected(true);
                jobImagesTab.setSelected(false);
                lineItemsTab.setSelected(false);
                planImageTab.setSelected(false);
                switchTabContent(Cfragment);
                break;
            case 2:
                ViewAnimationUtils.collapse(headerView);

                controllerTab.setSelected(false);
                jobImagesTab.setSelected(true);
                lineItemsTab.setSelected(false);
                planImageTab.setSelected(false);
                Bundle args = new Bundle();
                args.putString("WorkOrderId", workOrder[0].getFieldValue("id"));
                args.putString("WorkOrderName", workOrder[0].getFieldValue("name"));
                IFragment.setArguments(args);
                switchTabContent(IFragment);
                break;
            case 3:
                ViewAnimationUtils.collapse(headerView);

                controllerTab.setSelected(false);
                jobImagesTab.setSelected(false);
                lineItemsTab.setSelected(true);
                planImageTab.setSelected(false);
                Bundle args1 = new Bundle();
                args1.putString("SaleOrderId", salesOrderId);
                args1.putString("WorkOrderId", workOrder[0].getFieldValue("id"));
                args1.putString("WorkOrderName", workOrder[0].getFieldValue("name"));
                args1.putString("StartTime", workOrder[0].getFieldValue("start_time"));
                args1.putString("StopTime", workOrder[0].getFieldValue("stop_time"));
                args1.putString("Status", workOrder[0].getFieldValue("status"));
                args1.putString("Description", workOrder[0].getFieldValue("description"));
                LIFragment.setArguments(args1);
                switchTabContent(LIFragment);
                break;
            case 4:
                ViewAnimationUtils.collapse(headerView);

                controllerTab.setSelected(false);
                jobImagesTab.setSelected(false);
                lineItemsTab.setSelected(false);
                planImageTab.setSelected(true);
                Bundle args2 = new Bundle();
                args2.putString("JobID", jobID);
                args2.putString("WorkOrderId", workOrder[0].getFieldValue("id"));
                args2.putString("WorkOrderName", workOrder[0].getFieldValue("name"));
                Pfragment.setArguments(args2);
                switchTabContent(Pfragment);
                break;
            default:
                break;
        }
    }

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



