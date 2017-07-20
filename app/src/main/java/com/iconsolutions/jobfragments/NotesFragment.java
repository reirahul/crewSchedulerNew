package com.iconsolutions.jobfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iconsolutions.crewschedular.R;
//import java.lang.reflect.Field;

/**
 * Created by kashif on 4/8/16.
 */
public class NotesFragment extends Fragment {

    public NotesFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup v, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_notes, null);

        return view;
    }
}