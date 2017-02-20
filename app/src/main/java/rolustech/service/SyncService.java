package rolustech.service;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import rolustech.beans.UserPreferences;
import rolustech.communication.db.DBConnection;
import rolustech.communication.soap.SOAPClient;
import rolustech.helper.AlertHelper;
import rolustech.helper.NetworkHelper;
import rolustech.helper.SoapHelper;
import rolustech.helper.SyncHelper;

public class SyncService extends IntentService{

	
	public SyncService() {
		super("SyncService");
	}
	
	public SyncService(String name) {
		super(name);
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		Log.e("SyncService", "Started");


        try {
            File database = new File(DBConnection.DB_PATH + DBConnection.DB_NAME);

            if (UserPreferences.userID != null && database.exists()) {
//                String url = SoapHelper.generateUrl(UserPreferences.url);
//                SOAPClient soap = new SOAPClient(UserPreferences.url);

//                UserPreferences.syncRunning = true;

//                new SyncHelper().performSync(null, true, false, getApplicationContext());

//                UserPreferences.syncRunning = false;
                sendEmailtoManager();
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

	}

    public void sendEmailtoManager(){

        try {
            String workOrderId;
            Object[] wKeys = UserPreferences.WorkOrderHoursRequest.keySet().toArray();
            for (int i = 0; i < wKeys.length; i++) {
                workOrderId = (String) wKeys[i];
                String hours = UserPreferences.WorkOrderHoursRequest.get(workOrderId);

                SOAPClient soap = new SOAPClient(UserPreferences.url);
                String response = soap.setValueEntry("ro_crew_work_order", hours, workOrderId);
//                if(response != "-1")
                    UserPreferences.WorkOrderHoursRequest.remove(workOrderId);
            }

            Object[] lKeys = UserPreferences.LineItemsQtyRequest.keySet().toArray();
            for (int i = 0; i < lKeys.length; i++) {
                String lineItemId = (String) lKeys[i];
                String quantity = UserPreferences.LineItemsQtyRequest.get(lineItemId);

                SOAPClient soap = new SOAPClient(UserPreferences.url);
                String response = soap.setValueEntry("AOS_Products_Quotes", quantity, lineItemId);
//                if(response != "-1")
                    UserPreferences.LineItemsQtyRequest.remove(lineItemId);

            }
        }
        catch (Exception e){
//            Log.d("Exception: ", e.getMessage());
        }
    }

}
