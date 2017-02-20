package com.iconsolutions.crewschedular;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.util.Base64;
import android.widget.Toast;

import com.iconsolutions.adapter.JobImagesAdapter;
import com.iconsolutions.adapter.JobsListAdapter;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import crewschedular.fragmentinterface.BaseBackPressedListener;
import rolustech.beans.SugarBean;
import rolustech.beans.UserPreferences;
import rolustech.communication.db.DBClient;
import rolustech.communication.soap.SOAPClient;
import rolustech.helper.NetworkHelper;
import rolustech.helper.NormalSync;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by kashif on 4/11/16.
 */
public class JobImagesFragment extends Fragment {

    private static final int RESULT_OK = 1;
    View view;
    FragmentActivity fm;
    ImageView placeHolder;
    String workOrderId, workOrderName;
    String docRevIds = null;

    ProgressDialog p_bar;
    android.os.Handler handler = new android.os.Handler();

    Button nextButton, previousButton, imagesButton, cameraButton;
    private static int RESULT_LOAD_IMG = 1, REQUEST_IMAGE_CAPTURE = 100;
    String imgDecodableString;

    GridView gridView;
    JobImagesAdapter josImagesAdapter = null;
    Boolean isUpdated = false;

    ArrayList<String> pathsList = new ArrayList<String>();// list of file paths
    File[] listFile;
    protected SugarBean docBeans[], revBeans[];
    protected SugarBean dbean, rbean;

    public JobImagesFragment(){
        this.fm = this.getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_jobimages, null);

        initUI();

        return view;
    }

    private void initUI() {

        placeHolder = (ImageView) view.findViewById(R.id.imageView);
        nextButton = (Button) view.findViewById(R.id.images_next_button);
        previousButton = (Button) view.findViewById(R.id.images_prev_button);

        gridView = (GridView) view.findViewById(R.id.gridview);
        workOrderId = getArguments().getString("WorkOrderId");
        workOrderName = getArguments().getString("WorkOrderName");

        imagesButton = (Button) view.findViewById(R.id.select_image_btn);
        cameraButton = (Button) view.findViewById(R.id.camera_image_btn);
        imagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                getActivity().startActivityForResult(galleryIntent, RESULT_LOAD_IMG);

            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    getActivity().startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }

            }
        });

        isUpdated = false;
        if(docBeans == null) {
            fetchRelatedDocuments();
        }
        else {
            if(docRevIds != null && docRevIds.length() > 0) {
                josImagesAdapter = null;
                updateJobsGridView();
            }
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToTabView(3);
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToTabView(1);
            }
        });

    }

    public void moveToTabView(int tab){
        WorkOrderFragment parentFragment = (WorkOrderFragment) this.getParentFragment();
        parentFragment.nextAction(tab);
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
                        newBeans = dbean.retrieveAll("(documents.ro_crew_work_order_id = '"+ workOrderId +"')", "", 0, 200, 0, null);
                        if (newBeans.length > 0) {
                            docBeans = newBeans;
                        } else {
                            docBeans = new SugarBean[0];
                        }

                        docRevIds = null;
                        for(int j=0; j<docBeans.length; j++){
                            SugarBean bean = docBeans[j];
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
                            updateJobsGridView();
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
        String filePath = getExternalStorageDirectory()+"/CrewPhotos/";

        for(int i=0; i<revIds.length; i++){
            File file = new File(filePath+revIds[i]+".jpeg");
            if(!file.exists())
                retIds.add(revIds[i]);
        }

        return retIds;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_OK && data != null) {
            try {
                if (requestCode == RESULT_LOAD_IMG){

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = this.fm.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();

                    uploadDocument(imgDecodableString, null);

                }else if(requestCode == REQUEST_IMAGE_CAPTURE){
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    uploadDocument(null, imageBitmap);
                }
            } catch (Exception e) {

            }
        }

    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        File direct = new File(getExternalStorageDirectory() + "/CrewPhotos");
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

    public String uploadDocument(final String picturePath, final Bitmap imageBitmap){

        p_bar = ProgressDialog.show(fm, getResources().getString(R.string.app_name), "Please wait...");
        p_bar.setCanceledOnTouchOutside(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

//                    final BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inSampleSize = 2;
                    String filename = "Camera Pic0.jpeg";
                    Bitmap bitmap = imageBitmap;
                    if(picturePath != null) {
                        filename = picturePath.substring(picturePath.lastIndexOf("/") + 1);
                        bitmap = BitmapFactory.decodeFile(picturePath);
                    }

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    final String byteData = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    if(NetworkHelper.isAvailable(fm)) {
                        SugarBean.loadCom(fm, true, false);
                    }
                    else
                        SugarBean.loadCom(fm, false, true);

                    SugarBean docbean = new SugarBean(fm, "Documents");
                    docbean.updateFieldValue("document_name", filename);
                    docbean.updateFieldValue("revision", "1");
                    docbean.updateFieldValue("ro_crew_work_order_id", workOrderId);
                    docbean.updateFieldValue("ro_crew_work_order_name", workOrderName);
                    String revdocId = docbean.uploadDocument(byteData, filename, false);

                    DBClient dbClient = new DBClient(fm);
                    dbClient.syncDocRevId("Documents", docbean.getFieldValue("id"), revdocId);
                    docbean.updateFieldValue("document_revision_id", revdocId);
                    docbean.save(false);

                    if(!revdocId.equals("-1"))
                    {
                        String fileName = revdocId + ".jpeg";
                        docRevIds = docRevIds+",'"+ revdocId + "'";
                        createDirectoryAndSaveFile(bitmap, fileName);
                        isUpdated = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (p_bar.isShowing()) {
                            p_bar.hide();
                        }
                        if(docRevIds != null && docRevIds.length() > 0)
                            updateJobsGridView();
                    }
                });
            }
        }).start();

        return "1";
    }

    public void getFromSdcard()
    {
        File file= new File(getExternalStorageDirectory(),"CrewPhotos");
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

                if(found)
                    pathsList.add(listFile[i].getAbsolutePath());
            }
        }
    }

    public void updateJobsGridView() {

        pathsList = null;
        pathsList = new ArrayList<String>();
        getFromSdcard();

        if (pathsList != null && pathsList.size() > 0) {
            if (josImagesAdapter == null) {
                josImagesAdapter = new JobImagesAdapter(this.fm, pathsList, R.layout.item_grid_image);
                gridView.setAdapter(josImagesAdapter);
            }
            else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        josImagesAdapter.updateReceiptsList(pathsList);
                    }
                }, 2500);

            }
        }
        else {
            pathsList = new ArrayList<String>();
            if (josImagesAdapter == null) {
                josImagesAdapter = new JobImagesAdapter(this.fm, pathsList, R.layout.item_grid_image);
                gridView.setAdapter(josImagesAdapter);
            }
            else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        josImagesAdapter.updateReceiptsList(pathsList);
                    }
                }, 2500);
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
