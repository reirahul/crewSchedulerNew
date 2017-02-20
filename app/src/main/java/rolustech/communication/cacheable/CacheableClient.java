package rolustech.communication.cacheable;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import rolustech.beans.UserPreferences;
import rolustech.communication.Communicator;
import rolustech.communication.db.DBClient;
import rolustech.communication.soap.SOAPClient;
import rolustech.communication.soap.SOAPClientV2;
import rolustech.helper.AlertHelper;
import rolustech.helper.SoapHelper;
import rolustech.tempStorage.SugarBeanContainer;

public class CacheableClient implements Communicator {
	private DBClient db;
	private SOAPClient soap;

	public CacheableClient(Context context) {
		db = new DBClient(context);
		
		if(!UserPreferences.loaded){
			UserPreferences.reLoadPrefernces(context);
		}
		
		if (UserPreferences.usingV2Soap) {
			soap = new SOAPClientV2(SoapHelper.generateUrlV2(UserPreferences.displayUrl));
		} else {
			soap = new SOAPClient(SoapHelper.generateUrl(UserPreferences.displayUrl));
		}
	}
	
	@Override
	public int login(String username, String password) {
		return 1;
	}
	
/*
 * return array list of all the records' name values pairs found for the provided criteria
 * if found noting returns empty array list
 */
	
	@Override
	public ArrayList<ArrayList<String[]>> getEntryList(String moduleName,String[] select_fields,String where,int total,int offset,String orderBy, int deleted,
			String[] relatedMods) throws Exception{
		//Caching returned results
		try{
			checkSession();
			ArrayList<ArrayList<String[]>> data = soap.getEntryList(moduleName, select_fields, where, total, offset, orderBy, deleted, null);
			cacheData(moduleName, data);
		}catch (Exception e) {
			AlertHelper.logError(e);
		}
		
		//Retrieving from DB now
		return db.getEntryList(moduleName, select_fields, where, total, offset, orderBy, deleted, null);
	}

	public  ArrayList<ArrayList<ArrayList<String[]>>> getEntryWorkOrder(String record_id) throws Exception{

		ArrayList<ArrayList<String[]>> data = new ArrayList<ArrayList<String[]>>();

		ArrayList records = new ArrayList();
		records.add(data);
		return records;
	}

	public String setValueEntry(String moduleName, String field_value, String record_id) throws Exception {

		return "-1";
	}

	public String setDocumentRevision(String documentID, String fileData, String fileName, String revision) throws Exception{

		return "1";
	}

	public HashMap<String, String> getDocumentRevision(String revisionID) throws Exception
	{
		HashMap<String, String> values = new HashMap<String, String>();
		return  values;
	}

	@Override
	public SugarBeanContainer getEntryListV2Sync(
			String moduleName, String[] select_fields, String where, int total,
			int offset, String orderBy, int deleted,
			String[] relatedMods) throws Exception {

		return new SugarBeanContainer();
	}
	
	/*
	 *  return array list of all the records' name values pairs for the provided select of the record associated with the provided id in the provided module if exists 
	 * otherwise null
	 */

	@Override
	public ArrayList<String[]> getEntry(String moduleName,String[] select_fields,String id, boolean isRelRequest) throws Exception {
		//Caching returned results
		try {
			checkSession();
			ArrayList<ArrayList<String[]>> cacheableData = new ArrayList<ArrayList<String[]>>();
			cacheableData.add(soap.getEntry(moduleName, select_fields, id, isRelRequest));
			cacheData(moduleName, cacheableData);
		} catch (Exception e) {
			AlertHelper.logError(e);
		}
		
		//Retrieving from DB now
		return db.getEntry(moduleName, select_fields, id, isRelRequest);
	}
	
/*
 * insert or updates the record in the provided module
 * returns id if successful otherwise -1
 */

	@Override
	public String setEntry(String moduleName, String []name, String []value, boolean newWithID, boolean performingSync, boolean isRelRequest) throws Exception {
		if(moduleName == null || name == null || value == null){
			return "-1";
		}
		
		//Saving in Server
		String serverId = "-1";
		try{
			if(!performingSync){
				checkSession();
				serverId = soap.setEntry(moduleName, name, value, newWithID, performingSync, isRelRequest);
			}
		} catch (Exception e) {
			AlertHelper.logError(e);
		}
		
		//If failed to save on server return -1
		if (serverId.equalsIgnoreCase("-1")) {
			return "-1";
		}
	
		// Updating id retrieved from server
		for (int j = 0; j < name.length; j++) {
			if (name[j].equalsIgnoreCase("id")) {
				value[j] = serverId;
				newWithID = db.getEntry(moduleName, new String[]{"id"}, serverId, isRelRequest).size() == 0;;
			}
		}
			
		//Saving into DB
		return db.setEntry(moduleName, name, value, newWithID, performingSync, isRelRequest);
	}
	
	/*
	* Calls setEntry for all records
	*/
	@Override
	public String[] setEntryList(String moduleName, Vector<String[]> nameLists, Vector<String[]> valueLists, boolean[] isNewWithID, boolean performingSync) throws Exception{
		String ids[] = new String[nameLists.size()];
		
		//Saving
		for(int i=0; i<nameLists.size(); i++){
			try {
				ids[i] = setEntry(moduleName, nameLists.get(i), valueLists.get(i), isNewWithID[i], performingSync, false);
			} catch (Exception e) {
				AlertHelper.logError(e);
				ids[i] = "-1";
			}
		}
		
		return ids;
	}

/*
 * returns the total record for the specified criteria
 */
	
	@Override
	public long getEntriesCount(String moduleName, String where, int deleted) throws Exception{
		return db.getEntriesCount(moduleName, where, deleted);
	}

/*
 * sets a relationship between the provided records
 */

	@Override
	public String setRelationship(String moduleName,String moduleID, String relatedModuleName, String relatedIDs, Vector<String[]> nameLists, Vector<String[]> valueLists, boolean performingSync, int deleted) throws Exception {
		if(moduleName == null || moduleID == null || relatedModuleName == null || relatedIDs == null){
			return "-1";
		}
		
		//Saving in Server
		String serverId = "-1";
		try{
			serverId = soap.setRelationship(moduleName, moduleID, relatedModuleName, relatedIDs, null, null, performingSync, deleted);
		} catch (Exception e) {
			AlertHelper.logError(e);
		}
		
		//If failed to save on server return -1
		if (serverId.equalsIgnoreCase("-1")) {
			return "-1";
		}
			
		//Saving into DB
		return db.setRelationship(moduleName, moduleID, relatedModuleName, relatedIDs, null, null, performingSync, deleted);
	}

/*
 * Converting the format of data and saving into db
 */
	
	private void cacheData(String moduleName, ArrayList<ArrayList<String[]>> data) {
		Vector<String[]> nameLists = new Vector<String[]>(); 
		Vector<String[]>valueLists = new Vector<String[]>();
		boolean isNewWithID[] = new boolean[data.size()];
		
		for(int i=0; i<data.size(); i++){
			String name[] = new String[data.get(i).size()];
			String value[] = new String[data.get(i).size()];
			
			String id = "";
			
			for(int j=0; j<data.get(i).size(); j++){
				name[j] = data.get(i).get(j)[0];
				value[j] = data.get(i).get(j)[1];
				
				if(name[j].equalsIgnoreCase("id")){
					id = value[j];
				}
			}
			
			nameLists.add(name);
			valueLists.add(value);
			isNewWithID[i] = false;
			
			try {
				isNewWithID[i] = db.getEntry(moduleName, new String[]{"id"}, id, false).size() == 0;
			} catch (Exception e) {
				AlertHelper.logError(e);
			}
		}
		
		try {
			db.setEntryList(moduleName, nameLists, valueLists, isNewWithID, true);
		} catch (Exception e) {
			AlertHelper.logError(e);
		}
	}

	@Override
	public void resetSyncFlag(String moduleName, int deleted) {
		db.resetSyncFlag(moduleName, deleted);
	}

	public void resetId(String moduleName, String recordId, String newRecordId){
		db.resetId(moduleName, recordId, newRecordId);
	}

	private void checkSession() {
		if(SOAPClient.getSess_id() == null){
			soap.login(UserPreferences.userName, UserPreferences.password);
		}
	}

	@Override
	public void deleteViaQuery(String moduleName, String where) throws Exception {
		throw new Exception("Removing records is not allowed in online with cache mode");
	}

	@Override
	public ArrayList<ArrayList<String[]>> getEntryListRelationship(String relationshipName, String where, String orderBy, int offset, String[] select_fields, int total, int deleted) throws Exception {
		return soap.getEntryListRelationship(relationshipName, where, orderBy, offset, select_fields, total, deleted);
	}

	@Override
	public String[] getRaltionships(String moduleName, String moduleID,
			String relatedModuleName, boolean performingSync, int deleted)
			throws Exception {
		// TODO Auto-generated method stub
		return new String[0];
	}

}
