package com.iconsolutions.crewschedular;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.iconsolutions.crewschedular.SampleListAdapter.*;
import com.iconsolutions.menuhelper.SampleListFragment;

/**
 * Created by kashif on 5/25/16.
 */

public class SampleListAdapter extends ArrayAdapter<SampleListFragment.SampleItem> {

    public SampleListAdapter(Context context) {
        super(context, 0);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_cell_item, null);
        }

        TextView title = (TextView) convertView.findViewById(R.id.row_title);
        title.setText(getItem(position).tag);


        return convertView;
    }


}