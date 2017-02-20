package com.iconsolutions.crewschedular;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;

import com.iconsolutions.adapter.JobImagesAdapter;
import com.iconsolutions.adapter.PlanImagesAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import crewschedular.fragmentinterface.BaseBackPressedListener;
import rolustech.beans.SugarBean;
import rolustech.beans.UserPreferences;
import rolustech.communication.soap.SOAPClient;
import rolustech.helper.NetworkHelper;
import rolustech.helper.NormalSync;

/**
 * Created by kashif on 4/11/16.
 */
public class PlanImageFragment extends Fragment{

    View view;
    FragmentActivity fm;
    Button previous;
    String docRevIds = null;
    String workOrderId, workOrderName, jobId;
    ProgressDialog p_bar;
    android.os.Handler handler = new android.os.Handler();

    GridView gridView;
    PlanImagesAdapter planImagesAdapter = null;
    Boolean isUpdated = false;
    HashMap<String, String> planImagesNames = new HashMap<String, String>();

    ArrayList<String> pathsList = new ArrayList<String>();// list of file paths
    File[] listFile;
    protected SugarBean docBeans[], revBeans[];
    protected SugarBean dbean, rbean;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.planimage_fragment, null);

        previous = (Button) view.findViewById(R.id.planimage_previous_button);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToPreviousView();
            }
        });

        gridView = (GridView) view.findViewById(R.id.plangridview);
        workOrderId = getArguments().getString("WorkOrderId");
        workOrderName = getArguments().getString("WorkOrderName");
        jobId = getArguments().getString("JobID");

        isUpdated = false;
        if(docBeans == null) {
            fetchRelatedDocuments();
        }
        else {
            if(docRevIds != null && docRevIds.length() > 0) {
                planImagesAdapter = null;
                updatePlanImagesGridView();
            }
        }

        return view;
    }

    public void moveToPreviousView(){
        WorkOrderFragment parentFragment = (WorkOrderFragment) this.getParentFragment();
        parentFragment.nextAction(3);
    }

    public void fetchRelatedDocuments()
    {
        p_bar = ProgressDialog.show(fm, getResources().getString(R.string.app_name), "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    UserPreferences.reLoadPrefernces(fm);
                    dbean = new SugarBean(fm, "Documents");

                    SugarBean[] newBeans;
                    docBeans = new SugarBean[0];
                    Boolean login = false;
                    if(NetworkHelper.isAvailable(fm)) {
                        SugarBean.loadCom(fm, true, false);
                        login = NormalSync.loadFromServer();
                    }
                    else
                        SugarBean.loadCom(fm, false, true);



                    if ((NetworkHelper.isAvailable(fm) && login) || !NetworkHelper.isAvailable(fm)) {
                        newBeans = dbean.retrieveAll("(documents.rt_jobs_id = '"+ jobId +"')", "", 0, 200, 0, null);
                        if (newBeans.length > 0) {
                            docBeans = newBeans;
                        } else {
                            docBeans = new SugarBean[0];
                        }

                        docRevIds = null;
                        for(int j=0; j<docBeans.length; j++){
                            SugarBean bean = docBeans[j];
                            planImagesNames.put(bean.getFieldValue("document_revision_id"), bean.getFieldValue("document_name"));

                            if(docRevIds != null)
                                docRevIds = docRevIds+",'"+ bean.getFieldValue("document_revision_id") + "'";
                            else
                                docRevIds = "'"+ bean.getFieldValue("document_revision_id") + "'";
                        }

                        if(docBeans != null && docBeans.length > 0) {

                            ArrayList<String> idsList = checkIfImagesExists();
                            if(NetworkHelper.isAvailable(fm)) {

                                if (idsList != null && idsList.size() > 0) {
                                    SOAPClient com = new SOAPClient(UserPreferences.url);

                                    for (int j = 0; j < idsList.size(); j++) {
                                        HashMap<String, String> values = com.getDocumentRevision(idsList.get(j));

                                        if (values.size() > 0) {
                                            if(values.get("filename").toLowerCase().contains(".pdf")) {
                                                byte[] decodedString = Base64.decode(values.get("file"), Base64.DEFAULT);
                                                createDirectoryAndSavePDFFile(decodedString, idsList.get(j) + ".pdf");
                                            }
                                            else {
                                                byte[] decodedString = Base64.decode(values.get("file"), Base64.DEFAULT);
                                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                                createDirectoryAndSaveFile(decodedByte, idsList.get(j) + ".jpeg");
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch(Exception e){
                    e.getStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing()) {
                            p_bar.hide();
                        }
                        if(docBeans != null && docBeans.length > 0) {
                            updatePlanImagesGridView();
                        }
                    }
                });

            }
        }).start();
    }

    public ArrayList<String> checkIfImagesExists(){

        ArrayList<String> retIds = new ArrayList<String>();
        String ids = docRevIds.replace("'", "");
        String[] revIds = ids.split(",");
        String filePath = android.os.Environment.getExternalStorageDirectory()+"/CrewPhotos/";

        for(int i=0; i<revIds.length; i++){
            File file = new File(filePath+revIds[i]+".jpeg");
            File pdf = new File(filePath+revIds[i]+".pdf");
            if(!file.exists() && !pdf.exists())
                retIds.add(revIds[i]);
        }

        return retIds;
    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        File direct = new File(Environment.getExternalStorageDirectory() + "/CrewPhotos");
        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/CrewPhotos/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File(new File("/sdcard/CrewPhotos/"), fileName);
        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 20, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void createDirectoryAndSavePDFFile(byte[] pdfBytes, String fileName){
        File direct = new File(Environment.getExternalStorageDirectory() + "/CrewPhotos");
        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/CrewPhotos/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File(new File("/sdcard/CrewPhotos/"), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream os;
            os = new FileOutputStream(file, false);
            os.write(pdfBytes);
            os.flush();
            os.close();
        }
        catch (Exception e){

        }
    }

    public void updatePlanImagesGridView() {

        pathsList = null;
        pathsList = new ArrayList<String>();
        getFromSdcard();

        if (pathsList != null && pathsList.size() > 0) {
            if (planImagesAdapter == null) {
                planImagesAdapter = new PlanImagesAdapter(this.fm, pathsList, R.layout.item_grid_image, planImagesNames);
                gridView.setAdapter(planImagesAdapter);
            }
            else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        planImagesAdapter.updateReceiptsList(pathsList, planImagesNames);
                    }
                }, 2500);

            }
        }
        else {
            pathsList = new ArrayList<String>();
            if (planImagesAdapter == null) {
                planImagesAdapter = new PlanImagesAdapter(this.fm, pathsList, R.layout.item_grid_image, planImagesNames);
                gridView.setAdapter(planImagesAdapter);
            }
            else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        planImagesAdapter.updateReceiptsList(pathsList, planImagesNames);
                    }
                }, 2500);
            }
        }

    }

    public void getFromSdcard()
    {
        File file= new File(android.os.Environment.getExternalStorageDirectory(),"CrewPhotos");
        if (file.isDirectory())
        {
            listFile = null;
            listFile = file.listFiles();
            for (int i = 0; i < listFile.length; i++)
            {
                String str1 = docRevIds.replace("'", "");
                String[] parts = str1.split(",");
                Boolean found = false;
                for (int j = 0; j < parts.length; j++) {
                    if(!parts[j].equals("")) {
                        if (listFile[i].getAbsolutePath().contains(parts[j])) {
                            found = true;
                            break;
                        }
                    }

                }

                if(found) {
                    pathsList.add(listFile[i].getAbsolutePath());
                }
            }
        }
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
