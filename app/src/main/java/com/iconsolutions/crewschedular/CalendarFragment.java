package com.iconsolutions.crewschedular;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iconsolutions.adapter.JobsListAdapter;
import com.iconsolutions.helper.EventDecorator;
import com.iconsolutions.helper.UserPreferences;
import com.iconsolutions.menuhelper.MenuListFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import crewschedular.fragmentinterface.BaseBackPressedListener;
import rolustech.beans.SugarBean;
import rolustech.helper.NetworkHelper;
import rolustech.helper.NormalSync;

/**
 * Created by kashif on 4/19/16.
 */
public class CalendarFragment extends Fragment implements RadioGroup.OnCheckedChangeListener,View.OnClickListener {

    View view;
    FragmentActivity fm;
    private RadioGroup radioGroupSelector;
    private RadioButton rbDay, rbWeek, rbMonth, rbToday;
    MaterialCalendarView materialCalendarView;
    Date mSelectedDate;
    TextView tvSelectedDate;
    RelativeLayout dateNavigator;
    ImageView previous_date, next_date;

    String selectedDate;
    ListView jobs_lv;
    RelativeLayout completedJobs, inCompletedJobs;

    ArrayList<SugarBean> jobs = null;
    ProgressDialog p_bar;
    Handler handler = new Handler();

    private View focus;

    protected SugarBean beans[];
    protected SugarBean bean;

    private SlidingMenu leftMenu;

    private LinearLayout left_menu_btn;
    private MenuListFragment menuListFragment;

    protected String where = "", userWhere = "", orderByField = "date_start", orderByDir = "ASC";
    protected int offset = 0;
    Boolean isUpdated = false, fetchingData = false;
    JobsListAdapter adapter = null;

    private static final SimpleDateFormat fmtOut = new SimpleDateFormat("EEEE d MMM yyyy");
    Boolean isMonthData = true;
    List<CalendarDay> list = new ArrayList<CalendarDay>();

    public CalendarFragment() {
        this.fm = getActivity();
    }

//    public CalendarFragment(FragmentActivity fa){
//        this.fm = fa;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup v, Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) this.fm;
        mainActivity.setTitle("Crew Daily Schedule");

        view = inflater.inflate(R.layout.fragment_calendar, v, false);
        rbDay = (RadioButton) view.findViewById(R.id.rbDay);
        rbWeek = (RadioButton) view.findViewById(R.id.rbWeek);
        rbMonth = (RadioButton) view.findViewById(R.id.rbMonth);
        rbToday = (RadioButton) view.findViewById(R.id.rbToday);
        tvSelectedDate = (TextView) view.findViewById(R.id.selected_date);
        radioGroupSelector = (RadioGroup) view.findViewById(R.id.radioGroupSelector);
        dateNavigator = (RelativeLayout) view.findViewById(R.id.dateNavigator);
        previous_date = (ImageView) view.findViewById(R.id.previous_date);
        next_date = (ImageView) view.findViewById(R.id.next_date);
        jobs_lv = (ListView) view.findViewById(R.id.jobs_listview);
        completedJobs = (RelativeLayout) view.findViewById(R.id.complete_jobs);
        inCompletedJobs = (RelativeLayout) view.findViewById(R.id.incompleted_jobs);

        completedJobs.setSelected(false);
        inCompletedJobs.setSelected(false);

        left_menu_btn = (LinearLayout)getActivity().findViewById(R.id.left_menu_btn);
        left_menu_btn.setOnClickListener(this);

        focus = getActivity().getCurrentFocus();
        setLeftSlideMenu();
        toggleLeftSlideMenu();

        isMonthData = true;

//        jobs_lv.setOnTouchListener(new View.OnTouchListener() {
//            // Setting on Touch Listener for handling the touch inside ScrollView
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                // Disallow the touch request for parent scroll on touch of child view
//                v.getParent().requestDisallowInterceptTouchEvent(true);
//                return false;
//            }
//        });

        try {
            setMaterialCalendarView();
            setListener();
            radioGroupSelector.setOnCheckedChangeListener(this);
            rbToday.setChecked(true);
        } catch (Exception e) {

        }
        return view;
    }

    /****
     * Method for Setting the Height of the ListView dynamically.
     * *** Hack to fix the issue of not showing all the items of the ListView
     * *** when placed inside a ScrollView
     ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public void PreviousJob()
    {
        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(date);
        userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".date_start < '" + dateString + "')";
        isMonthData = false;
        completedJobs.setSelected(true);
        inCompletedJobs.setSelected(false);
        getContent();
    }
    public void TodayJob()
    {
        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(date);
        userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".date_start = '" + dateString + "')";
        isUpdated = true;
        isUpdated = true;
        isMonthData = false;
        getContent();
    }

 public void CompletedJob()
    {
        userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND  " + bean.moduleName.toLowerCase() + ".status = 'All Task Completed' )";
//                userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " +bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".complete = 1 )";
        isUpdated = true;
        isMonthData = false;
        getContent();
    }

 public void InCompletedJob()
    {
        userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " +bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "' AND (" + bean.moduleName.toLowerCase() + ".status <> 'All Task Completed' OR " + bean.moduleName.toLowerCase() + ".status IS NULL ))";
        //          userWhere = "(" +bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".complete = 0  OR B IS NULL)";
        isUpdated = true;
        isMonthData = false;
        completedJobs.setSelected(false);
        inCompletedJobs.setSelected(true);
        getContent();
    }


    private void setListener() {
        previous_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completedJobs.setSelected(false);
                inCompletedJobs.setSelected(false);

                Calendar calendar = Calendar.getInstance();

                if (mSelectedDate == null)
                    mSelectedDate = calendar.getTime();

                calendar.setTime(mSelectedDate);
                mSelectedDate = setOnDayTextChange(calendar, false);
                isMonthData = false;
                setSelectedDateText(mSelectedDate);
            }
        });
        next_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completedJobs.setSelected(false);
                inCompletedJobs.setSelected(false);
                Calendar calendar = Calendar.getInstance();

                if (mSelectedDate == null)
                    mSelectedDate = calendar.getTime();

                calendar.setTime(mSelectedDate);
                mSelectedDate = setOnDayTextChange(calendar, true);
                isMonthData = false;
                setSelectedDateText(mSelectedDate);
            }
        });

        completedJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND  " + bean.moduleName.toLowerCase() + ".status = 'All Task Completed' )";
//                userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " +bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".complete = 1 )";
                isUpdated = true;
                isMonthData = false;
                completedJobs.setSelected(true);
                inCompletedJobs.setSelected(false);
                getContent();
            }
        });

        inCompletedJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " +bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "' AND (" + bean.moduleName.toLowerCase() + ".status <> 'All Task Completed' OR " + bean.moduleName.toLowerCase() + ".status IS NULL ))";
      //          userWhere = "(" +bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " + bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".complete = 0  OR B IS NULL)";
                isUpdated = true;
                isMonthData = false;
                completedJobs.setSelected(false);
                inCompletedJobs.setSelected(true);

                getContent();
            }
        });
    }

    private void setMaterialCalendarView() {
        materialCalendarView = (MaterialCalendarView) view.findViewById(R.id.calendarView);
//        materialCalendarView.setTopbarVisible(false);

        materialCalendarView.setSelectionColor(getResources().getColor(R.color.light_gray));
        mSelectedDate = Calendar.getInstance().getTime();
//        materialCalendarView.setTitleFormatter(new DateFormatTitleFormatter(fmtOut));
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                mSelectedDate = date.getDate();
                isMonthData = false;
                setSelectedDateText(mSelectedDate);

            }
        });
        isMonthData = true;
        materialCalendarView.setSelectedDate(mSelectedDate);
        materialCalendarView.setCurrentDate(mSelectedDate);
        setSelectedDateText(mSelectedDate);
    }

    public void setSelectedDateText(Date date) {
        if (UserPreferences.reLoadPrefernces(getContext()) && !fetchingData) {
            completedJobs.setSelected(false);
            inCompletedJobs.setSelected(false);
            fetchingData = true;
            bean = new SugarBean(getContext(), "ro_crew_work_order");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateString = sdf.format(date);
            selectedDate = dateString;
//            userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " +bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".date_start = '" + dateString + "'" + " AND " + bean.moduleName.toLowerCase() + ".status != 'All Task Completed' )";
            userWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " +bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".date_start = '" + dateString + "'" + ")";

            getContent();

            tvSelectedDate.setText(fmtOut.format(date));
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        completedJobs.setSelected(false);
        inCompletedJobs.setSelected(false);
        switch (checkedId) {
            case R.id.rbDay:
                if (rbDay.isChecked()) {
                    isMonthData = false;
                    rbDay.setTextColor(Color.WHITE);
                    rbMonth.setTextColor(getResources().getColor(R.color.light_gray));
                    rbToday.setTextColor(getResources().getColor(R.color.light_gray));
                    rbWeek.setTextColor(getResources().getColor(R.color.light_gray));
                    seDayVisible();
                }
                break;
            case R.id.rbMonth:
                if (rbMonth.isChecked()) {
                    isMonthData = false;
                    rbDay.setTextColor(getResources().getColor(R.color.light_gray));
                    rbMonth.setTextColor(Color.WHITE);
                    rbToday.setTextColor(getResources().getColor(R.color.light_gray));
                    rbWeek.setTextColor(getResources().getColor(R.color.light_gray));
                    setMonthVisible();
                }
                break;
            case R.id.rbWeek:
                if (rbWeek.isChecked()) {
                    isMonthData = false;
                    rbDay.setTextColor(getResources().getColor(R.color.light_gray));
                    rbMonth.setTextColor(getResources().getColor(R.color.light_gray));
                    rbToday.setTextColor(getResources().getColor(R.color.light_gray));
                    rbWeek.setTextColor(Color.WHITE);
                    setWeekVisible();
                }
                break;
            case R.id.rbToday:
                if (rbToday.isChecked()) {
                    setTodayVisible();
                    rbDay.setTextColor(getResources().getColor(R.color.light_gray));
                    rbMonth.setTextColor(getResources().getColor(R.color.light_gray));
                    rbToday.setTextColor(Color.WHITE);
                    rbWeek.setTextColor(getResources().getColor(R.color.light_gray));
                }
                break;

        }
    }

    private void setWeekVisible() {
        materialCalendarView.setVisibility(View.VISIBLE);
        materialCalendarView.setCalendarDisplayMode(CalendarMode.WEEKS);
//        mSelectedDate = Calendar.getInstance().getTime();
        materialCalendarView.setSelectedDate(mSelectedDate);

        Calendar now = Calendar.getInstance();
        now.setTime(mSelectedDate);
        now.add(Calendar.DATE, 7);
        materialCalendarView.setCurrentDate(CalendarDay.from(now), true);


//        setSelectedDateText(mSelectedDate);
        dateNavigator.setVisibility(View.GONE);

    }

    private void setTodayVisible() {

        dateNavigator.setVisibility(View.GONE);
        materialCalendarView.setVisibility(View.VISIBLE);
        materialCalendarView.setCalendarDisplayMode(CalendarMode.MONTHS);
        mSelectedDate = Calendar.getInstance().getTime();
        materialCalendarView.setSelectedDate(mSelectedDate);
        materialCalendarView.setCurrentDate(mSelectedDate);
        if (!isMonthData) {
            setSelectedDateText(mSelectedDate);
        }
    }

    private void setMonthVisible() {

        dateNavigator.setVisibility(View.GONE);
        materialCalendarView.setVisibility(View.VISIBLE);
        materialCalendarView.setCalendarDisplayMode(CalendarMode.MONTHS);
//        mSelectedDate = Calendar.getInstance().getTime();
        materialCalendarView.setSelectedDate(mSelectedDate);
        materialCalendarView.setCurrentDate(mSelectedDate);

//        setSelectedDateText(mSelectedDate);
    }

    private void seDayVisible() {
        materialCalendarView.setVisibility(View.GONE);
        dateNavigator.setVisibility(View.VISIBLE);
//        mSelectedDate = Calendar.getInstance().getTime();
//        setSelectedDateText(mSelectedDate);
    }


    private Date setOnDayTextChange(Calendar calendar, boolean isForward) {
        if (isForward) {
            calendar.set(Calendar.HOUR_OF_DAY, 24);
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, -24);
        }
        return calendar.getTime();
    }


    private void getContent() {

        p_bar = ProgressDialog.show(getActivity(), getResources().getString(R.string.app_name), "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);

        //Fetching records
        new Thread(new Runnable() {
            public void run() {
                if (isMonthData || list == null || list.size() == 0) {
                  //  list.removeAll(list);
                    //Fetching beans
                    bean = new SugarBean(getContext(), "ro_crew_work_order");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                    String dateString = sdf.format(Calendar.getInstance().getTime());
                    String mainWhere = "(" + bean.moduleName.toLowerCase() + ".system_job_type = '" + UserPreferences.PREFS_SYSTEM_TYPE + "'" + " AND " +bean.moduleName.toLowerCase() + ".crew_work_id = '" + UserPreferences.userID + "'" + " AND " + bean.moduleName.toLowerCase() + ".date_start LIKE '%" + dateString + "%'" + ")";

                    try {
                        String whr = where;
                        if (mainWhere != null && mainWhere.length() > 0) {
                            if (whr != null && where.length() > 0) {
                                whr += " AND ";
                            } else {
                                whr = "";
                            }

                            whr += mainWhere;
                        }

                        SugarBean[] newBeans;
                        beans = new SugarBean[0];

                        if (NetworkHelper.isAvailable(getContext())) {
                            SugarBean.loadCom(getContext(), true, false);
                            if (NormalSync.loadFromServer()) {
                                newBeans = bean.retrieveAll(whr, orderByField + " " + orderByDir, offset, 50, 0, null);
                                if (newBeans.length > 0) {
                                    beans = newBeans;
                                } else {
                                    beans = new SugarBean[0];
                                }
                            }
                        } else {
                            SugarBean.loadCom(getContext(), false, true);
                            newBeans = bean.retrieveAll(whr, orderByField + " " + orderByDir, offset, 50, 0, null);
                            if (newBeans.length > 0) {
                                beans = newBeans;
                            } else {
                                beans = new SugarBean[0];
                            }
                        }
                        Calendar calendar = Calendar.getInstance();
                        ArrayList<Date> markedDates = new ArrayList<Date>();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        for (SugarBean bean : beans) {
                            String estimateDate = bean.getFieldValue("date_start");
                            markedDates.add(formatter.parse(estimateDate));
                        }

                        for (Date date : markedDates) {
                            calendar.setTime(date);
                            CalendarDay calendarDay = CalendarDay.from(calendar);
                            list.add(calendarDay);
                        }

                    } catch (Exception e) {
                        Log.e("Exception: ", e.getMessage());
                    }
                }

                bean = new SugarBean(getContext(), "ro_crew_work_order");
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

                    if (NetworkHelper.isAvailable(getContext())) {
                        SugarBean.loadCom(getContext(), true, false);
                        if (NormalSync.loadFromServer()) {
                            newBeans = bean.retrieveAll(whr, orderByField + " " + orderByDir, offset, 50, 0, null);
                            if (newBeans.length > 0) {
                                beans = newBeans;
                            } else {
                                beans = new SugarBean[0];
                            }
                        }
                    } else {
                        SugarBean.loadCom(getContext(), false, true);
                        newBeans = bean.retrieveAll(whr, orderByField + " " + orderByDir, offset, 50, 0, null);
                        if (newBeans.length > 0) {
                            beans = newBeans;
                        } else {
                            beans = new SugarBean[0];
                        }
                    }

                } catch (Exception e) {
//                    Log.e("Exception: ", e.getMessage());
                    beans = new SugarBean[0];
                }

                if (beans == null) beans = new SugarBean[0];

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing()) {
                            p_bar.hide();
                        }

                        populateJobs();

                        materialCalendarView.addDecorators(new EventDecorator(getResources().getColor(R.color.opaque_red), list));

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

            if (!isUpdated) {
                adapter = new JobsListAdapter(getContext(), jobs, R.layout.job_list_item);
                jobs_lv.setAdapter(adapter);
            } else {
                adapter.updateReceiptsList(jobs);
            }
        } else {
            jobs = new ArrayList();
            if (!isUpdated) {
                adapter = new JobsListAdapter(getContext(), jobs, R.layout.job_list_item);
                jobs_lv.setAdapter(adapter);
            } else {
                adapter.updateReceiptsList(jobs);
            }
        }

        setListViewHeightBasedOnChildren(jobs_lv);
        fetchingData = false;
        isMonthData = false;
    }
    //
    // 
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

        mainActivity.setTitle("Crew Daily Schedule");
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
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        list.clear();
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
