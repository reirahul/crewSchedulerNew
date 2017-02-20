package com.iconsolutions.crewschedular;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import java.lang.reflect.Field;

import crewschedular.fragmentinterface.BaseBackPressedListener;

/**
 * Created by kashif on 3/24/16.
 */
public class SettingsFragment extends Fragment implements OnClickListener{

    View view;
    FragmentActivity fm;
    ProgressDialog p_bar;

    Button import_database, sync_database, save_settings;
    String title = "Settings";

    public SettingsFragment(){
        this.fm = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup v, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_settings, v, false);
        initUI();

        return view;
    }

    public void initUI(){
        import_database = (Button) view.findViewById(R.id.import_database_button);
        sync_database = (Button) view.findViewById(R.id.sync_database_button);
        save_settings = (Button) view.findViewById(R.id.save_settings_button);

        import_database.setOnClickListener(this);
        sync_database.setOnClickListener(this);
        save_settings.setOnClickListener(this);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(this.fm != null)
        {
            ((MainActivity) this.fm)
                    .setOnBackPressedListener(new BaseBackPressedListener(this.fm));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id)
        {
            case R.id.import_database_button:
                importDatabaseAction();
                break;
            case R.id.sync_database_button:
                syncDatabaseAction();
                break;
            case R.id.save_settings_button:
                saveSettings();
                break;
            default:
                break;
        }
    }

    public void importDatabaseAction() {

    }

    public void syncDatabaseAction(){

    }

    public void saveSettings(){
        rolustech.beans.UserPreferences.mode = 0;

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
