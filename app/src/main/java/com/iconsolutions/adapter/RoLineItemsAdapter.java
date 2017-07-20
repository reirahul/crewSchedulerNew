package com.iconsolutions.adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iconsolutions.crewschedular.R;

import java.util.ArrayList;

import rolustech.beans.SugarBean;
import rolustech.communication.soap.SOAPClient;

/**
 * Created by kashif on 4/7/16.
 */
public class RoLineItemsAdapter extends BaseAdapter {
    ArrayList<SugarBean> data;
    ArrayList<SugarBean> datas;
    Context context;
    int resourceId,tmp=0;
    ViewHolder holder;
    View view;
    EditText selectedET;

    ProgressDialog p_bar;
    Handler handler = new Handler();

    private SOAPClient soapClient;
/*
    public RoLineItemsAdapter(Context context, ArrayList<SugarBean> beansList, int resourceID) {
        this.context = context;
        this.data = beansList;
        this.resourceId = resourceID;
        UserPreferences.reLoadPrefernces(context);
    }
*/
    public RoLineItemsAdapter(Context context, ArrayList<SugarBean> datas, int resourceId) {
        this.context=context;
        this.datas=datas;
        this.resourceId = resourceId;
    }

    @Override
    public int getCount() {
        return this.datas.size();
    }

    @Override
    public Object getItem(int position) {
        return this.datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        SugarBean data = datas.get(position);
       if (convertView == null) {
           LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
           convertView = inflater.inflate(resourceId, parent, false);
           view = convertView;
           initUI();
       }
       else {
           holder = (ViewHolder) view.getTag();
       }
 //       Log.d("ROLineItemsAdaptor","Crew_App Data  => "+ data.getNameArray(false));
          hideTitle(position);
        String batchStatus = data.getFieldValue("is_approved").equals("0")?"N/A":"Approved";
                holder.lineItemName.setText(data.getFieldValue("name"));
                 holder.installedQty.setText(data.getFieldValue("installed_qty"));
                 holder.batchStatus.setText(batchStatus);
                 holder.batchNo.setText(data.getFieldValue("batch_number"));
                 holder.date.setText(data.getFieldValue("date_entered"));
                 holder.crewName.setText("Renecrew");
                 holder.totalMan.setText(data.getFieldValue("totalmen"));
                 holder.startTime.setText(data.getFieldValue("start_time"));
                 holder.endTime.setText(data.getFieldValue("end_time"));

        return view;
    }
    private void hideTitle(int position)
    {
        if (position == 0) {
            holder.mainLayout.getLayoutParams().height = convertDPToPx(90);
                holder.title1.setVisibility(View.VISIBLE);
                holder.title2.setVisibility(View.VISIBLE);
                holder.title3.setVisibility(View.VISIBLE);
                holder.title4.setVisibility(View.VISIBLE);
                holder.title5.setVisibility(View.VISIBLE);
                holder.title6.setVisibility(View.VISIBLE);
                holder.title7.setVisibility(View.VISIBLE);
                holder.title8.setVisibility(View.VISIBLE);
                holder.title9.setVisibility(View.VISIBLE);
//            holder.title1.getLayoutParams().height = convertDPToPx(45);
//            holder.title2.getLayoutParams().height = convertDPToPx(45);
//            holder.title3.getLayoutParams().height = convertDPToPx(45);
//            holder.title4.getLayoutParams().height = convertDPToPx(45);
//            holder.title5.getLayoutParams().height = convertDPToPx(45);
//            holder.title6.getLayoutParams().height = convertDPToPx(45);
//            holder.title7.getLayoutParams().height = convertDPToPx(45);
//            holder.title8.getLayoutParams().height = convertDPToPx(45);
//            holder.title9.getLayoutParams().height = convertDPToPx(45);

        } else {
            holder.mainLayout.getLayoutParams().height = convertDPToPx(45);
            holder.title1.setVisibility(View.GONE);
            holder.title2.setVisibility(View.GONE);
            holder.title3.setVisibility(View.GONE);
            holder.title4.setVisibility(View.GONE);
            holder.title5.setVisibility(View.GONE);
            holder.title6.setVisibility(View.GONE);
            holder.title7.setVisibility(View.GONE);
            holder.title8.setVisibility(View.GONE);
            holder.title9.setVisibility(View.GONE);

        }


        if (position % 2 == 0) {
            holder.mainLayout.setBackgroundColor(this.context.getResources().getColor(R.color.list_bg1));

        } else {
            holder.mainLayout.setBackgroundColor(this.context.getResources().getColor(R.color.list_bg2));
        }

    }

    public static int convertDPToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }



    public static double parseDoubleOrNull(String str) {
        return !str.equals("") ? Double.parseDouble(str) : 0;
    }

    public static class ViewHolder {
        TextView title1, title2, title3, title4, title5, title6, title7, title8, title9;
        TextView lineItemName,installedQty, crewName, batchStatus, totalMan,startTime,endTime,batchNo,date;
        LinearLayout mainLayout;

    }

    private void showDialog(String desc) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_notes);

        final TextView description = (TextView) dialog.findViewById(R.id.notes_description);
        final TextView dismiss = (TextView) dialog.findViewById(R.id.remove_dialog_btn);

        description.setText(desc);

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }
    private void initUI()
    {
        holder = new ViewHolder();
        holder.lineItemName = (TextView) view.findViewById(R.id.lineItemText);
        holder.installedQty = (TextView) view.findViewById(R.id.installedQtyText);
        holder.batchStatus = (TextView) view.findViewById(R.id.batchStatusText);
        holder.batchNo = (TextView) view.findViewById(R.id.batchNoText);
        holder.date = (TextView) view.findViewById(R.id.dateText);
        holder.crewName = (TextView) view.findViewById(R.id.crewNameText);
        holder.totalMan = (TextView) view.findViewById(R.id.noOfManText);
        holder.startTime = (TextView) view.findViewById(R.id.startTimeText);
        holder.endTime = (TextView) view.findViewById(R.id.endTimeText);


        holder.title1 = (TextView) view.findViewById(R.id.lineItemTitle);
        holder.title2 = (TextView) view.findViewById(R.id.installedQtyTitle);
        holder.title3 = (TextView) view.findViewById(R.id.batchStatusTitle);
        holder.title4 = (TextView) view.findViewById(R.id.batchNoTitle);
        holder.title5 = (TextView) view.findViewById(R.id.dateTitle);
        holder.title6 = (TextView) view.findViewById(R.id.crewNameTitle);
        holder.title7 = (TextView) view.findViewById(R.id.noOfManTitle);
        holder.title8 = (TextView) view.findViewById(R.id.startTimeTitle);
        holder.title9 = (TextView) view.findViewById(R.id.endTimeTitle);
        holder.mainLayout = (LinearLayout) view.findViewById(R.id.linitem_heading);
        view.setTag(holder);

    }
}
