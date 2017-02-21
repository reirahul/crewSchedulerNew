package com.iconsolutions.crewschedular;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.iconsolutions.adapter.LineItemsAdapter;

import java.util.ArrayList;
import java.util.Hashtable;

import crewschedular.fragmentinterface.BaseBackPressedListener;
import rolustech.beans.Field;
import rolustech.beans.ModuleConfig;
import rolustech.beans.SugarBean;
import rolustech.beans.UserPreferences;
import rolustech.communication.soap.SOAPClient;
import rolustech.helper.AlertHelper;
import rolustech.helper.NetworkHelper;

import static com.iconsolutions.adapter.LineItemsAdapter.parseDoubleOrNull;

/**
 * Created by kashif on 4/11/16.
 */
public class LineItemsFragment extends Fragment{

    View view;
    FragmentActivity fm;
    EditText startDate,endDate,totalMen;
    ListView lineItems_lv;
    ArrayList<SugarBean> lineItems = null;
    LineItemsAdapter lineItemsAdapter;
    Boolean isUpdated = false, isNotesUpdated = false;
    Button previousButton, saveButton;
    ArrayList<String> statusNames, statusValues;
    ProgressDialog p_bar;
    Handler handler = new Handler();
    String saleOrderId, workOrderId, workOrderName, woStatus, woDescription;
    Dialog dialog;
    WorkOrderFragment parentFragment;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.lineitems_fragment, null);
        parentFragment = (WorkOrderFragment) this.getParentFragment();
        this.lineItems = new ArrayList<SugarBean>();
        this.lineItems = parentFragment.getLineItems();
        initUI();
        return view;
    }

    private void initUI(){

        previousButton = (Button) view.findViewById(R.id.lineitems_prev_button);
        saveButton = (Button) view.findViewById(R.id.lineitems_save_button);
        lineItems_lv = (ListView) view.findViewById(R.id.lineitems_lv);

        saleOrderId = getArguments().getString("SaleOrderId");
        workOrderId = getArguments().getString("WorkOrderId");
        workOrderName = getArguments().getString("WorkOrderName");
        if(!isNotesUpdated) {
//        if(!woStatus.equalsIgnoreCase(""))
            woStatus = getArguments().getString("Status");
//        if(!woDescription.equalsIgnoreCase(""))
            woDescription = getArguments().getString("Description");
        }
        populateLineItems();

        UserPreferences.reLoadPrefernces(this.getContext());
        ModuleConfig config = UserPreferences.moduleConfiguration.get("ro_crew_work_order");
        Hashtable<String, Field> fields = config.fields;

        if(fields != null && fields.size() > 0) {

            Field field = fields.get("status");

            String[][] options = field.options;

            final String[] names = options[0];
            final String[] values = options[1];
            statusValues = new ArrayList<String>();
            statusNames = new ArrayList<String>();
            for(int i=0; i<names.length; i++){
                statusNames.add(names[i]);
                statusValues.add(values[i]);
            }

        }

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToTabView(2);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(fm, v);
                lineItemsAdapter.removeFocus();
                showDialog();
            }
        });

    }

    private void populateLineItems() {
        if (!isUpdated) {
            lineItemsAdapter = new LineItemsAdapter(this.fm, lineItems, R.layout.lineitem_item);
            lineItems_lv.setAdapter(lineItemsAdapter);
        } else {
            lineItemsAdapter.updateReceiptsList(lineItems);
        }
    }



    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public void moveToTabView(int tab){
        WorkOrderFragment parentFragment = (WorkOrderFragment) this.getParentFragment();
        parentFragment.nextAction(tab);
    }

    private void showDialog() {

        dialog = new Dialog(getActivity(),android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.dialog);
        final EditText etNotes = (EditText) dialog.findViewById(R.id.etNotes);
        final TextView tSave = (TextView) dialog.findViewById(R.id.tSave);
        startDate = (EditText) dialog.findViewById(R.id.start_date);
        endDate = (EditText) dialog.findViewById(R.id.end_date);
        totalMen = (EditText) dialog.findViewById(R.id.total_men);
        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spiner_option);
        ArrayAdapter<String> plantsAdapter = new ArrayAdapter<String>(fm, android.R.layout.simple_spinner_item,statusNames);
        plantsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(plantsAdapter);
        spinner.setSelection(plantsAdapter.getPosition(woStatus));
        etNotes.setText(woDescription);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDateTime(startDate);
            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDateTime(endDate);
            }
        });
        tSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = spinner.getSelectedItem().toString();
                String notes = etNotes.getText().toString();
                saveInstalledQty();
                saveWorkOrder(status, notes);
            }
        });
        dialog.show();

    }

    public void getDateTime(final TextView tv) {
        final String[] dobStr = new String[1];
        final Dialog dialogDT = new Dialog(getActivity());
        dialogDT.setContentView(R.layout.date_time_layout);
        dialogDT.setTitle("Select date of birth");
        dialogDT.show();
        Button btnSet = (Button) dialogDT.findViewById(R.id.okBtn);
        final TimePicker dp = (TimePicker) dialogDT
                .findViewById(R.id.datePicker1);
        dp.setVisibility(View.VISIBLE);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dobStr[0] = dp.getCurrentHour()  + " " + (dp.getCurrentMinute() + 1) + " ";
                tv.setText(dobStr[0]);
                dialogDT.dismiss();
            }
        });
    }

    public void saveWorkOrder(final String status, final  String notes){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Boolean success = false;
                try {
//                    if (NetworkHelper.isAvailable(fm)) {
//                        SOAPClient com = new SOAPClient(UserPreferences.url);
                        SugarBean woBean = new SugarBean(fm, "ro_crew_work_order");
                        woBean.updateFieldValue("id", workOrderId);
                        woBean.updateFieldValue("status", status);
                        woBean.updateFieldValue("description", notes);
                        String response = woBean.save(false);

                        if(status.equalsIgnoreCase("All Task Completed")) {
                            SugarBean soBean = new SugarBean(fm, "ro_crew_work_order");
                            soBean.updateFieldValue("id", saleOrderId);
                            soBean.updateFieldValue("complete", "1");
                            String response1 = soBean.save(false);
                        }

                        if (response != "-1") {
                            isNotesUpdated = true;
                            woStatus = status;
                            woDescription = notes;
                            success = true;
//                            AlertHelper.showAlert(fm, "Success", "Work Order successfully saved");
                        } else {
//                            AlertHelper.showAlert(fm, "Error", "Work Order save failed");
                        }
//                    }
                } catch (Exception e) {
//                    Log.d("ERROR", e.getMessage());
//                    AlertHelper.showAlert(context.conte, "Error", e.getMessage());
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
    //                    Toast.makeText(fm, "Successfully Saved", Toast.LENGTH_SHORT).show();
                        parentFragment.setNotes(notes);
                        dialog.hide();
                    }
                });

            }
        }).start();
    }
    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition
                + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
    public void saveInstalledQty() {
        p_bar = ProgressDialog.show(getActivity(), "Crew App", "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                View parentView = null;
                String[] names = {"name","start_time","end_time","totalmen","app_installed_qty","total_amount","aos_products_quotes_id"
                        ,"crew_work_id","rt_batch_id","ro_crew_work_order_id"};

                for(int i=0;i<lineItems_lv.getCount();i++) {

                    parentView = getViewByPosition(i, lineItems_lv);
                    EditText v = (EditText) parentView .findViewById(R.id.installed_qty_text);
                    final SugarBean object = lineItems.get(i);
                    try {
                        String[] values = {object.getFieldValue("name"),startDate.getText().toString(),endDate.getText().toString()
                                , totalMen.getText().toString(), v.getText().toString(),"0", object.getFieldValue("id"), UserPreferences.userID
                                , "0", workOrderId};

                        String response;
                        if (NetworkHelper.isAvailable(getActivity())) {
                            SOAPClient com = new SOAPClient(UserPreferences.url);
                            object.updateFieldValue("app_installed_qty", "0");
                            int prevQty = (int) parseDoubleOrNull(v.getText().toString()) + (int) parseDoubleOrNull(object.getFieldValue("app_prev_installed_qty"));
                            object.updateFieldValue("app_prev_installed_qty", String.valueOf(prevQty));
                            response = object.save(false);
                            String direct = com.setEntry("ro_crew_work_line_items",names,values,false,false,false);
                            Log.e("Crew_App","Successfully Saved Directly =>"+direct);
                            }
                        else {
                            SugarBean wo_bean = new SugarBean(getActivity(), "AOS_Products_Quotes");
                            wo_bean.updateFieldValue("id", object.getFieldValue("id"));
                            wo_bean.updateFieldValue("app_installed_qty", "0");
                            int prevQty = (int) parseDoubleOrNull(v.getText().toString()) + (int) parseDoubleOrNull(object.getFieldValue("app_prev_installed_qty"));
                            wo_bean.updateFieldValue("app_prev_installed_qty", String.valueOf(prevQty));
                            object.updateFieldValue("app_prev_installed_qty", String.valueOf(prevQty));
                            object.updateFieldValue("app_installed_qty", "0");
                            response = wo_bean.save(false);
                            Log.e("Crew_App","Successfully Saved =>"+response);
                        }

                        lineItems.set(i, object);
                    } catch (Exception e) {
                    Log.e("ERROR", e.getMessage());
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing()) {
                            p_bar.dismiss();
                        }
                        lineItemsAdapter.notifyDataSetChanged();

                    }
                });

            }
        }).start();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.fm = getActivity();
        if(this.fm != null)
        {
            ((MainActivity) this.fm).setOnBackPressedListener(new BaseBackPressedListener(this.fm));
        }
    }

    @Override
    public void onResume() {
        MainActivity mainActivity = (MainActivity) this.fm;

//        mainActivity.setTitle(title);
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
            java.lang.reflect.Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
