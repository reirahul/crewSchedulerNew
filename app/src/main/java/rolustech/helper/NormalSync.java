package rolustech.helper;

import android.content.Context;

import java.io.File;

import rolustech.beans.Field;
import rolustech.beans.Relationship;
import rolustech.beans.SugarBean;
import rolustech.beans.UserPreferences;
import rolustech.communication.db.DBConnection;
import rolustech.communication.soap.SOAPClient;

public class NormalSync {

	public static String performSync(Context context, boolean syncAll) {

		AlertHelper.logError(new Exception("Date in where clause:" + UserPreferences.lastSync));

		Object[] modules = UserPreferences.moduleConfiguration.keySet().toArray();

		String failedModules = "";

		//Sync modules
		for(int i=0; i<modules.length; i++){
			try {
				//Only sync used modules
				if(UserPreferences.moduleConfiguration.get((String)modules[i]).available){
					syncModule((String)modules[i], context, 0);
					
					if(UserPreferences.lastSync != null){
						//For deleted records
						syncModule((String)modules[i], context, 1);
					}
				}
			} catch (Exception e) {
				AlertHelper.logError(e);
				failedModules += modules[i] + ", ";
			}
		}

		if(syncAll) {
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

			//Sync Relationships
			for(int i=0; i<modules.length; i++){
				try {
					//Only sync used modules
					if(UserPreferences.moduleConfiguration.get((String)modules[i]).available){
						if(UserPreferences.useExtendedImport){
							syncBulkRelationships((String)modules[i], context, 0);

							if(UserPreferences.lastSyncRelationShip != null){
								//For deleted records
								syncBulkRelationships((String)modules[i], context, 1);
							}
						}else{
							syncRelationships((String)modules[i], context, 0);

							if(UserPreferences.lastSyncRelationShip != null){
								//For deleted records
								syncRelationships((String)modules[i], context, 1);
							}
						}
					}
				} catch (Exception e) {
					AlertHelper.logError(e);
					failedModules += "Relationships for " + modules[i] + ", ";
				}
			}
		}
		
		//Reload communicator
		SugarBean.loadCom(context, false, false);
		
		//Syncing team members' id, myday_counts
		if(loadFromServer()){
			UserPreferences.save(context);
		}
		
		return failedModules.trim().equalsIgnoreCase("") ? null : failedModules.substring(0, failedModules.length() - 2);
	}
	
/*
 * sync module records between server and DB
 */
	public static void syncModule(String moduleName, Context context, int deleted) throws Exception {
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
				AlertHelper.logError(new Exception("Records are more than Import Limit. Only " + UserPreferences.importLimit + " will be imported."));
			}
			
			int retrieved = 0, failed = 0;
			for(int i=1; i<=total; i+=20){
				//Flip communicator
				SugarBean.loadCom(context, syncItr == 0, true);
				
				//Retrieving changed records
				SugarBean[] beans = bean.retrieveAll(where + dbWhere, "date_modified desc", i-1, 20, deleted, null);
				retrieved += beans.length;
				
				//Flip communicator
				SugarBean.loadCom(context, syncItr == 1, true);
				
				//saving all beans to alternate location
				String[] ret = bean.saveAll(beans, true);
				for(int j=0; j<ret.length; j++){
					if(ret[j].equalsIgnoreCase("-1")) failed++;
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
			
			AlertHelper.logError(new Exception("Found total relationships recrds from Server " + total));

			if(total > UserPreferences.importLimit && !bean.moduleName.equalsIgnoreCase("Users")) {
				total = UserPreferences.importLimit;
				AlertHelper.logError(new Exception("Records are more than Import Limit. Only " + UserPreferences.importLimit + " will be imported."));
			}
			
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
						
					for (int j=0; j<beans.length; j++) {
						//getting relationship where clause
						String where = RelationshipHelper.getRelationshipWhere(relBean, moduleName, beans[j].fields.get("id").value, UserPreferences.lastSyncRelationShip, null, deleted);
						
						for(int syncItr=0; syncItr<2; syncItr++){
							//loading DB where for getting record from DB
							if(syncItr == 1){
								where = RelationshipHelper.getRelationshipWhere(relBean, moduleName, beans[j].fields.get("id").value, UserPreferences.lastSyncRelationShip, DBConnection.SYNC_COLUMN + " IN ('1', '2')", deleted);
							}
							
							//Loading SOAP for 0 and DB for 1
							SugarBean.loadCom(context, syncItr == 0, true);
							
							bean = new SugarBean(context, relatedModule);
							
							total = (int) bean.getRecordsCount(where, 0);
							
							AlertHelper.logError(new Exception("Found total updated records from " + (bean.usingDB() ? "Local DB " : "Server ") + total));

							if(total > UserPreferences.importLimit && !bean.moduleName.equalsIgnoreCase("Users")) {
								total = UserPreferences.importLimit;
								AlertHelper.logError(new Exception("Records are more than Import Limit. Only " + UserPreferences.importLimit + " will be imported."));
							}
							
							for(int k=1; k<=total; k+=20){
								//Loading SOAP for 0 and DB for 1
								SugarBean.loadCom(context, syncItr == 0, true);
								
								//Retrieving changed records
								SugarBean[] relatedBeans = bean.retrieveAll(where, "id desc", k-1, 20, 0, null);
								
								//Loading SOAP for 1 and DB for 0
								SugarBean.loadCom(context, syncItr == 1, true);
								
								for(int l=0 ; l<relatedBeans.length; l++){
									beans[j].setRelationship(relatedModule, relatedBeans[l].fields.get("id").value, true, 0);
								}
							}
						}
						
						//resetting sync flag on join table
						new SugarBean(context, relBean.fields.get("join_table").value).resetSyncFlag(deleted);
					}
				}
			}
		}
	}
	
	protected static void syncBulkRelationships(String moduleName, Context context, int deleted) throws Exception {
		String where = "1=1";
		SugarBean bean = new SugarBean(context, moduleName);
		
		if(UserPreferences.lastSync != null){
			where = "date_modified>='" + UserPreferences.lastSyncRelationShip + "'";
		}
		
		Object[] modules = UserPreferences.moduleConfiguration.keySet().toArray();
		for(int i=0; i<modules.length; i++){
			String relatedModule = (String)modules[i];
			
			//Only sync used modules
			if(UserPreferences.moduleConfiguration.get(relatedModule).available){
				//Loading DB Com
				SugarBean.loadCom(context, false, true);
				
				//Getting relationship defs
				SugarBean relBean = RelationshipHelper.getRelationshipDef(moduleName, relatedModule, context);
				
				if(relBean == null){
					continue;
				}
				
				//if third table exists
				if(!relBean.fields.get("join_table").value.equalsIgnoreCase("NULL") && !relBean.fields.get("join_table").value.trim().equalsIgnoreCase("")){
					//Log module name
					AlertHelper.logError(new Exception("Synchronizing" + (deleted == 1 ? " deleted" : "") + " Relationships for:" + moduleName + " and " + relatedModule));
					
					String moduleIdField = relBean.fields.get("join_key_lhs").value;
					String relatedModuleIdField = relBean.fields.get("join_key_rhs").value;
					
					if(moduleName.equalsIgnoreCase(relBean.fields.get("rhs_module").value)){
						moduleIdField = relBean.fields.get("join_key_rhs").value;
						relatedModuleIdField = relBean.fields.get("join_key_lhs").value;
					}
					
					for(int syncItr=0; syncItr<2; syncItr++){
						//Loading SOAP for 0 and DB for 1
						SugarBean.loadCom(context, syncItr == 0, true);
						
						String dbWhere = "";
						if(syncItr == 1){
							dbWhere = " AND " + DBConnection.SYNC_COLUMN + " IN ('1', '2')";
						}
						
						SugarBean[] relBeans = relBean.retrieveAllRelationship(relBean.fields.get("relationship_name").value, where + dbWhere, deleted);
						
						AlertHelper.logError(new Exception("Found total updated recrds from " + (bean.usingDB() ? "Local DB " : "Server ") + relBeans.length));
						
						//Loading SOAP for 1 and DB for 0
						SugarBean.loadCom(context, syncItr == 1, true);
						
						for(int k = 0; k < relBeans.length; k++){
							SugarBean relB = relBeans[k];
							
							Field id = bean.fields.get("id");
							id.value = relB.fields.get(moduleIdField).value;
							bean.fields.put("id", id);
							
							bean.setRelationship(relatedModule, relB.fields.get(relatedModuleIdField).value,true, deleted);
						}
					}
					
					//resetting sync flag on join table
					new SugarBean(context, relBean.fields.get("join_table").value).resetSyncFlag(deleted);
				}
			}
		}
	}
	
/*
 * load values from server
 */
	public static boolean loadFromServer() {
		SOAPClient com = new SOAPClient(UserPreferences.url);
		int ret = com.login(UserPreferences.userName, UserPreferences.password);
		if(ret == 1){
			try {
				UserPreferences.userID = com.getCurrentUserID();
			} catch (Exception e) {
				AlertHelper.logError(e);
			}
			
			return true;
		}
		
		return false;
	}
	
/*
 * Regenerates the module structure
 * imports fields
 * deletes DB
 * re perform sync 
 */
	public static String importDatabase(Context context) {
		if(!UserPreferences.loaded){
			UserPreferences.reLoadPrefernces(context);
		}
		
		if(UserPreferences.useExtendedImport) {
			try {
				return ImportDatabase.downloadDB(UserPreferences.url.replaceAll("soap.php", "sqliteDB") + "/sqliteZip.zip", context);
			} catch (Exception e) {
				AlertHelper.logError(e);
			}

			return "Database Download";
		}
		
		if(importFields()){
			UserPreferences.lastSync = null;
			UserPreferences.lastSyncRelationShip = null;
			
			UserPreferences.save(context);
			
			removeDB();
			
			//Loading communicator to re construct structure.
			SugarBean.loadCom(context, false, true);
			
			//Re-sync data
			return performSync(context, true);
		}else{
			//Reverting changes made
			UserPreferences.reLoadPrefernces(context);
		}
		
		return "Fields definitions";
	}
/*
 * removes DB file to re-import	
 */
	public static void removeDB() {
		//removing DB file
		File file = new File(DBConnection.DB_PATH + DBConnection.DB_NAME);
		if(file.exists()){
			file.delete();
		}
	}

/*
 * import fields from SOAP
 */
	public static boolean importFields() {
		// load fields from server and return true if loaded
		SOAPClient com = new SOAPClient(UserPreferences.url);
		Object[] modules = UserPreferences.moduleConfiguration.keySet().toArray();

		boolean success = true;
		for (int i = 0; i < modules.length; i++) {
			String modName = (String) modules[i];

			try {
				//Load fields of all modules so that DB structure is consistant
				if(UserPreferences.moduleConfiguration.get(modName).fields == null){
					UserPreferences.moduleConfiguration.get(modName).fields = com.getModulesFields(modName);
				}
			} catch (Exception e) {
				AlertHelper.logError(e);
				success = false;
			}
		}
		
		ConfigurationHelper.addMissingFields();
		
		return success;
	}
}
