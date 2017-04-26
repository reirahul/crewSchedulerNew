package com.iconsolutions.crewschedular;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.iconsolutions.adapter.JobsListAdapter;
import com.iconsolutions.helper.UserPreferences;
import com.iconsolutions.menuhelper.MenuListFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

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
public class CrewJobsListFragment extends Fragment implements OnClickListener{

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

    private SlidingMenu leftMenu;

    private MenuListFragment menuListFragment;

    private LinearLayout left_menu_btn;

    int daysCounter = 0;

    Boolean error = false;

    View focus ;

    public CrewJobsListFragment() {
        this.fm = getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup v, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_jobslist, null);
        focus = getActivity().getCurrentFocus();
        initUI();
        getContent();
        setLeftSlideMenu();
        toggleLeftSlideMenu();
        return view;
    }

    private void initUI() {

        MainActivity mainActivity = (MainActivity) this.fm;
        mainActivity.setTitle(title);

        jobs_lv = (ListView) view.findViewById(R.id.jobs_lv);
        displayDate = (TextView) view.findViewById(R.id.selected_date);
        previousDate = (ImageView) view.findViewById(R.id.previous_date);
        nextDate = (ImageView) view.findViewById(R.id.next_date);

        left_menu_btn = (LinearLayout)getActivity().findViewById(R.id.left_menu_btn);
        left_menu_btn.setOnClickListener(this);


        final Date today = new Date();
        String date = dateForDisplay(today);
        displayDate.setText(date);

        if (UserPreferences.reLoadPrefernces(this.fm)) {
            bean = new SugarBean(this.fm, "ro_crew_work_order");
            long currentDate = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateString = sdf.format(currentDate);
//            userWhere = "";
            userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".date_start = '" + dateString + "')";
//            userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".date_start = '" + dateString + "')";

            displayDate.setTextColor(Color.BLACK);

//        long date1= System.currentTimeMillis();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        String dateString = sdf.format(date1);
//        displayDate.setText(dateForDisplay(new Date()));
//        userWhere = "(" +bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_manager_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".date_start < '" + dateString + "')";

//        userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " +bean.moduleName.toLowerCase() + ".assigned_user_id = '" + "1" + "')";

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

    public void onPreviousJob()
    {
        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(date);
        displayDate.setText(dateForDisplay(new Date()));
        userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".date_start < '" + dateString + "')";
        isUpdated = true;
        displayDate.setTextColor(Color.GRAY);
        getContent();
    }
    public void onCompletedJob()
    {
        userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "' AND " + bean.moduleName.toLowerCase() + ".status = 'All Task Completed' )";
        isUpdated = true;
        displayDate.setTextColor(Color.GRAY);
        getContent();

    }
    public void onInCompletedJob()
    {
        userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "' AND " +bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "' AND (" + bean.moduleName.toLowerCase() + ".status <> 'All Task Completed' OR " + bean.moduleName.toLowerCase() + ".status IS NULL ))";
        isUpdated = true;
        displayDate.setTextColor(Color.GRAY);
        getContent();
    }
    public void onTodayJob()
    {
        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(date);
        displayDate.setText(dateForDisplay(new Date()));
        userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".date_start = '" + dateString + "')";
        isUpdated = true;
        displayDate.setTextColor(Color.GRAY);
        getContent();
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
        if (beans != null && beans.length > 0) {
            jobs = new ArrayList();
            for (int i = 0; i < beans.length; i++) {
                jobs.add(beans[i]);
            }

            if (!isUpdated) {
                adapter = new JobsListAdapter(this.fm, jobs, R.layout.job_list_item);
                jobs_lv.setAdapter(adapter);
            } else {
                adapter.updateReceiptsList(jobs);
            }
        } else {
            jobs = new ArrayList();
            if (!isUpdated) {
                adapter = new JobsListAdapter(this.fm, jobs, R.layout.job_list_item);
                jobs_lv.setAdapter(adapter);
            } else {
                adapter.updateReceiptsList(jobs);
            }
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


    private void setLeftSlideMenu() {
//        getActionBar().hide();
        leftMenu = new SlidingMenu(getActivity());
        leftMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
 //       leftMenu.setShadowWidthRes(R.dimen.bottom_margin);
//		menu.setShadowDrawable(R.drawable.shadow);
 //       leftMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset_left);
        leftMenu.setBehindWidth(110);
        leftMenu.setFadeDegree(0.35f);
        leftMenu.attachToActivity(getActivity(), SlidingMenu.SLIDING_CONTENT);
        leftMenu.setMenu(R.layout.left_menu_frame);
        leftMenu.setMode(SlidingMenu.LEFT);
        setLeftMenuListFragment();
    }
    public void setLeftMenuListFragment() {
        menuListFragment = new MenuListFragment();
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


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_menu_btn:
                if (focus != null) {
                    hiddenKeyboard(v);
                }
                toggleLeftSlideMenu();
                break;

        }
    }

    private void hiddenKeyboard(View v) {
        InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

}
