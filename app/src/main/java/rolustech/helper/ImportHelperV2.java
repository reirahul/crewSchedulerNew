package rolustech.helper;

import android.content.Context;
import android.graphics.Bitmap;

import org.kobjects.base64.Base64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import rolustech.beans.Relationship;
import rolustech.beans.SugarBean;
import com.iconsolutions.helper.UserPreferences;
import rolustech.communication.db.DBConnection;
import rolustech.communication.soap.SOAPClient;
import rolustech.tempStorage.SugarBeanContainer;

public class ImportHelperV2 {

	public static String performSync(Context context, boolean syncAll) {

		AlertHelper.logError(new Exception("Date in where clause:" + UserPreferences.lastSync));

		Object[] modules = UserPreferences.moduleConfiguration.keySet().toArray();

		String failedModules = "";

		if (syncAll) {
			//Sync relationship definitions
			try {
				syncRelationshipDefs(context, 0);

				if(UserPreferences.lastSyncRelationShip != null){
					//For deleted records
					syncRelationshipDefs(context, 1);
				}
			} catch (Exception e) {
				AlertHelper.logError(e);
			}
		}
		
		//Sync modules
		for(int i=0; i<modules.length; i++){
			try {
				
				String[] relatedModules = null;
				if (syncAll) {
					relatedModules = RelationshipHelper.getRelatedModulesName((String)modules[i], context);
				}
				
				//Only sync used modules
				if(UserPreferences.moduleConfiguration.get((String)modules[i]).available){
					syncModule((String)modules[i], context, 0, relatedModules);
					if (syncAll) {
						syncRelationships((String)modules[i], context, 0);
					}
					
					if(UserPreferences.lastSync != null){
						//For deleted records
						syncModule((String)modules[i], context, 1, relatedModules);
						if (syncAll && UserPreferences.lastSyncRelationShip != null) {
							syncRelationships((String)modules[i], context, 1);
						}
					}
				}

			} catch (Exception e) {
				AlertHelper.logError(e);
				failedModules += modules[i] + ", ";
			}
		}
		
		//Reload communicator
		SugarBean.loadCom(context, false, false);
		
		//Syncing team members' id, myday_counts
		if(ImportHelper.loadFromServer()){
			UserPreferences.save(context);
		}
		
		return failedModules.trim().equalsIgnoreCase("") ? null : failedModules.substring(0, failedModules.length() - 2);
	}
	
/*
 * sync module records between server and DB
 */
	public static void syncModule(String moduleName, Context context, int deleted, String[] relatedModules) throws Exception {
		String where = "1=1";
		
		SugarBean bean = new SugarBean(context, moduleName);
		
		if(UserPreferences.lastSync != null){
			where = "(" + 
					bean.moduleName.toLowerCase() + ".date_modified>='" + UserPreferences.lastSync + "'" +
					" OR " + 
					bean.moduleName.toLowerCase() + ".date_entered>='" + UserPreferences.lastSync + "'" +
					")";
		}
		//Log module name
		AlertHelper.logError(new Exception("Synchronizing " + bean.moduleName + (deleted == 1 ? " deleted" : "") + " records"));

		// Preventing the Sync of Duplicate Records in Sync for Latest Cases and Sync for My Cases
		
		for(int syncItr=0; syncItr<2; syncItr++){
			//Loading communicator to get count of changed records from server/DB
			SugarBean.loadCom(context, syncItr == 0, true);
			
			if(bean.usingDB() && bean.fields.get(DBConnection.SYNC_COLUMN) == null){
				continue;
			}
			
			String dbWhere = "";
			if(bean.usingDB()){
				dbWhere = " AND " + bean.moduleName.toLowerCase() + "." + DBConnection.SYNC_COLUMN + " IN ('1', '2')";
			}

			int total = (int) bean.getRecordsCount(where + dbWhere, deleted);
			
			AlertHelper.logError(new Exception("Found total updated recrds from " + (bean.usingDB() ? "Local DB " : "Server ") + total));

			if(total > UserPreferences.importLimit && !bean.moduleName.equalsIgnoreCase("Users")) {
				total = UserPreferences.importLimit;
			}
			
			int retrieved = 0, failed = 0;
			for(int i=1; i<=total; i+=20){
				//Flip communicator
				SugarBean.loadCom(context, syncItr == 0, true);
				
				//Retrieving changed records
				SugarBean[] beans = null;
				SugarBeanContainer beansContainer = null;
				
				if(bean.usingDB()) {
					beans = bean.retrieveAll(where + dbWhere, "date_modified desc", i-1, 20, deleted, null);
				} else {
					beansContainer = bean.retrieveAllV2Sync(where + dbWhere, "date_modified desc", i-1, 20, deleted, relatedModules);
					beans = beansContainer.beans;
				}
				retrieved += beans.length;
				
				//Flip communicator
				SugarBean.loadCom(context, syncItr == 1, true);
				
				//saving all beans to alternate location
				String[] ret = bean.saveAll(beans, true);

				if(syncItr == 1 && moduleName.equalsIgnoreCase("Notes")) {
				//	syncAttachments(ret);
				}

				for(int j=0; j<ret.length; j++){
					if(ret[j].equalsIgnoreCase("-1")) failed++;
				}
				
				if(beansContainer != null && relatedModules != null) {
					for (int k = 0; k < relatedModules.length; k++) {
						//Only sync used modules
						if(UserPreferences.moduleConfiguration.get(relatedModules[k]).available){
							//Getting relationship defs
							SugarBean relBean = RelationshipHelper.getRelationshipDef(moduleName, relatedModules[k], context);
	
							if(relBean == null){
								continue;
							}
	
							for(int j=0; j<ret.length; j++){
								if(ret[j].equalsIgnoreCase("-1")) 
									continue;
								
								ArrayList<ArrayList<String[]>> rel = beansContainer.relationships.get(j+"").get(relatedModules[k].toLowerCase());
								SugarBean[] relBeans = SoapHelper.getRelatedBeans(relatedModules[k], context, rel, j);
								
								if (relBeans != null) {
									for (int l = 0; l < relBeans.length; l++) {
										beansContainer.beans[j].setRelationship(relatedModules[k], relBeans[l].getFieldValue("id"), true, deleted);
									}
								}
							}
						}
					}
				}
				
			}
			
			//logging if some records have failed saving
			AlertHelper.logError(new Exception("Successful records in updation " + (retrieved - failed)));
			if(failed > 0) AlertHelper.logError(new Exception("Failed records in updation" + failed));
		}
		
		//resetting synconSave to 0
		if(bean.fields.get(DBConnection.SYNC_COLUMN) != null){
			bean.resetSyncFlag(deleted);
		}
	}

	/*
	 * synchronizes relationship defs from the server
	 */
	public static void syncRelationshipDefs(Context context, int deleted) throws Exception {
		//Log module name
		AlertHelper.logError(new Exception("Synchronizing " + (deleted == 1 ? " deleted" : "") + "Relationship Definitions"));

		Relationship bean = new Relationship(context);

		//Loading DB Com
		SugarBean.loadCom(context, false, true);

		//removing existing relationships to avoid duplication
		bean.removeAll();

		//Jumping to online mode
		SugarBean.loadCom(context, true, false);

		int retrieved = 0, failed = 0;

		try{
			int total = (int) bean.getRecordsCount(null, deleted);

			AlertHelper.logError(new Exception("Found total relationships rec0rds from Server " + total));

			for(int i=1; i<=total; i+=20){
				//Loading SOAP com
				SugarBean.loadCom(context, true, true);

				//Retrieving changed records
				SugarBean[] beans = bean.retrieveAll(null, "id desc", i-1, 20, deleted, null);
				retrieved += beans.length;

				//Loading DB Com
				SugarBean.loadCom(context, false, true);

				//saving all beans to alternate location
				String[] ret = bean.saveAll(beans, true);
				for(int j=0; j<ret.length; j++){
					if(ret[j].equalsIgnoreCase("-1")) failed++;
				}
			}
		}catch (Exception e) {

		}

		//logging if some records have failed saving
		AlertHelper.logError(new Exception("Successful records in updation " + (retrieved - failed)));
		if(failed > 0) AlertHelper.logError(new Exception("Failed records in updation" + failed));
	}

	/*
	 * Sync updated relationships between all modules
	 */
	protected static void syncRelationships(String moduleName, Context context, int deleted) throws Exception {
		if(moduleName.equalsIgnoreCase("Products")) return;
		
		//Loading DB Com
		SugarBean.loadCom(context, false, true);

		SugarBean bean = new SugarBean(context, moduleName);

		int total = (int) bean.getRecordsCount(null, 0);

		SugarBean[] beans = bean.retrieveAll(null, "id desc", -1, total, 0, null);

		Object[] modules = UserPreferences.moduleConfiguration.keySet().toArray();
		for(int i=0; i<modules.length; i++){
			String relatedModule = (String)modules[i];

			//Loading DB Com
			SugarBean.loadCom(context, false, true);

			//Only sync used modules
			if(UserPreferences.moduleConfiguration.get(relatedModule).available){
				//Getting relationship defs
				SugarBean relBean = RelationshipHelper.getRelationshipDef(moduleName, relatedModule, context);

				if(relBean == null){
					continue;
				}

				//if third table exists
				if(!relBean.fields.get("join_table").value.equalsIgnoreCase("NULL") && !relBean.fields.get("join_table").value.trim().equalsIgnoreCase("")){
					//Log module name
					AlertHelper.logError(new Exception("Synchronizing" + (deleted == 1 ? " deleted" : "") + " Relationships for:" + moduleName + " and " + relatedModule));

					for(int j=0; j<beans.length; j++) {
						//loading DB where for getting record from DB
						String where = RelationshipHelper.getRelationshipWhere(relBean, moduleName, beans[j].fields.get("id").value, UserPreferences.lastSyncRelationShip, DBConnection.SYNC_COLUMN + " IN ('1', '2')", deleted);

						//Loading SOAP for 0 and DB for 1
						SugarBean.loadCom(context, false, true);

						bean = new SugarBean(context, relatedModule);

						total = (int) bean.getRecordsCount(where, 0);

						if(total > UserPreferences.importLimit && !bean.moduleName.equalsIgnoreCase("Users")) {
							total = UserPreferences.importLimit;
						}
						
						AlertHelper.logError(new Exception("Found total updated recrds from " + (bean.usingDB() ? "Local DB " : "Server ") + total));
						
						/*if(total > UserPreferences.importLimit) {
							total = UserPreferences.importLimit;
						}*/
						
						for(int k=1; k<=total; k+=20){
							//Loading SOAP for 0 and DB for 1
							SugarBean.loadCom(context, false, true);

							//Retrieving changed records
							SugarBean[] relatedBeans = bean.retrieveAll(where, "id desc", k-1, 20, 0, null);

							//Loading SOAP
							SugarBean.loadCom(context, true, true);

							for(int l=0 ; l<relatedBeans.length; l++){
								beans[j].setRelationship(relatedModule, relatedBeans[l].fields.get("id").value, true, deleted);
							}
						}
					}
					//resetting sync flag on join table
					new SugarBean(context, relBean.fields.get("join_table").value).resetSyncFlag(deleted);
				}
			}
		}
	}

	protected static void syncAttachments(String[] ids) throws Exception {
		for(int i = 0; i < ids.length; i++) {
			String moduleId = ids[i];
			
			if(!moduleId.equalsIgnoreCase("-1")) {
				Bitmap bitmap = ImageCacheHelper.readFromCache(UserPreferences.APP_NAME, moduleId + ".jpg");
				
				ByteArrayOutputStream bao = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 95, bao);
				byte [] ba = bao.toByteArray();

				SOAPClient com = new SOAPClient(UserPreferences.url);
				String id = com.setNoteAttachment(moduleId, moduleId + ".jpg", Base64.encode(ba));
				
				//Delete the image from sdcard when it goes to server successfuly.
				if(id != null) {
					ImageCacheHelper.deleteFromCache(UserPreferences.APP_NAME, moduleId + ".jpg");
				}
			}
		}
	}
	
}
