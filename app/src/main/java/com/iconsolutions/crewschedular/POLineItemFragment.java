package com.iconsolutions.crewschedular;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.iconsolutions.adapter.RoLineItemsAdapter;
import com.iconsolutions.helper.UserPreferences;

import java.util.ArrayList;
import java.util.HashMap;

import crewschedular.fragmentinterface.BaseBackPressedListener;
import rolustech.beans.SugarBean;
import rolustech.communication.soap.SOAPClient;
import rolustech.helper.NetworkHelper;
import rolustech.helper.NormalSync;


/**
 * A simple {@link Fragment} subclass.
 */
public class POLineItemFragment extends Fragment {

    View view;
    FragmentActivity fm;
    String salesOrderId;
    ProgressDialog p_bar;
    android.os.Handler handler = new android.os.Handler();
    SugarBean orders[] = null;
    SugarBean workOrder;
    Button nextButton ,previousButton;
    String controllerSaveValue = new String();
    ArrayList<ArrayList<String[]>> records = null;
    protected int offset, selectedRec;
    public static SugarBean[] beans;
    public static SugarBean bean;
    ListView pmFeedList;
    private String userWhere="",orderByField = "date_entered", orderByDir = "ASC";
    private String workOrderId,workOrderName;
    private ArrayList<SugarBean> jobs;
    private String[] fields;
    HashMap<String,String> data;
    ArrayList<HashMap<String,String>> datas =new ArrayList<>();
    RoLineItemsAdapter pmFeedBacklineitemadopter;
    public POLineItemFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_polineitem, container, false);
        workOrderId = getArguments().getString("WorkOrderId");
        workOrderName = getArguments().getString("WorkOrderName");
        initUI();
        datas = new ArrayList<>();
 //      pmFeedBacklineitemadopter = new RoLineItemsAdapter(getContext(),datas,R.layout.pm_feedback_list_item);
  //      pmFeedList.setAdapter(pmFeedBacklineitemadopter);
        return view;
    }

    private void initUI() {

        if (workOrder != null && controllerSaveValue.length() == 0) {
            controllerSaveValue = workOrder.getFieldValue("controller_multi_select");
        }

        pmFeedList = (ListView)view.findViewById(R.id.po_line_item_list);

        view.findViewById(R.id.polineitems_prev_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToTabView(5);
            }
        });
        if (UserPreferences.reLoadPrefernces(this.fm)) {
            bean = new SugarBean(this.fm, "ro_crew_work_line_items");
        }
        userWhere = bean.moduleName.toLowerCase() + ".ro_crew_work_order_id = '"+workOrderId+"'";
        fields = new String[]{"name","start_time","end_time","totalmen","installed_qty","total_amount","aos_products_quotes_id"
                ,"crew_work_id","rt_batch_id","ro_crew_work_order_id","batch_number","is_approved","date_entered"};

           createWorkOrderLineItemsOnline();
    }

    public void createWorkOrderLineItemsOnline() {
        p_bar = ProgressDialog.show(this.fm, getResources().getString(R.string.app_name), "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    data = new HashMap<String, String>();
                    String whr = "";
                    if (userWhere != null && userWhere.length() > 0) {
                        whr += userWhere;
                    }

                    if (NetworkHelper.isAvailable(getActivity())) {
                        SOAPClient com = new SOAPClient(UserPreferences.url);
                        ArrayList<ArrayList<String[]>> records = null;
                        String modName = "AOS_Products_Quotes";
                        try {
                            records = com.getEntryList(modName, fields,"", 100, 0,"",0, null);
                            if (records != null) {
                                beans = new SugarBean[records.size()];
                                    for (int i = 0; i < records.size(); i++) {
                                        ArrayList<String[]> record = records.get(i);
                                        beans[i] = new SugarBean(fm, modName);
                                        SugarBean li_bean = new SugarBean(fm, modName);
                                        data =new HashMap<String, String>();
                                        for (int j = 0; j < record.size(); j++) {
                                            String name = record.get(j)[0];
                                            data.put(name, record.get(j)[1]);
                                            if (record.get(j)[1] != null) {
                                                beans[i].setFieldValue(name, record.get(j)[1]);
                                                li_bean.updateFieldValue(name, record.get(j)[1]);
                                                data.put(name, record.get(j)[1]);
                                                }
                                        }
                                        datas.add(data);
                                        Log.d("Crew_app_PM"," Data List  => "+data.toString());
  //                                          li_bean.loadCom(fm, false, true);
  //                                          li_bean.save(true);
                                    }

                                }
                            } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        SugarBean[] newBeans;
                        beans = new SugarBean[0];
                        SugarBean.loadCom(getActivity(), false, true);
                        newBeans = bean.retrieveAll(whr, orderByField + " " + orderByDir, offset, 100, 0, null);
                        if (newBeans.length > 0) {
                            beans = newBeans;
                        } else {
                            beans = new SugarBean[0];
                        }
                    }
                }catch (Exception e) {
//                    Log.d("ERROR", e.getMessage());
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing()) {
                            p_bar.hide();
                        }
//                        pmFeedBacklineitemadopter.notifyDataSetChanged();
                        populateJobs();
                    }
                });

            }
        }).start();
    }

    public void createWorkOrderAndLineItems() {
        p_bar = ProgressDialog.show(this.fm, getResources().getString(R.string.app_name), "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String whr = "";
                    if (userWhere != null && userWhere.length() > 0) {
                         whr += userWhere;
                    }
                    SugarBean[] newBeans;
                    beans = new SugarBean[0];

                    if (NetworkHelper.isAvailable(getActivity())) {
                        SugarBean.loadCom(getActivity(), true, false);
                        if (NormalSync.loadFromServer()) {
                            newBeans = bean.retrieveAll(whr, orderByField + " " + orderByDir, offset, 100, 0, null);
                            if (newBeans.length > 0) {
                                beans = newBeans;
                            } else {
                                beans = new SugarBean[0];
                            }
                        }
                    } else {
                        SugarBean.loadCom(getActivity(), false, true);
                        newBeans = bean.retrieveAll(whr, orderByField + " " + orderByDir, offset, 100, 0, null);
                        if (newBeans.length > 0) {
                            beans = newBeans;
                        } else {
                            beans = new SugarBean[0];
                        }
                    }
                }catch (Exception e) {
//                    Log.d("ERROR", e.getMessage());
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing()) {
                            p_bar.hide();
                        }
                        populateJobs();
                    }
                });

            }
        }).start();
    }

    private void populateJobs() {
        if (beans != null && beans.length > 0) {
            jobs = new ArrayList();
            for (int i = 0; i < beans.length; i++) {
                jobs.add(beans[i]);
            }
        } else {
            jobs = new ArrayList();
                    }

    }

     public void moveToTabView(int tabNumber){
        WorkOrderFragment parentFragment = (WorkOrderFragment) this.getParentFragment();
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
