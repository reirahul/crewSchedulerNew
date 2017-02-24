package rolustech.helper;

import android.app.Activity;
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import rolustech.beans.Field;
import rolustech.beans.Relationship;
import rolustech.beans.SugarBean;
import rolustech.beans.SyncData;
import com.iconsolutions.helper.UserPreferences;
import rolustech.communication.CommunicationFactory;
import rolustech.communication.db.DBClient;
import rolustech.communication.db.DBConnection;
import rolustech.communication.soap.SOAPClient;

public class ImportHelperFileSync {
	private static CountDownLatch latch;
	private static String failedModules;
	private static boolean defaultRelations = false;
	
	public static String performSync(final Activity activity, boolean syncAll) {
		AlertHelper.logError(new Exception("Date in where clause:" + UserPreferences.lastSync));
		
		final Context context = activity.getApplicationContext();
		failedModules = "";

		SOAPClient com = (SOAPClient) CommunicationFactory.getCommunicator(context, true, false);
		if(com == null) {
			return "connection with the server";
		}

		Object[] modules = UserPreferences.moduleConfiguration.keySet().toArray();
		String modulesList = "";
		ArrayList<SyncData> modulesSyncData = new ArrayList<SyncData>(),
							cstmModulesSyncData = new ArrayList<SyncData>(),
							relationshipsSyncData = new ArrayList<SyncData>();
		
		for(int i = 0; i < modules.length; i++) {
			modulesList += (String) modules[i];
			if(i != modules.length-1) {
				modulesList += ",";
			}
		}

		String query = "date_modified > '";
		if(syncAll) {
			query += UserPreferences.lastSyncRelationShip + "'";
		} else {
			query += UserPreferences.lastSync + "'";
		}
		ArrayList<SyncData> syncPathReturn;
		
		try {
			syncPathReturn = com.getSyncFilePath(query, modulesList);

			for(int i = 0; i < syncPathReturn.size(); i++) {
				SyncData data = syncPathReturn.get(i);
				
				// Categorizing Modules, Custom-Modules and Relationships
				if(data.type.equalsIgnoreCase("module") && UserPreferences.moduleConfiguration.get(data.name).available) {
					modulesSyncData.add(data);
				} else if(data.type.equalsIgnoreCase("custom_module") && UserPreferences.moduleConfiguration.get(data.name).available) {
					cstmModulesSyncData.add(data);
				} else if (data.type.equalsIgnoreCase("relationship")) {
					relationshipsSyncData.add(data);
				}
			}
		} catch (Exception e) {
			AlertHelper.logError(e);
		}

		if(syncAll) {
			//Sync relationship definitions
			try {
				ImportHelper.syncRelationshipDefs(context, 0);

				if(UserPreferences.lastSyncRelationShip != null){
					//For deleted records
					ImportHelper.syncRelationshipDefs(context, 1);
				}
			} catch (Exception e) {
				AlertHelper.logError(e);
			}

			//Sync Relationships
			try {
				SugarBean[] relationshipModules;
				SugarBean[] relationships = retrieveRelationshipDefs(context, 0);
				SugarBean[] deletedRelationships = retrieveRelationshipDefs(context, 1);
				
				if(!defaultRelations) {
					relationshipModules = new SugarBean[relationships.length + deletedRelationships.length];
					System.arraycopy(relationships, 0, relationshipModules, 0, relationships.length);
					System.arraycopy(relationships, 0, relationshipModules, relationships.length, relationships.length);
				} else {
					relationshipModules = relationships;
				}
				
				// selecting only used relationships.
				ArrayList<SyncData> tempRelSyncData = new ArrayList<SyncData>();
				for (int i = 0; i < relationshipsSyncData.size(); i++) {
					SyncData syncData = relationshipsSyncData.get(i);
					
					for (int j = 0; j < relationshipModules.length; j++) {
						SugarBean bean = relationshipModules[j];
						
						if(syncData.lhs_module.equalsIgnoreCase(bean.fields.get("lhs_module").value)
								&& syncData.rhs_module.equalsIgnoreCase(bean.fields.get("rhs_module").value)) {
							tempRelSyncData.add(syncData);
							downloadAndSyncFile(context, syncData.name, syncData.path);
						}
					}
				}
				relationshipsSyncData = tempRelSyncData;
				
				
			} catch (Exception e) {
				AlertHelper.logError(e);
			}
			
		}

		// setting count-down-latch
		if(syncAll) {
			latch = new CountDownLatch(modulesSyncData.size() + cstmModulesSyncData.size() + relationshipsSyncData.size());
		} else {
			latch = new CountDownLatch(modulesSyncData.size() + cstmModulesSyncData.size());
		}
		
		// performing file-based sync on Modules
		for(int i = 0; i < modulesSyncData.size(); i++) {
			SyncData data = modulesSyncData.get(i);
			downloadAndSyncFile(context, data.name, data.path);
		}

		// performing file-based sync on Custom Modules
		for(int i = 0; i < cstmModulesSyncData.size(); i++) {
			SyncData data = cstmModulesSyncData.get(i);
			downloadAndSyncFile(context, data.name + "_cstm", data.path);
		}
		
		// performing file-based sync on relationships.
		if(syncAll) {
			for (int i = 0; i < relationshipsSyncData.size(); i++) {
				SyncData data = relationshipsSyncData.get(i);
				downloadAndSyncFile(context, data.name, data.path);
			}
		}
		
//		try {
//			// Wait for the sync threads to complete.
//			latch.await();
//		} catch (InterruptedException e) {
//			AlertHelper.logError(e);
//		}
		
		// Loop to sync modified records from local database to the server.
		for(int i = 0; i < modules.length; i++) {
			syncModuleToServer(new SugarBean(context, (String) modules[i]), context, 0);
			
			if(UserPreferences.lastSync != null){
				//For deleted records
				syncModuleToServer(new SugarBean(context, (String) modules[i]), context, 1);
			}
		}
		
		//Sync Relationships
		if(syncAll) {
			for(int i=0; i<modules.length; i++){
				try {
					//Only sync used modules
					if(UserPreferences.moduleConfiguration.get((String)modules[i]).available){
						if(UserPreferences.useExtendedImport){
							syncBulkRelationshipsToServer((String)modules[i], context, 0);
	
							if(UserPreferences.lastSyncRelationShip != null){
								//For deleted records
								syncBulkRelationshipsToServer((String)modules[i], context, 1);
							}
						}else{
							syncRelationshipsToServer((String)modules[i], context, 0);
	
							if(UserPreferences.lastSyncRelationShip != null){
								//For deleted records
								syncRelationshipsToServer((String)modules[i], context, 1);
							}
						}
					}
				} catch (Exception e) {
					AlertHelper.logError(e);
					failedModules += "Relationships for " + modules[i] + ", ";
				}
			}
		}
		
		try {
			//Reload communicator
			SugarBean.loadCom(context, false, true);
			
			//Syncing team members' id, myday_counts
			if(ImportHelper.loadFromServer()){
				UserPreferences.save(context);
			}
		} catch (Exception e) {
			AlertHelper.logError(e);
		}

		return failedModules.trim().equalsIgnoreCase("") ? null : failedModules.substring(0, failedModules.length());
	}

/*
 * downloads file with given path and saves it with given module name.
 */
	public static void downloadAndSyncFile(final Context context, final String moduleName, final String path) {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
				try {
					DBClient dbClient = new DBClient(context);
					
					new DownloadHelper().downloadZippedFile(moduleName, path);
					
					File file = SDCardHelper.readFileFromCache("/Tamtam/SyncFiles/", moduleName.toLowerCase() + "_replace_into.txt");
					executeRawQuery(file, dbClient);
					
					SDCardHelper.deleteFromCache("/Tamtam/SyncFiles/", moduleName.toLowerCase() + "_replace_into.txt");
				} catch (Exception e) {
					AlertHelper.logError(e);
					failedModules += moduleName;
				}
				//latch.countDown();
//			}
//		}).start();
	}

/*
 * reads query string from text file and executes query on sqlite database
 */
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
		    	queries = text.toString().split("---IPHONE SYNC---");
			    for(int i = 0; i < queries.length; i++) {
			    	dbClient.executeRawQuery(queries[i]);
			    }
		    }
		}
	}

/*
 * performs sync on locally edited records in the given module...
 */
	private static void syncModuleToServer(SugarBean bean, Context context, int deleted) {
		String where = "(" + 
							bean.moduleName.toLowerCase() + ".date_modified>='" + UserPreferences.lastSync + "'" +
						" OR " + 
							bean.moduleName.toLowerCase() + ".date_entered>='" + UserPreferences.lastSync + "'" +
						")";
		
		//Log module name
		AlertHelper.logError(new Exception("Synchronizing " + bean.moduleName));
		
		//Loading communicator to get count of changed records from server/DB
		SugarBean.loadCom(context, false, true);
		
		if(bean.fields.get(DBConnection.SYNC_COLUMN) == null){
			return;
		}
		
		String dbWhere = " AND " + bean.moduleName.toLowerCase() + "." + DBConnection.SYNC_COLUMN + " IN ('1', '2')";
		
		try {
			int total = (int) bean.getRecordsCount(where + dbWhere, deleted);
			
			AlertHelper.logError(new Exception("Found total updated recrds from " + (bean.usingDB() ? "Local DB" : "Server") + " : " + total + ""));
			
			int retrieved = 0, failed = 0;
			for(int i=1; i<=total; i+=20){
				//loading communicator for local db
				SugarBean.loadCom(context, false, true);
				
				//Retrieving changed records
				SugarBean[] beans = bean.retrieveAll(where + dbWhere, "date_modified desc", i-1, 20, deleted, null);
				retrieved += beans.length;
				
				//Flip communicator to save data on server
				SugarBean.loadCom(context, true, false);
				
				//saving all beans to alternate location
				String[] ret = bean.saveAll(beans, true);
				for(int j=0; j<ret.length; j++){
					if(ret[j].equalsIgnoreCase("-1")) failed++;
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

	/*
	 * retrieving relationship defs from the local database
	 */
	public static SugarBean[] retrieveRelationshipDefs(Context context, int deleted) throws Exception {

		Relationship bean = new Relationship(context);
		SugarBean[] beans;
		//Loading DB Com
		SugarBean.loadCom(context, false, true);

		try{
			int total = (int) bean.getRecordsCount(null, deleted);

			//Retrieving changed records
			beans = bean.retrieveAll(null, "id desc", 0, total, deleted, null);
			defaultRelations = false;
			return beans;

		}catch (Exception e) {

			return null;
		}
	}

/*
 * Sync updated relationships between all modules
 */
	protected static void syncRelationshipsToServer(String moduleName, Context context, int deleted) throws Exception {
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
						String where = RelationshipHelper.getRelationshipWhere(relBean, moduleName, beans[j].fields.get("id").value, UserPreferences.lastSyncRelationShip, DBConnection.SYNC_COLUMN + " IN ('1', '2')", deleted);

							//Loading SOAP for 0 and DB for 1
							SugarBean.loadCom(context, false, true);

							bean = new SugarBean(context, relatedModule);

							total = (int) bean.getRecordsCount(where, 0);

							AlertHelper.logError(new Exception("Found total updated recrds from " + (bean.usingDB() ? "Local DB " : "Server ") + total));

							for(int k=1; k<=total; k+=20){
								//Loading SOAP for 0 and DB for 1
								SugarBean.loadCom(context, false, true);

								//Retrieving changed records
								SugarBean[] relatedBeans = bean.retrieveAll(where, "id desc", k-1, 20, 0, null);

								//Loading SOAP for 1 and DB for 0
								SugarBean.loadCom(context, true, true);

								for(int l=0 ; l<relatedBeans.length; l++){
									beans[j].setRelationship(relatedModule, relatedBeans[l].fields.get("id").value, true, 0);
								}
							}

						//resetting sync flag on join table
						new SugarBean(context, relBean.fields.get("join_table").value).resetSyncFlag(deleted);
					}
				}
			}
		}
	}

/*
 * sync bulk relationships to the server
 */
	protected static void syncBulkRelationshipsToServer(String moduleName, Context context, int deleted) throws Exception {
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

					//Loading DB communicator
					SugarBean.loadCom(context, false, true);

					String dbWhere = " AND " + DBConnection.SYNC_COLUMN + " IN ('1', '2')";

					SugarBean[] relBeans = relBean.retrieveAllRelationship(relBean.fields.get("relationship_name").value, where + dbWhere, deleted);

					AlertHelper.logError(new Exception("Found total updated recrds from " + (bean.usingDB() ? "Local DB " : "Server ") + relBeans.length));

					//Loading SOAP communicator
					SugarBean.loadCom(context, true, false);

					for(int k = 0; k < relBeans.length; k++){
						SugarBean relB = relBeans[k];

						Field id = bean.fields.get("id");
						id.value = relB.fields.get(moduleIdField).value;
						bean.fields.put("id", id);

						bean.setRelationship(relatedModule, relB.fields.get(relatedModuleIdField).value,true, deleted);
					}

					//resetting sync flag on join table
					new SugarBean(context, relBean.fields.get("join_table").value).resetSyncFlag(deleted);
				}
			}
		}
	}

}
