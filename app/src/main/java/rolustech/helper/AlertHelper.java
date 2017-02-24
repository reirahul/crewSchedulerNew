package rolustech.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;

import com.iconsolutions.crewschedular.R;

import rolustech.beans.SugarBean;
import com.iconsolutions.helper.UserPreferences;

public class AlertHelper {

	protected static boolean debug = false;

	public static void alertError(Activity act, Exception e){
		if(debug){
			showAlert(act, e.getClass().getName(), e.getMessage());
		}else{
			logError(e);
		}
	}

	public static void showAlert(Activity act, String title, String msg){
		AlertDialog diag = new AlertDialog.Builder(act).create();
		diag.setCancelable(true);

		if(title != null) diag.setTitle(title);
		if(msg != null) diag.setMessage(msg);

		diag.setInverseBackgroundForced(true);
		diag.setButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		diag.show();
	}

	public static void logError(Exception e) {
		FileWriter logger = null;

		try {
			File dir = new File(Environment.getExternalStorageDirectory(), UserPreferences.APP_NAME);
			if(!dir.exists()){
				dir.mkdirs();
			}

			logger = new FileWriter(Environment.getExternalStorageDirectory() + "/" + UserPreferences.APP_NAME + "/errors.log", true);
			logger.write(
					new Date().toGMTString() + " " +" [ File Name ] ==> " +
							e.getStackTrace()[0].getFileName() +"  "+ "[ Class Name  ] ==> " + e.getStackTrace()[0].getClassName() + "  " + " [ Method Name] ==>"+  
							e.getStackTrace()[0].getMethodName()+" "+ "[ Line Number ] ==> "+ e.getStackTrace()[0].getLineNumber()+" "+ "[ Exception Class & Message ] ==> "+
							e.getClass().getName() + ": " + e.getMessage() + " "+ "[ Cause ] ==>"+ e.getCause()+ "\n\n"
					);
			logger.close();
		} catch (Exception ex) {}
	}


	public static void logMessage(String title, String msg) {
		FileWriter logger = null;

		try {
			File dir = new File(Environment.getExternalStorageDirectory(), "AutoAccelerator");
			if(!dir.exists()){
				dir.mkdirs();
			}

			logger = new FileWriter(Environment.getExternalStorageDirectory() + "/AutoAccelerator/auto_accelerator.log", true);
			logger.write(new Date().toGMTString() + " ===> " + title + ": " + msg + "\n");
			logger.close();
		} catch (Exception e) {}
	}



	/*
	 * Shows image in alert dialog
	 */
	public static void showImageDialog(Activity act,String filename,Bitmap image) {

		final Dialog dialog = new Dialog(act);
		dialog.setTitle("Attachment");
		dialog.setCancelable(true);
		dialog.setContentView(R.layout.online_image);
		ImageView onlineImage=(ImageView)dialog.findViewById(R.id.online_downloaded_image);
		onlineImage.setImageBitmap(image);
		dialog.show();

	}
	public static void printBeans(SugarBean object)
	{
		ArrayList<String> bf = new ArrayList<>();
		ArrayList<String> bf1 = new ArrayList<String>();
		StringBuffer finalData = new StringBuffer();
		for (String value : object.getNameArray(true))
			bf.add(value);
		for (String value : object.getValueArray(true))
			bf1.add(value);
		for (int i = 0; i < bf.size(); i++)
			finalData.append(bf.get(i) + " = " + bf1.get(i) + " , ");
		Log.e("Crew_App", "Responce of Server= " + finalData.toString());
	}
}
