package com.iconsolutions.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iconsolutions.crewschedular.R;
import com.iconsolutions.helper.UserPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import rolustech.beans.SugarBean;
import rolustech.communication.soap.SOAPClient;
import rolustech.helper.AlertHelper;
import rolustech.helper.NetworkHelper;

import static com.iconsolutions.crewschedular.R.drawable.start_time;
import static com.iconsolutions.crewschedular.R.drawable.stop_time;

/**
 * Created by kashif on 4/7/16.
 */
public class LineItemsAdapter extends BaseAdapter {
    ArrayList<SugarBean> data;
    Context context;
    int resourceId,tmp=0,dueItem,totalItem,preInstalItem,preResvItem;

    EditText selectedET;

    ProgressDialog p_bar;
    Handler handler = new Handler();

    private SOAPClient soapClient;

    public LineItemsAdapter(Context context, ArrayList<SugarBean> beansList, int resourceID) {
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
//        try {
//    View view;
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resourceId, parent, false);
//        view = convertView;

            holder = new ViewHolder();

            holder.lineItemName = (TextView) convertView.findViewById(R.id.product_text);
            holder.qunatity = (TextView) convertView.findViewById(R.id.qty_text);
//            holder.notes = (TextView) convertView.findViewById(R.id.notes_text);
            holder.installedQty = (EditText) convertView.findViewById(R.id.installed_qty_text);
            holder.deliveredQty = (TextView) convertView.findViewById(R.id.delqty_text);
            holder.prevInstalledQty = (TextView) convertView.findViewById(R.id.prev_inst_qty_text);
            holder.dueItem = (EditText) convertView.findViewById(R.id.due_item);
            holder.additionalQty = (EditText) convertView.findViewById(R.id.additional_qty_lineitem);
            holder.resvQty = (TextView) convertView.findViewById(R.id.resv_qty_text);
            holder.additionalQty = (TextView) convertView.findViewById(R.id.co_qty_lineitem);
            holder.prevResvQty = (TextView) convertView.findViewById(R.id.prev_resv_qty_text);
            holder.startStopTime = (ImageView) convertView.findViewById(R.id.start_stop_clock);
            holder.notesImage = (ImageView) convertView.findViewById(R.id.notesImage);


            holder.title1 = (TextView) convertView.findViewById(R.id.qtyTitle);
            holder.title2 = (TextView) convertView.findViewById(R.id.productTitle);
            holder.title3 = (TextView) convertView.findViewById(R.id.deliveredQtyTitle);
            holder.title4 = (TextView) convertView.findViewById(R.id.instQtyTitle);
            holder.title5 = (TextView) convertView.findViewById(R.id.prevInstQtyTitle);
            holder.title6 = (TextView) convertView.findViewById(R.id.adtQtyTitle);
            holder.title7 = (TextView) convertView.findViewById(R.id.dueItemTitle);
            holder.title8 = (TextView) convertView.findViewById(R.id.timeTitle);
            holder.title9 = (TextView) convertView.findViewById(R.id.notesTitle);
            holder.title10 = (TextView) convertView.findViewById(R.id.resvQtyTitle);
            holder.title11 = (TextView) convertView.findViewById(R.id.prevResvQtyTitle);

            holder.mainLayout = (LinearLayout) convertView.findViewById(R.id.linitem_heading);

            convertView.setTag(holder);
        } else {
//        view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }

        final ViewHolder finalHolder = holder;
        if (this.data != null) {
            final SugarBean object = this.data.get(position);
                AlertHelper.printBeans(object);
             try {

                holder.lineItemName.setText(object.getFieldValue("name"));
//                holder.notes.setText(object.getFieldValue("description"));
                String quntity = String.valueOf((int) parseDoubleOrNull(object.getFieldValue("product_qty")));
                holder.installedQty.setText("0");
                holder.qunatity.setText(quntity);
                if (quntity.equalsIgnoreCase("") || quntity.equalsIgnoreCase("0")) {

                    holder.installedQty.setEnabled(false);
                    holder.installedQty.setClickable(false);

                }else{
                    holder.installedQty.setEnabled(true);
                    holder.installedQty.setClickable(true);
                }
//                    holder.installedQty.setText(String.valueOf((int) parseDoubleOrNull(object.getFieldValue("app_installed_qty"))));

                holder.prevInstalledQty.setText(String.valueOf((int) parseDoubleOrNull(object.getFieldValue("app_prev_installed_qty"))));
                preInstalItem = (int) parseDoubleOrNull(object.getFieldValue("app_prev_installed_qty"));
                totalItem =   (int) parseDoubleOrNull(object.getFieldValue("product_qty"));
                dueItem = totalItem - preInstalItem;
//                holder.dueItem.setText(String.valueOf((int) parseDoubleOrNull(object.getFieldValue("app_due_item"))));
                holder.dueItem.setText(String.valueOf(dueItem));
                holder.additionalQty.setText(object.getFieldValue("app_additional_qty"));
                holder.deliveredQty.setText(object.getFieldValue("app_delivered_qty"));
                 holder.prevResvQty.setText(object.getFieldValue("app_previous_received_qty"));
                 holder.deliveredQty.setText(object.getFieldValue("app_delivered_qty"));

            } catch (Exception e) {
//                Log.d("Exception", e.getMessage());
            }

            holder.additionalQty.setTag(position);
            holder.additionalQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        if (finalHolder.additionalQty.getText().toString().equals("0"))
                            finalHolder.additionalQty.setText("");
                    }
                    final int tag = (int) view.getTag();
                    final String quantity = finalHolder.additionalQty.getText().toString();

                    if (!quantity.equals("0") && !quantity.equals("") && !(quantity.equalsIgnoreCase(object.getFieldValue("app_additional_qty")))) {

                        android.app.AlertDialog diag = new android.app.AlertDialog.Builder(context).create();
                        diag.setCancelable(true);
                        diag.setMessage("Are you sure to request for additional quantity?");
                        diag.setTitle(UserPreferences.APP_NAME);
                        diag.setInverseBackgroundForced(true);
                        diag.setButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestQuantity(tag, quantity);
                                dialog.dismiss();
                            }
                        });
                        diag.setButton2("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        diag.show();

                    }
                }
            });

            try {
                ArrayList<String> startTimeArr = new ArrayList<String>();
                if (!(object.getFieldValue("start_time").equals("")))
                    startTimeArr = convertJSONToArray(new JSONArray(object.getFieldValue("start_time")));

                ArrayList<String> stopTimeArr = new ArrayList<String>();
                if (!(object.getFieldValue("stop_time").equals("")))
                    stopTimeArr = convertJSONToArray(new JSONArray(object.getFieldValue("stop_time")));

                if (startTimeArr.size() == stopTimeArr.size())
                    holder.startStopTime.setImageResource(start_time);
                else
                    holder.startStopTime.setImageResource(stop_time);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            holder.startStopTime.setTag(position);
            holder.startStopTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int tag = (int) v.getTag();
                    try {
                        ArrayList<String> startTimeArr = new ArrayList<String>();
                        if (!(object.getFieldValue("start_time").equals("")))
                            startTimeArr = convertJSONToArray(new JSONArray(object.getFieldValue("start_time")));
                        Log.d("LineItemsAdaptor","Crew_App Start Responce -> "+startTimeArr.toString());
                        ArrayList<String> stopTimeArr = new ArrayList<String>();
                        if (!(object.getFieldValue("stop_time").equals("")))
                            stopTimeArr = convertJSONToArray(new JSONArray(object.getFieldValue("stop_time")));
                        Log.d("LineItemsAdaptor","Crew_App Stop Responce -> "+stopTimeArr.toString());

                        saveStartStopTime(position, (ImageView) v, startTimeArr, stopTimeArr);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });

            final int preReceivedQty=(int) parseDoubleOrNull(holder.prevResvQty.getText().toString().toString());

            holder.installedQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        selectedET = null;
                        EditText ed = (EditText) v;
                        int instqty=(int) parseDoubleOrNull(ed.getText().toString().toString());
                        int receivedQty=(int) parseDoubleOrNull(holder.resvQty.getText().toString().toString());
                        int totalQty = (int) parseDoubleOrNull(ed.getText().toString().toString()) + (int) parseDoubleOrNull(object.getFieldValue("app_prev_installed_qty"));
 //                       String product_qty = object.getFieldValue("product_qty");
                        if (totalQty <= (int) parseDoubleOrNull(object.getFieldValue("product_qty")) && instqty <= ((preReceivedQty+receivedQty)-(int) parseDoubleOrNull(object.getFieldValue("app_prev_installed_qty")))) {  }
                        else {
                            AlertHelper.showAlert((FragmentActivity) context, UserPreferences.APP_NAME, "Installed quantity must be less than or equal to total quantity");
                            ed.setText("0");
//                            holder.resvQty.setText("0");
                        }

                        Log.i(UserPreferences.APP_NAME, "DONE pressed");

                    }
                }
            });

            holder.resvQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    EditText ed = (EditText) view;
                    int totalQty = (int)parseDoubleOrNull(object.getFieldValue("product_qty"));
                    if ((int)parseDoubleOrNull(holder.qunatity.getText().toString()) >= preReceivedQty+(int)parseDoubleOrNull(ed.getText().toString())) {  }
                    else {
                        AlertHelper.showAlert((FragmentActivity) context, UserPreferences.APP_NAME, "Previews Received + Received  quantity must be less than or equal to Total quantity");
                        ed.setText("0");
                    }

                    Log.i(UserPreferences.APP_NAME, "DONE pressed");
                }
            });

/*
            holder.installedQty.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        EditText ed = (EditText)view;
                        int totalQty = Integer.parseInt(ed.getText().toString()) + (int) parseDoubleOrNull(object.getFieldValue("app_prev_installed_qty"));
                        String product_qty = object.getFieldValue("product_qty");

                        if (totalQty <= (int) (Double.parseDouble(object.getFieldValue("product_qty")))) {

                            if (!ed.getText().toString().equals("0") && !ed.getText().toString().equals("") && !(ed.getText().toString().equalsIgnoreCase(object.getFieldValue("app_installed_qty")))) {
                                saveInstalledQty(position, (EditText)ed);
                                tmp=1;
                            }

                        } else {
                            AlertHelper.showAlert((FragmentActivity) context, UserPreferences.APP_NAME, "Installed quantity must be less than or equal to total quantity");
                        }
                        Log.i(UserPreferences.APP_NAME, "DONE pressed");
                        return true;
                    }
                    return false;
                }
            });

            holder.installedQty.setOnEditorActionListener(
                    new EditText.OnEditorActionListener() {
                        public boolean onEditorAction(TextView ed, int actionId, KeyEvent event) {
                            if(EditorInfo.IME_ACTION_DONE==actionId || EditorInfo.IME_ACTION_UNSPECIFIED==actionId)
                            {
                                int totalQty = Integer.parseInt(ed.getText().toString()) + (int) parseDoubleOrNull(object.getFieldValue("app_prev_installed_qty"));
                                String product_qty = object.getFieldValue("product_qty");

                                if (totalQty <= (int) (Double.parseDouble(object.getFieldValue("product_qty")))) {

                                    if (!ed.getText().toString().equals("0") && !ed.getText().toString().equals("") && !(ed.getText().toString().equalsIgnoreCase(object.getFieldValue("app_installed_qty")))) {
                                        saveInstalledQty(position, (EditText)ed);
                                    }

                                } else {
                                    AlertHelper.showAlert((FragmentActivity) context, UserPreferences.APP_NAME, "Installed quantity must be less than or equal to total quantity");
                                }
                                Log.i(UserPreferences.APP_NAME, "DONE pressed");
                                return true;
                            }
                    return false;
                }
            });
*/

            holder.deliveredQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        if (finalHolder.deliveredQty.getText().toString().equals("0"))
                            finalHolder.deliveredQty.setText("");
                    }
                }
            });

            holder.notesImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String desc = object.getFieldValue("description");
                    if (desc != null && desc.length() > 0) {
                        showDialog(object.getFieldValue("description"));
                    }
                }
            });

            holder.deliveredQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        selectedET = null;
                        EditText ed = (EditText) v;
                        if (ed.getText().toString() != null && !ed.getText().toString().equals("")) {

                            if (!ed.getText().toString().equals("0") && !ed.getText().toString().equals("") && !(ed.getText().toString().equalsIgnoreCase(object.getFieldValue("app_delivered_qty")))) {
                                saveDeliveredQty(position, ed);
                            }
                        }
                    } else {
                        selectedET = (EditText) v;
                        if (finalHolder.deliveredQty.getText().toString().equals("0"))
                            finalHolder.deliveredQty.setText("");
                    }
                }
            });

            if (position == 0) {
                holder.mainLayout.getLayoutParams().height = convertDPToPx(90);
//                holder.title1.setVisibility(View.VISIBLE);
//                holder.title2.setVisibility(View.VISIBLE);
//                holder.title3.setVisibility(View.VISIBLE);
//                holder.title4.setVisibility(View.VISIBLE);
//                holder.title5.setVisibility(View.VISIBLE);
//                holder.title6.setVisibility(View.VISIBLE);
//                holder.title7.setVisibility(View.VISIBLE);
//                holder.title8.setVisibility(View.VISIBLE);
                holder.title1.getLayoutParams().height = convertDPToPx(45);
                holder.title2.getLayoutParams().height = convertDPToPx(45);
                holder.title3.getLayoutParams().height = convertDPToPx(45);
                holder.title4.getLayoutParams().height = convertDPToPx(45);
                holder.title5.getLayoutParams().height = convertDPToPx(45);
                holder.title6.getLayoutParams().height = convertDPToPx(45);
                holder.title7.getLayoutParams().height = convertDPToPx(45);
                holder.title8.getLayoutParams().height = convertDPToPx(45);
                holder.title9.getLayoutParams().height = convertDPToPx(45);
                holder.title10.getLayoutParams().height = convertDPToPx(45);
                holder.title11.getLayoutParams().height = convertDPToPx(45);

            } else {
                holder.mainLayout.getLayoutParams().height = convertDPToPx(45);
                holder.title1.getLayoutParams().height = 0;
                holder.title2.getLayoutParams().height = 0;
                holder.title3.getLayoutParams().height = 0;
                holder.title4.getLayoutParams().height = 0;
                holder.title5.getLayoutParams().height = 0;
                holder.title6.getLayoutParams().height = 0;
                holder.title7.getLayoutParams().height = 0;
                holder.title8.getLayoutParams().height = 0;
                holder.title9.getLayoutParams().height = 0;
                holder.title10.getLayoutParams().height = 0;
                holder.title11.getLayoutParams().height = 0;

            }


            if (position % 2 == 0) {
                holder.mainLayout.setBackgroundColor(this.context.getResources().getColor(R.color.list_bg1));

            } else {
                holder.mainLayout.setBackgroundColor(this.context.getResources().getColor(R.color.list_bg2));
            }

        }


        return convertView;
    }

    public static int convertDPToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public void updateReceiptsList(ArrayList<SugarBean> newlist) {
        data.clear();
        data.addAll(newlist);
        this.notifyDataSetChanged();
    }

    public static double parseDoubleOrNull(String str) {
        return !str.equals("") ? Double.parseDouble(str) : 0;
    }

    public static class ViewHolder {
        TextView title1, title2, title3, title4, title5, title6, title7, title8, title9,title10,title11;
        TextView lineItemName, qunatity, notes, prevInstalledQty,prevResvQty,resvQty,deliveredQty,additionalQty;
        EditText  dueItem,installedQty;
        ImageView  startStopTime, notesImage;
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

    public void saveStartStopTime(final int position, final ImageView v, final ArrayList<String> startTimeArr, final ArrayList<String> stopTimeArr) {

        p_bar = ProgressDialog.show(context, "Crew App", "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);
        final SugarBean object1 = this.data.get(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e("LineItemsAdaptor","Crew_App Responce -> Name = "+object1.getFieldValue("name")+", Id = " +object1.getFieldValue("id")+" ,Start time = "+object1.getFieldValue("start_time")+", Stop time = "+object1.getFieldValue("stop_time"));
                    long date1 = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String dateString = sdf.format(date1);

                    if (startTimeArr.size() == stopTimeArr.size()) {
                        startTimeArr.add(dateString);
                        JSONArray jArr = convertArrayListToJSON(startTimeArr);
                        object1.updateFieldValue("start_time", jArr.toString());
                    } else {
                        stopTimeArr.add(dateString);
                        JSONArray jArr = convertArrayListToJSON(stopTimeArr);
                        object1.updateFieldValue("stop_time", jArr.toString());
                    }
                    String response;
                    if (NetworkHelper.isAvailable(context)) {
                        SOAPClient com = new SOAPClient(UserPreferences.url);
                        response = object1.save(false);
                    } else {
                        response = object1.save(false);
                    }

                } catch (Exception e) {
                    Log.d("ERROR", e.getMessage());
                    AlertHelper.showAlert((Activity) context, "Error", e.getMessage());
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing()) {
                            p_bar.hide();
                        }
                        Toast.makeText(context, "Successfully Saved", Toast.LENGTH_SHORT).show();
                        data.set(position, object1);
                        notifyDataSetChanged();

                    }
                });

            }
        }).start();
    }


    public void saveInstalledQty(final int position, final EditText v) {

        p_bar = ProgressDialog.show(context, "Crew App", "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);
        final SugarBean object = data.get(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if (NetworkHelper.isAvailable(context)) {
                        SOAPClient com = new SOAPClient(UserPreferences.url);
                        object.updateFieldValue("app_installed_qty", "0");
                        int prevQty = (int) parseDoubleOrNull(v.getText().toString()) + (int) parseDoubleOrNull(object.getFieldValue("app_prev_installed_qty"));
                        object.updateFieldValue("app_prev_installed_qty", String.valueOf(prevQty));
                        String response = object.save(false);
                    } else {
                        SugarBean wo_bean = new SugarBean(context, "AOS_Products_Quotes");
                        wo_bean.updateFieldValue("id", object.getFieldValue("id"));
                        wo_bean.updateFieldValue("app_installed_qty", "0");
                        int prevQty = (int) parseDoubleOrNull(v.getText().toString()) + (int) parseDoubleOrNull(object.getFieldValue("app_prev_installed_qty"));
                        wo_bean.updateFieldValue("app_prev_installed_qty", String.valueOf(prevQty));
                        object.updateFieldValue("app_prev_installed_qty", String.valueOf(prevQty));
                        object.updateFieldValue("app_installed_qty", "0");
                        wo_bean.save(false);
                    }

                } catch (Exception e) {
//                    Log.d("ERROR", e.getMessage());
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing())
                        {
                            p_bar.dismiss();
                        }
                        Toast.makeText(context, "Successfully Saved", Toast.LENGTH_SHORT).show();
                        data.set(position, object);
                        notifyDataSetChanged();

                    }
                });

            }
        }).start();
    }


    public void saveDeliveredQty(final int position, final EditText v) {

        p_bar = ProgressDialog.show(context, "Crew App", "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);
        final SugarBean object = data.get(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if (NetworkHelper.isAvailable(context)) {
                        SOAPClient com = new SOAPClient(UserPreferences.url);
                        object.updateFieldValue("app_delivered_qty", v.getText().toString());
                        String response = object.save(false);
                    } else {
                        SugarBean wo_bean = new SugarBean(context, "AOS_Products_Quotes");
                        wo_bean.updateFieldValue("id", object.getFieldValue("id"));
                        wo_bean.updateFieldValue("app_delivered_qty", v.getText().toString());
                        object.updateFieldValue("app_delivered_qty", v.getText().toString());
                        wo_bean.save(false);
                    }

                } catch (Exception e) {
                     if(p_bar.isShowing())
                         p_bar.cancel();
                    Log.e("Crew ERROR", e.getMessage());
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing()) {
                            p_bar.cancel();
                        }

                        Toast.makeText(context, "Successfully Saved", Toast.LENGTH_SHORT).show();

                        data.set(position, object);
                        notifyDataSetChanged();

                    }
                });

            }
        }).start();
    }

    public JSONArray convertArrayListToJSON(ArrayList<String> list) {

        JSONArray jsArray = new JSONArray(list);
        return jsArray;
    }

    public ArrayList<String> convertJSONToArray(JSONArray jsonArray) {

        ArrayList<String> listdata = new ArrayList<String>();

        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    if (!jsonArray.get(i).toString().equals("")) {
                        listdata.add(jsonArray.get(i).toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return listdata;
    }

    public static String toyyMMdd(Date day) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = formatter.format(day);
        return date;
    }

    public void requestQuantity(final int tag, final String qty) {

        p_bar = ProgressDialog.show(context, "Crew App", "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);
        final SugarBean object = data.get(tag);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (NetworkHelper.isAvailable(context)) {

                        SOAPClient com = new SOAPClient(UserPreferences.url);
                        String response = com.setValueEntry("AOS_Products_Quotes", qty, object.getFieldValue("id"));
                        if (response != "-1") {
                            object.updateFieldValue("app_additional_qty", qty);

                            SugarBean wo_bean = new SugarBean(context, "AOS_Products_Quotes");
                            wo_bean.loadCom(context, false, true);
                            wo_bean.updateFieldValue("id", object.getFieldValue("id"));
                            wo_bean.updateFieldValue("app_additional_qty", qty);
                            wo_bean.save(false);

                        } else {

                        }
                    } else {
                        SugarBean wo_bean = new SugarBean(context, "AOS_Products_Quotes");
                        wo_bean.updateFieldValue("id", object.getFieldValue("id"));
                        wo_bean.updateFieldValue("app_additional_qty", qty);
                        object.updateFieldValue("app_additional_qty", qty);
                        wo_bean.save(false);

                        if (UserPreferences.LineItemsQtyRequest == null)
                            UserPreferences.LineItemsQtyRequest = new HashMap<String, String>();

                        UserPreferences.LineItemsQtyRequest.put(object.getFieldValue("id"), qty);
                        UserPreferences.save(context);
                    }
                } catch (Exception e) {
                    if(p_bar.isShowing())
                        p_bar.cancel();
                    object.updateFieldValue("app_additional_qty", qty);
                    Log.e("Crew ERROR", e.getMessage());
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing()) {
                            p_bar.hide();
                        }

                        Toast.makeText(context, "Successfully Saved", Toast.LENGTH_SHORT).show();

                        data.set(tag, object);
                        notifyDataSetChanged();

                    }
                });

            }
        }).start();
    }

    public void removeFocus() {
        if (selectedET != null)
            selectedET.clearFocus();
    }
}
