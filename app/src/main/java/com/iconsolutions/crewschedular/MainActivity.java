package com.iconsolutions.crewschedular;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iconsolutions.menuhelper.SampleListFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.io.File;

import crewschedular.fragmentinterface.OnBackPressedListener;
import rolustech.beans.UserPreferences;
import rolustech.helper.ImportHelper;

import static com.iconsolutions.menuhelper.SampleListFragment.deleteRecursive;

/**
 * Created by kashif on 3/22/16.
 */
public class MainActivity extends FragmentActivity implements OnClickListener {

    LinearLayout menu_btn;
    TextView title;

    private SlidingMenu menu;
    protected OnBackPressedListener onBackPressedListener;

    public static FragmentManager fManager;
    Fragment sampleListFragment, crewJobsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.INTERNET};
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        setSlideMenu();

        crewJobsFragment = new CrewJobsListFragment();
        switchContent(crewJobsFragment);

        initUI();

//        fManager = getSupportFragmentManager();
//
//        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
//
//            @Override
//            public void onBackStackChanged() {
//                FragmentManager manager = getSupportFragmentManager();
//                if (manager != null)
//                {
//                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
//                        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_frame);
//                        if (f instanceof CrewJobsListFragment) {
//                            setTitle("Crew Daily Schedule");
//                        }
//                        else if (f instanceof CalendarFragment) {
//                            setTitle("Calendar");
//                        }
//
//                    }
//                    else{
//                        setTitle("Crew Daily Schedule");
//                    }
//                }
//
//            }
//        });

    }

    private void initUI() {
        menu_btn = (LinearLayout) findViewById(R.id.menu_btn);
        menu_btn.setOnClickListener(this);

        title = (TextView) findViewById(R.id.header_title);
    }

    private void setSlideMenu() {
//        getActionBar().hide();
        menu = new SlidingMenu(this);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
//		menu.setShadowWidthRes(R.dimen.shadow_width);
//		menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
//		menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.menu_frame);
        menu.setMode(SlidingMenu.RIGHT);
        setSampleListFragment();
    }

    public void setSampleListFragment() {
        sampleListFragment = new SampleListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.menu_frame, sampleListFragment)
                .commit();
    }

    public void setTitle(String fragmentTitle) {
        if (title != null)
            title.setText(Html.fromHtml(fragmentTitle));
    }

    private void toggleSlideMenu() {
        if (menu.isMenuShowing()) {
            menu.showContent();
        } else {
            menu.showMenu();
        }
    }

    public void switchContent(Fragment fragment) {
        if (this.menu != null && menu.isMenuShowing()) {
            menu.showContent();
        }
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment, "MY_FRAGMENT").commit();
        }
    }

    public void addContent(Fragment fragment) {
        if (this.menu != null && menu.isMenuShowing()) {
            menu.showContent();
        }
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, fragment, "MY_FRAGMENT").addToBackStack(null).commit();
        }
    }

    /*public void switchTabContent(Fragment fragment) {
        if(fragment != null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tab_layout, fragment, "MY_FRAGMENT").commit();
        }
    }*/

    @Override
    public void onBackPressed() {
        if (menu.isMenuShowing()) {
            menu.showContent();
        }
        if (onBackPressedListener != null) {
            Fragment fragment = null;

            Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (f instanceof CrewJobsListFragment) {
                if (sampleListFragment != null && getSupportFragmentManager().findFragmentById(sampleListFragment.getId()) != null) {
                    getSupportFragmentManager().beginTransaction().remove(sampleListFragment)
                            .commit();
                    sampleListFragment = null;
                }
                if (crewJobsFragment != null && getSupportFragmentManager().findFragmentById(crewJobsFragment.getId()) != null) {
                    getSupportFragmentManager().beginTransaction().remove(crewJobsFragment)
                            .commit();
                    crewJobsFragment = null;
                }

                this.finish();
            } else if (f instanceof CalendarFragment || f instanceof FullMap) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        Fragment currFrag = (Fragment) getSupportFragmentManager().
                                findFragmentByTag("MY_FRAGMENT");
                        if (currFrag != null)
                            currFrag.onResume();
                    }

                }, 250);
            } else if (f instanceof WorkOrderFragment) {
//                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragment = new CrewJobsListFragment();
                switchContent(fragment);
                setTitle("Crew Daily Schedule");
            } else {
                super.onBackPressed();
            }
        } else {

            super.onBackPressed();
        }
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.menu_btn:
                View focus = getCurrentFocus();
                if (focus != null) {
                    hiddenKeyboard(v);
                }
                toggleSlideMenu();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if (sampleListFragment != null && getSupportFragmentManager().findFragmentById(sampleListFragment.getId()) != null) {
//            getSupportFragmentManager().beginTransaction().remove(sampleListFragment)
//                    .commit();
//            sampleListFragment = null;
//        }
//        if (crewJobsFragment != null && getSupportFragmentManager().findFragmentById(crewJobsFragment.getId()) != null) {
//            getSupportFragmentManager().beginTransaction().remove(crewJobsFragment)
//                    .commit();
//            crewJobsFragment = null;
/*        }
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + UserPreferences.APP_NAME);
        final File to = new File(dir.getAbsolutePath() + System.currentTimeMillis());
        dir.renameTo(to);
        deleteRecursive(to);
        File dir2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rSugarCRM");
        final File to1 = new File(dir2.getAbsolutePath() + System.currentTimeMillis());
        dir2.renameTo(to1);
        deleteRecursive(to1);
        super.onDestroy();
*/
    }

    private void hiddenKeyboard(View v) {
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    @Override
    protected void onPause() {

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (fragment instanceof WorkOrderFragment) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
                break;

            case 100:
                Fragment fragment1 = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (fragment1 instanceof WorkOrderFragment) {
                    fragment1.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
