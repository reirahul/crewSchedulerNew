package com.iconsolutions.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.iconsolutions.crewschedular.JobDetailsHomeFragment;
import com.iconsolutions.crewschedular.MainActivity;
import com.iconsolutions.crewschedular.R;
import com.iconsolutions.helper.UserPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import rolustech.beans.SugarBean;
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
            holder.jobNo = (TextView) convertView.findViewById(R.id.job_no);
            holder.jobName = (TextView) convertView.findViewById(R.id.job_title);
            holder.jobBuilder = (TextView) convertView.findViewById(R.id.job_builder);
            holder.jobCity = (TextView) convertView.findViewById(R.id.job_city);
//            holder.jobDate = (TextView) convertView.findViewById(R.id.job_date);
            holder.jobStatus = (TextView) convertView.findViewById(R.id.job_status);
            holder.updateStatusBtn = (TextView) convertView.findViewById(R.id.update_button);
            convertView.setTag(holder);
        } else {
//          view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }

        if (this.data != null) {
            final SugarBean object = this.data.get(position);
/*
            ArrayList<String> bf = new ArrayList<>();
            ArrayList<String> bf1 = new ArrayList<>();
            StringBuffer finalData = new StringBuffer();
            for (String value : object.getNameArray(true))
                bf.add(value);
            for (String value : object.getValueArray(true))
                bf1.add(value);
            for (int i = 0; i < bf.size(); i++)
                finalData.append(bf.get(i) + " = " + bf1.get(i) + ",");
            Log.d("Crew_App", "Responce of Server= " + finalData.toString());
*/
            String status = "";
            holder.updateStatusBtn.setEnabled(true);
            holder.updateStatusBtn.setAlpha(1.0f);
           if (object.getFieldValue("status").equalsIgnoreCase("All Task Completed")) {
                status = "Completed";
//                holder.updateStatusBtn.setEnabled(false);
                holder.updateStatusBtn.setAlpha(.5f);
            }
            else
           {
               status = "Incompleted";
           }
            holder.jobNo.setText(object.getFieldValue("number"));
            holder.jobName.setText(object.getFieldValue("name"));
            holder.jobCity.setText(object.getFieldValue("work_location"));
            holder.jobBuilder.setText(object.getFieldValue("jobs_account_builder_name"));
            holder.jobStatus.setText(status );
 //          holder.jobDate.setText( dateForDisplay(object.getFieldValue("date_start")));
            holder.updateStatusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity mainActivity = (MainActivity) context;
                    Fragment fragment = new JobDetailsHomeFragment();
                    Bundle args = new Bundle();
                    args.putString("SalesOrderId", object.getFieldValue("id"));
                    args.putString("SalesOrderNumber", object.getFieldValue("number"));
                    args.putString("SalesOrderType", object.getFieldValue("resi_order_type_c"));
                    args.putString("Contractor", object.getFieldValue("contractor_c"));
//                  args.putString("JobID", object.getFieldValue("crew_work_id"));
                    args.putString("JobName", object.getFieldValue("name"));
                    fragment.setArguments(args);
                    mainActivity.switchContent(fragment);
                }
            });
            Log.d("JobsListAdaptor","Crew_App Location => "+object.getFieldValue("work_location"));

 /*           if (position % 2 == 0) {
                convertView.setBackgroundColor(this.context.getResources().getColor(R.color.list_bg1));

            } else {
                convertView.setBackgroundColor(this.context.getResources().getColor(R.color.list_bg2));
            }
            */
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
        TextView jobName,jobStatus,jobBuilder,jobCity,jobNo,viewDetails,receivePO,updateStatusBtn;
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
