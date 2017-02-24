package com.iconsolutions.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.iconsolutions.crewschedular.MainActivity;
import com.iconsolutions.crewschedular.R;
import com.iconsolutions.crewschedular.WorkOrderFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import rolustech.beans.SugarBean;
import com.iconsolutions.helper.UserPreferences;
import rolustech.communication.soap.SOAPClient;


/**
 * Created by kashif on 3/29/16.
 */
public class JobsListAdapter extends BaseAdapter {
    ArrayList<SugarBean> data;
    Context context;
    int resourceId;
    boolean isOldJobsOpen = false;
    ProgressDialog p_bar;
    Handler handler = new Handler();

    private SOAPClient soapClient;

    public JobsListAdapter(Context context, ArrayList<SugarBean> beansList, int resourceID) {
        this.context = context;
        this.data = beansList;
        this.resourceId = resourceID;
        UserPreferences.reLoadPrefernces(context);
    }

    @Override
    public int getCount() {
        return this.data.size();
    }

    @Override
    public Object getItem(int position) {
        return this.data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

//        try {
//            View view;

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resourceId, parent, false);
//                view = convertView;
            holder = new ViewHolder();
            holder.jobName = (TextView) convertView.findViewById(R.id.job_title);
            holder.updateStatusBtn = (Button) convertView.findViewById(R.id.update_button);
            convertView.setTag(holder);
        } else {
//                view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }

        if (this.data != null) {
            final SugarBean object = this.data.get(position);
            String status = "";
            holder.updateStatusBtn.setEnabled(true);
            holder.updateStatusBtn.setAlpha(1.0f);
           if (object.getFieldValue("status").equalsIgnoreCase("All Task Completed")) {
                status = "Complete";
                holder.updateStatusBtn.setEnabled(false);
                holder.updateStatusBtn.setAlpha(.5f);
            }
            else
           {
               status = "Incomplete";
           }
            holder.jobName.setText(object.getFieldValue("name") + "   -   " + status + "   -   " + dateForDisplay(object.getFieldValue("date_start")));
            holder.updateStatusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity mainActivity = (MainActivity) context;
                    Fragment fragment = new WorkOrderFragment();
                    Bundle args = new Bundle();
                    args.putString("SalesOrderId", object.getFieldValue("id"));
                    args.putString("SalesOrderNumber", object.getFieldValue("number"));
                    args.putString("SalesOrderType", object.getFieldValue("resi_order_type_c"));
                    args.putString("Contractor", object.getFieldValue("contractor_c"));
//                    args.putString("JobID", object.getFieldValue("crew_work_id"));
                    args.putString("JobName", object.getFieldValue("name"));

                    fragment.setArguments(args);
                    mainActivity.switchContent(fragment);
                }
            });

            if (position % 2 == 0) {
                convertView.setBackgroundColor(this.context.getResources().getColor(R.color.list_bg1));

            } else {
                convertView.setBackgroundColor(this.context.getResources().getColor(R.color.list_bg2));
            }
        }
//        } catch (Exception e) {
//
//        }

        return convertView;

    }

    public void updateBtnPressed() {

    }

    public void updateReceiptsList(ArrayList<SugarBean> newlist) {
        data.clear();
        data.addAll(newlist);
        this.notifyDataSetChanged();
    }

//    @Override
//    public void notifyDataSetChanged() {
//        super.notifyDataSetChanged();
//    }

    static class ViewHolder {
        TextView jobName;
        Button updateStatusBtn;
    }

    public static String dateForDisplay(String estDate) {
        if (estDate.equals(""))
            return "";

        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = formatter1.parse(estDate);
            SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy");
            String dispDate = formatter.format(date);
            return dispDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";

    }
}
