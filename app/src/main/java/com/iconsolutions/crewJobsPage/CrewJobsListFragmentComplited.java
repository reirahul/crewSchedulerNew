package com.iconsolutions.crewJobsPage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.iconsolutions.adapter.JobsListAdapter;
import com.iconsolutions.crewschedular.MainActivity;
import com.iconsolutions.crewschedular.R;
import com.iconsolutions.helper.UserPreferences;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import crewschedular.fragmentinterface.BaseBackPressedListener;
import rolustech.beans.SugarBean;
import rolustech.helper.AlertHelper;
import rolustech.helper.NetworkHelper;
import rolustech.helper.NormalSync;

/**
 * Created by kashif on 3/22/16.
 */
public class CrewJobsListFragmentComplited extends Fragment{

    private static final String ARG_POSITION = "position";
    private static final String ARG_WHERE = "";
    View view;
    FragmentActivity fm;
    String title = "Crew Daily Schedule";

    ListView jobs_lv;

    ImageView previousDate, nextDate;
    TextView displayDate;

    ArrayList<SugarBean> jobs = null;
    ProgressDialog p_bar;
    Handler handler = new Handler();

    protected SugarBean beans[];
    protected SugarBean bean;

    protected String where = "", userWhere = "", hardWhere, orderByField = "date_entered", orderByDir = "ASC";
    protected int offset, selectedRec;

    protected Hashtable<String, String> sortFields = new Hashtable<String, String>();

    protected boolean refreshCount = false, returnID = false;

    Boolean isUpdated = false;
    JobsListAdapter adapter = null;

    int daysCounter = 0;

    Boolean error = false;

    View focus ;
    public int position;

    public CrewJobsListFragmentComplited() {
        this.fm = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup v, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_jobslist, null);
        initUI();
        getContent();
        return view;
    }

    private void initUI() {

        MainActivity mainActivity = (MainActivity) this.fm;
        mainActivity.setTitle(title);

        jobs_lv = (ListView) view.findViewById(R.id.jobs_lv);
        displayDate = (TextView) view.findViewById(R.id.selected_date);
        previousDate = (ImageView) view.findViewById(R.id.previous_date);
        nextDate = (ImageView) view.findViewById(R.id.next_date);

        jobs = new ArrayList();
        adapter = new JobsListAdapter(this.fm, jobs, R.layout.job_list_item);
        jobs_lv.setAdapter(adapter);

        bean = new SugarBean(this.fm, "ro_crew_work_order");
            final Date today = new Date();
            String date = dateForDisplay(today);
            displayDate.setText(date);

            if (UserPreferences.reLoadPrefernces(this.fm)) {
                userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "' AND " + bean.moduleName.toLowerCase() + ".status = 'All Task Completed' )";
                isUpdated = true;
                displayDate.setTextColor(Color.GRAY);
            }



        nextDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                daysCounter = daysCounter + 1;
                userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".date_start = '" + dateByAddingDays(daysCounter) + "')";
                isUpdated = true;
                displayDate.setTextColor(Color.BLACK);
                getContent();
            }
        });

        previousDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                daysCounter = daysCounter - 1;
                userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".date_start = '" + dateByAddingDays(daysCounter) + "')";
                isUpdated = true;
                displayDate.setTextColor(Color.BLACK);
                getContent();

            }
        });
    }

    public static String toyyMMdd(Date day) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(day);
        return date;
    }

    public static String dateForDisplay(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy");
        String dispDate = formatter.format(date);
        return dispDate;
    }

    public String dateByAddingDays(int days) {

        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        Date tommrrow = cal.getTime();
        String finalDate = toyyMMdd(tommrrow);

        String dispDate = dateForDisplay(tommrrow);
        displayDate.setText(dispDate);

        return finalDate;
    }

    private void getContent() {

        p_bar = ProgressDialog.show(this.fm, getResources().getString(R.string.app_name), "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);

        //Fetching records
        new Thread(new Runnable() {
            public void run() {
                error = false;
                fetchBeans();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar!=null) {
                            p_bar.cancel();
                            p_bar.dismiss();
                        }
                        populateJobs();

                        if (error) {
                            AlertHelper.showAlert(fm, UserPreferences.APP_NAME, "There was some error while fetching data, please try again.");
                        }

                    }
                });
            }
        }).start();
    }

    /*
 *  Fetching beans to populate list view
 */
    protected void fetchBeans() {
        //Fetching beans
        try {
            String whr = where;
            if (userWhere != null && userWhere.length() > 0) {
                if (whr != null && where.length() > 0) {
                    whr += " AND ";
                } else {
                    whr = "";
                }

                whr += userWhere;
            }

            SugarBean[] newBeans;
            beans = new SugarBean[0];

            if (NetworkHelper.isAvailable(this.fm)) {
                SugarBean.loadCom(this.fm, true, false);
                if (NormalSync.loadFromServer()) {
                    newBeans = bean.retrieveAll(whr, orderByField + " " + orderByDir, offset, 100, 0, null);
                    if (newBeans.length > 0) {
                        beans = newBeans;
                    } else {
                        beans = new SugarBean[0];
                    }
                }
            } else {
                SugarBean.loadCom(this.fm, false, true);
                newBeans = bean.retrieveAll(whr, orderByField + " " + orderByDir, offset, 100, 0, null);
                if (newBeans.length > 0) {
                    beans = newBeans;
                } else {
                    beans = new SugarBean[0];
                }
            }

        } catch (Exception e) {
            Log.e("Exception: ", e.getMessage());
            beans = new SugarBean[0];

            error = true;
        }

        if (beans == null) beans = new SugarBean[0];

    }

    private void populateJobs() {
        jobs.clear();
        if (beans != null && beans.length > 0) {
            jobs = new ArrayList();
            for (int i = 0; i < beans.length; i++) {
                jobs.add(beans[i]);
            }
            adapter.updateReceiptsList(jobs);

        } else {
            adapter.updateReceiptsList(jobs);
        }

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

        mainActivity.setTitle(title);
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
