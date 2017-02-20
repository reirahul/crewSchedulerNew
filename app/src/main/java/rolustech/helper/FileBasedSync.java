package rolustech.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;

import rolustech.beans.SugarBean;
import rolustech.beans.SyncData;
import rolustech.beans.UserPreferences;
import rolustech.communication.db.DBClient;
import rolustech.communication.db.DBConnection;
import rolustech.communication.soap.SOAPClient;

public class FileBasedSync {
	private static String failedModules;
	private static Context context;
    private static boolean syncAll;
    private static Handler handler = new Handler();
    private static Dialog dialog;

	public static String performSync(final Context con, boolean sync, Dialog dilog) {
		AlertHelper.logError(new Exception("Date in where clause:" + UserPreferences.lastSync));
		context = con;
        syncAll = sync;
		failedModules = "";
        dialog = dilog;
		Object[] modules = UserPreferences.moduleConfiguration.keySet().toArray();
		
		//Parse the sync data
        SOAPClient com = new SOAPClient(UserPreferences.url);
        if(com.login(UserPreferences.userName, UserPreferences.password) != 1) {
            return "";
        }
        ArrayList<SyncData> syncData = null;
        try {
            syncData = com.getSyncFilePath(UserPreferences.lastSync, "");
            SyncData data = syncData.get(0);
            downloadSyncFile(data.name, data.path);

        } catch (Exception e) {
            AlertHelper.logError(e);
        }

		return "";
	}


    public static void downloadSyncFile(final String moduleName, final String path) {

		try {
			new DownloadHelper().downloadZippedFile(moduleName, path);
			afterSync();
		} catch (Exception e) {
		}
	}

    public static void afterSync(){
        Object[] modules = UserPreferences.moduleConfiguration.keySet().toArray();
        //Sync modules
        syncModules(context, modules);

        return;

    }
	
	public static void syncModules(Context context, Object[] modules) {

		for(int i=0; i<modules.length; i++){
			try {
				//Only sync used modules
				if(UserPreferences.moduleConfiguration.get((String)modules[i]).available){
					//syncModule((String)modules[i], context, 0);
                    String moduleName = (String) modules[i];
                    if(moduleName.equalsIgnoreCase("DocumentRevisions"))
                        moduleName = "document_revisions";
					runSyncQueries(new SugarBean(context, moduleName), context);
					syncModuleToServer(new SugarBean(context, (String)modules[i]), context, 0);
					
//					if(UserPreferences.lastSync != null){
////						For deleted records
//						syncModuleToServer(new SugarBean(context, (String)modules[i]), context, 1);
//					}
				}
			} catch (Exception e) {
				AlertHelper.logError(e);
				failedModules += modules[i] + ", ";
			}
		}
	}
	
	public static void runSyncQueries (final SugarBean bean, final Context context) {
		DBClient dbClient = new DBClient(context);
		
		try {
			File file = SDCardHelper.readFileFromCache("/rSugarCRM/SyncFiles/sqlite/", bean.moduleName.toLowerCase() + "_delete.txt");
			executeRawQuery(file, dbClient);
			file = SDCardHelper.readFileFromCache("/rSugarCRM/SyncFiles/sqlite/", bean.moduleName.toLowerCase() + "_insert.txt");
			executeRawQuery(file, dbClient);

			file = SDCardHelper.readFileFromCache("/rSugarCRM/SyncFiles/sqlite/", bean.moduleName.toLowerCase() + "_cstm_delete.txt");
			executeRawQuery(file, dbClient);
			file = SDCardHelper.readFileFromCache("/rSugarCRM/SyncFiles/sqlite/", bean.moduleName.toLowerCase() + "_cstm_insert.txt");
			executeRawQuery(file, dbClient);
			
			SDCardHelper.deleteFromCache("/rSugarCRM/SyncFiles/sqlite/", bean.moduleName.toLowerCase() + "_delete.txt");
			SDCardHelper.deleteFromCache("/rSugarCRM/SyncFiles/sqlite/", bean.moduleName.toLowerCase() + "_insert.txt");
			SDCardHelper.deleteFromCache("/rSugarCRM/SyncFiles/sqlite/", bean.moduleName.toLowerCase() + "_cstm_delete.txt");
			SDCardHelper.deleteFromCache("/rSugarCRM/SyncFiles/sqlite/", bean.moduleName.toLowerCase() + "_cstm_insert.txt");
		} catch (Exception e) {
			AlertHelper.logError(e);
			failedModules += bean.moduleName;
		}
	}


	private static void executeRawQuery(File file, DBClient dbClient) throws Exception{
		String [] queries;	
		
		if(file != null) {
			//Read text from file
			StringBuilder text = new StringBuilder();
			
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;
		    while ((line = br.readLine()) != null) {
		        text.append(line);
		        text.append('\n');
		    }
		    if(!text.toString().equalsIgnoreCase("")) {
		    	queries = text.toString().split("---sqliteSync SYNC---");
			    for(int i = 0; i < queries.length; i++) {
					if(!queries[i].equalsIgnoreCase("") && !queries[i].equalsIgnoreCase("\n"))
			    		dbClient.executeRawQuery(queries[i]);
			    }
		    }
		}
	}

/*
 * 
 */
	private static void syncModuleToServer(SugarBean bean, Context context, int deleted) {

        String where = "";
		
		//Log module name
		AlertHelper.logError(new Exception("Synchronizing" + bean.moduleName));
		
		//Loading communicator to get count of changed records from server/DB
		SugarBean.loadCom(context, false, true);
		
		if(bean.fields.get(DBConnection.SYNC_COLUMN) == null){
			return;
		}
		
		String dbWhere = /*" AND " +*/ bean.moduleName.toLowerCase() + "." + DBConnection.SYNC_COLUMN + " IN ('1', '2')";
        if(bean.moduleName.equalsIgnoreCase("DocumentRevisions"))
            dbWhere = "document_revisions." + DBConnection.SYNC_COLUMN + " IN ('1', '2')";

		try {
			int total = (int) bean.getRecordsCount(where + dbWhere, deleted);
			
			AlertHelper.logError(new Exception("Found total updated records from " + (bean.usingDB() ? "Local DB" : "Server") + " : " + total + ""));
			
			int retrieved = 0, failed = 0;
			for(int i=1; i<=total; i+=20){
				//Flip communicator
				SugarBean.loadCom(context, false, true);
				
				//Retrieving changed records
				SugarBean[] beans = bean.retrieveAll(where + dbWhere, "date_modified desc", i-1, 20, deleted, null);
				retrieved += beans.length;
				
				//Flip communicator
				SugarBean.loadCom(context, true, false);
                if (bean.moduleName.equalsIgnoreCase("DocumentRevisions")) {

                    for (int k = 0; k < beans.length; k++) {
                        SugarBean revBean = beans[k];
                        String filePath = android.os.Environment.getExternalStorageDirectory() + "/CrewPhotos/";
                        File file = new File(filePath + revBean.getFieldValue("id") + ".jpeg");
                        if (file.exists()) {
//                            final String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
                            final Bitmap bitmap = BitmapFactory.decodeFile(filePath + revBean.getFieldValue("id") + ".jpeg");
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            final String byteData = Base64.encodeToString(byteArray, Base64.DEFAULT);

							SugarBean.loadCom(context, true, false);
                            String documenRevtId = bean.setDocumentRevision(revBean.getFieldValue("document_id"), byteData, revBean.getFieldValue("filename"), "1");
                            if (!documenRevtId.equals("-1")) {

                                DBClient dbClient = new DBClient(context);
                                dbClient.uodateDocRevisionId("Documents", revBean.getFieldValue("document_id"), revBean.getFieldValue("id"));

                                bean.resetID(revBean.getFieldValue("id"), documenRevtId);
                                String fileName = documenRevtId + ".jpeg";
                                createDirectoryAndSaveFile(bitmap, fileName);
                            }
                        }

                    }
                }

                else {
                    //saving all beans to alternate location
                    String[] ret = bean.saveAll(beans, true);
                    for (int j = 0; j < ret.length; j++) {
                        if (ret[j].equalsIgnoreCase("-1")) failed++;
                    }
                }
			}
			
			//logging if some records have failed saving
			AlertHelper.logError(new Exception("Successful records in updation : " + (retrieved - failed) + ""));
			if(failed > 0) AlertHelper.logError(new Exception("Failed records in updation : " + failed + ""));
			
			//resetting synconSave to 0
			if(bean.fields.get(DBConnection.SYNC_COLUMN) != null){
				bean.resetSyncFlag(deleted);
			}
		} catch(Exception e) {
			AlertHelper.logError(e);
		}
	}

    private static void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

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
}
