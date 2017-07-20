package com.iconsolutions.crewschedular;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iconsolutions.helper.UserPreferences;
import com.iconsolutions.helper.VerticalViewPager;
import com.iconsolutions.jobfragments.ControllerFragment;
import com.iconsolutions.jobfragments.JobImagesFragment;
import com.iconsolutions.jobfragments.LineItemsFragment;
import com.iconsolutions.jobfragments.NotesFragment;
import com.iconsolutions.jobfragments.PMFeedbackFragment;
import com.iconsolutions.jobfragments.POLineItemFragment;
import com.iconsolutions.jobfragments.PlanImageFragment;
import com.iconsolutions.jobfragments.WorkOrderFragment;
import com.iconsolutions.menuhelper.JobMenuFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;

import crewschedular.fragmentinterface.BaseBackPressedListener;
import rolustech.beans.SugarBean;
import rolustech.communication.soap.SOAPClient;
import rolustech.helper.AlertHelper;
import rolustech.helper.NetworkHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class JobDetailsHomeFragment extends Fragment implements View.OnClickListener{
    View view;
    FragmentActivity fm;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    VerticalViewPager mViewPager;
    private View focus;
    ProgressDialog p_bar;
    private SlidingMenu leftMenu;
    private JobMenuFragment menuListFragment;
    private LinearLayout left_menu_btn;
    android.os.Handler handler = new android.os.Handler();
    ArrayList<SugarBean> lItems = null;
    private SugarBean bean;
    ArrayList listRecords;
    String title = "Work Order Update",salesOrderId,jobName;
    SugarBean workOrder[] = null, lineItems[] = null;
    private TextView workOrderNo,job_Name;
    private Fragment pmfragment,Cfragment,jIFragment,LIFragment,pofragment,Pfragment,wFragment;
    private String jobID="";

    public JobDetailsHomeFragment() {
        // Required empty public constructor
    }
    public static Fragment newInstance(int position, String s) {
        return new CrewJobsListFragmentHome();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup v, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_job_home, null);


        mViewPager = (VerticalViewPager)view.findViewById(R.id.jobpager);
        //       mViewPager.setPageTransformer(true, new DepthPageTransformer());

        left_menu_btn = (LinearLayout)getActivity().findViewById(R.id.left_menu_btn);
        left_menu_btn.setOnClickListener(this);

        focus = getActivity().getCurrentFocus();
        initUI();
        setLeftSlideMenu();
        toggleLeftSlideMenu();
        return view;

    }
    private void initUI() {
        wFragment = new WorkOrderFragment();
        pmfragment = new PMFeedbackFragment();
        Cfragment = new ControllerFragment();
        jIFragment = new JobImagesFragment();
        LIFragment = new LineItemsFragment();
        Pfragment = new PlanImageFragment();
        pofragment = new POLineItemFragment();

        MainActivity mainActivity = (MainActivity) this.fm;
        mainActivity.setTitle(title + " - " + getArguments().getString("JobName"));
//        purchageOrder = (RelativeLayout) view.findViewById(R.id.purchase_orders);
//        pOItems = (RelativeLayout) view.findViewById(R.id.po_line_items);
        workOrderNo = (TextView) view.findViewById(R.id.work_order);

        job_Name = (TextView) view.findViewById(R.id.job_name);

        left_menu_btn = (LinearLayout)getActivity().findViewById(R.id.left_menu_btn);
        left_menu_btn.setOnClickListener(this);

        listRecords = new ArrayList();

//        ViewAnimationUtils.expand(headerView);

        salesOrderId = getArguments().getString("SalesOrderId");
   //        jobID = getArguments().getString("JobID");
        jobName = getArguments().getString("JobName");
        job_Name.setText(jobName);
        createWorkOrderAndLineItems();
    }

    public void nextAction(int position)
    {
        mViewPager.setCurrentItem(position,true);
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

//    public ArrayList<ArrayList<String[]>> getWorkOrder(){
//        if(listRecords != null && listRecords.size() > 0) {
//            ArrayList<ArrayList<String[]>> records = (ArrayList<ArrayList<String[]>>) listRecords.get(1);
//            return records;
//        }
//
//        return null;
//    }


    @Override
    public void onClick(View view) {
//        focus = getActivity().getCurrentFocus();
        switch (view.getId())
        {
            case R.id.left_menu_btn:
                if (focus != null) {
                    hiddenKeyboard(view);
                }
                toggleLeftSlideMenu();
                break;
        }
    }

    public SugarBean getWorkOrder() {
        if (workOrder != null && workOrder.length > 0)
            return workOrder[0];
        return new SugarBean(this.fm, "ro_crew_work_order");
    }
/*
    private void switchTabContent(Fragment fragment) {
        if (fragment != null) {
            this.getChildFragmentManager().beginTransaction()
                    .replace(R.id.tab_layout, fragment, "MY_FRAGMENT").commit();

        }
    }
*/
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
                                    }
                                    else {
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

//                                            bean.loadCom(fm, false, true);
//                                            bean.save(true);

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
        }
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        // Set up the ViewPager with the sections adapter.

        mViewPager.setAdapter(mSectionsPagerAdapter);
        //       mViewPager.setCurrentItem(pos);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position)
                {
                    case 0:
                        ((JobMenuFragment) getFragmentManager().findFragmentById(R.id.left_menu_frame)).selectTab(R.id.main_landing);
                        break;
                    case 1:
                        ((JobMenuFragment) getFragmentManager().findFragmentById(R.id.left_menu_frame)).selectTab(R.id.po_line_item);
                        break;
                    case 2:
                        ((JobMenuFragment) getFragmentManager().findFragmentById(R.id.left_menu_frame)).selectTab(R.id.line_item);
                        break;
                    case 3:
                        ((JobMenuFragment) getFragmentManager().findFragmentById(R.id.left_menu_frame)).selectTab(R.id.plan_image);
                        break;
                    case 4:
                        ((JobMenuFragment) getFragmentManager().findFragmentById(R.id.left_menu_frame)).selectTab(R.id.notes);
                        break;
                    case 5:
                        ((JobMenuFragment) getFragmentManager().findFragmentById(R.id.left_menu_frame)).selectTab(R.id.job_image);
                        break;
                    case 6:
                        ((JobMenuFragment) getFragmentManager().findFragmentById(R.id.left_menu_frame)).selectTab(R.id.controller);
                        break;
                  case 7:
                        ((JobMenuFragment) getFragmentManager().findFragmentById(R.id.left_menu_frame)).selectTab(R.id.feedback);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private void setLeftSlideMenu() {
//        getActionBar().hide();
        leftMenu = new SlidingMenu(getActivity());
        leftMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        leftMenu.setBehindWidth(110);
        leftMenu.setFadeDegree(0.35f);
        leftMenu.attachToActivity(getActivity(), SlidingMenu.SLIDING_CONTENT);
        leftMenu.setMenu(R.layout.left_menu_frame);
        leftMenu.setMode(SlidingMenu.LEFT);
        setLeftMenuListFragment();
    }
    public void setLeftMenuListFragment() {
        menuListFragment = new JobMenuFragment();
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


    private void hiddenKeyboard(View v) {
        InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
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
        if (leftMenu.isMenuShowing()) {
            leftMenu.showContent();
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {

 //           Toast.makeText(getActivity(),"Postion --> "+position,Toast.LENGTH_SHORT).show();
            switch (position)
            {
                case 0:
                    return new WorkOrderFragment();

                case 1:
                    Bundle args6 = new Bundle();
                    args6.putString("WorkOrderId", workOrder[0].getFieldValue("id"));
                    args6.putString("WorkOrderName", workOrder[0].getFieldValue("name"));
                    pofragment.setArguments(args6);
                    return pofragment;

                case 2:
                    Bundle args1 = new Bundle();
                    args1.putString("SaleOrderId", salesOrderId);
                    args1.putString("WorkOrderId", workOrder[0].getFieldValue("id"));
                    args1.putString("WorkOrderName", workOrder[0].getFieldValue("name"));
                    args1.putString("StartTime", workOrder[0].getFieldValue("start_time"));
                    args1.putString("StopTime", workOrder[0].getFieldValue("stop_time"));
                    args1.putString("Status", workOrder[0].getFieldValue("status"));
                    args1.putString("Description", workOrder[0].getFieldValue("description"));
                    LIFragment.setArguments(args1);
                    return LIFragment;
               case 3:
                    Bundle args2 = new Bundle();
                    args2.putString("JobID", jobID);
                    args2.putString("WorkOrderId", workOrder[0].getFieldValue("id"));
                    args2.putString("WorkOrderName", workOrder[0].getFieldValue("name"));
                    Pfragment.setArguments(args2);
                    return Pfragment;

                case 4:
                    return new NotesFragment();
                case 5:
                    Bundle args = new Bundle();
                    args.putString("WorkOrderId", workOrder[0].getFieldValue("id"));
                    args.putString("WorkOrderName", workOrder[0].getFieldValue("name"));
                    jIFragment.setArguments(args);
                    return jIFragment;

                case 6:
                    return new ControllerFragment();
                case 7:
                    Bundle args7 = new Bundle();
                    args7.putString("WorkOrderId", workOrder[0].getFieldValue("id"));
                    args7.putString("WorkOrderName", workOrder[0].getFieldValue("name"));
                    pmfragment.setArguments(args7);
                    return pmfragment;

                default:  return null;
            }

        }

        @Override
        public int getCount() {
            // Show 8 total pages.
            return 8;
        }
    }

}
