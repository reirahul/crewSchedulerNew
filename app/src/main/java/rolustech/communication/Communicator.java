package rolustech.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import rolustech.tempStorage.SugarBeanContainer;


public abstract interface Communicator {

	/*
	 * login to sugar	
	 */	
	abstract int login(String username, String password);
	/*
	 * return array list of all the records' name values pairs found for the provided criteria
	 * if found noting returns empty array list
	 */	
	abstract ArrayList<ArrayList<String[]>> getEntryList(String moduleName, String[] select_fields, String where, int total, int offset, String orderBy, int deleted, String[] relatedModules) throws Exception;
	abstract SugarBeanContainer getEntryListV2Sync(String moduleName, String[] select_fields, String where, int total, int offset, String orderBy, int deleted, String[] relatedModules) throws Exception;
    abstract ArrayList<ArrayList<ArrayList<String[]>>> getEntryWorkOrder(String record_id) throws Exception;
	/*
	 *  return array list of all the records' name values pairs for the provided select of the record associated with the provided id in the provided module if exists 
	 * otherwise null
	 */

	abstract ArrayList<String[]> getEntry(String moduleName, String[] select_fields, String id, boolean isRelRequest) throws Exception;

	/*
	 * insert or updates the record in the provided module
	 * returns id if successful otherwise -1
	 */

	abstract String setEntry(String moduleName, String[] name, String[] value, boolean newWithID, boolean performingsync, boolean isRelRequest) throws Exception;


	/*
	 * CUSTOM CALL
	 * returns id if successful otherwise -1
	 */

	abstract HashMap<String, String> getDocumentRevision(String revisionID) throws Exception;

	/*
	 * CUSTOM CALL
	 * returns id if successful otherwise -1
	 */

	abstract String setValueEntry(String moduleName, String field_value, String record_id) throws Exception;


	/*
	 * CUSTOM CALL
	 * returns id if successful otherwise -1
	 */

	abstract String setDocumentRevision(String documentID, String fileData, String fileName, String revision) throws Exception;


	/*
	 * insert or updates the records in the provided module
	 * returns ids if successful otherwise -1
	 */
	abstract String[] setEntryList(String moduleName, Vector<String[]> nameLists, Vector<String[]> valueLists, boolean[] isNewWithID, boolean performingSync) throws Exception;
	
	/*
	 * returns the total record for the specified criteria
	 */

	abstract long getEntriesCount(String moduleName, String where, int deleted) throws Exception;

	/*
	 * sets a relationship between the provided records
	 */

	abstract String setRelationship(String moduleName, String moduleID, String relatedModuleName, String relatedIDs, Vector<String[]> nameLists, Vector<String[]> valueLists, boolean performingSync, int deleted) throws Exception;

	/*
	 * returns array of IDs for related array of mentioned module for a given record
	 */
	
	abstract String[] getRaltionships(String moduleName, String moduleID, String relatedModuleName, boolean performingSync, int deleted) throws Exception;
	
	/*
	 * Delete record with provided where clause	
	 */

	abstract void deleteViaQuery(String moduleName, String where) throws Exception;

	/*
	 * 	Resets the sync flags to 0
	 */
	
	abstract void resetSyncFlag(String moduleName, int deleted);

	/*
	 * 	Resets the sync flags to 0
	 */
	abstract void resetId(String moduleName, String recordId, String newRecordId);

	/*
	 * 	Return the list of all relationships
	 */
	abstract ArrayList<ArrayList<String[]>> getEntryListRelationship(String relationshipName, String where, String orderBy, int offset, String[] select_fields, int total, int deleted) throws Exception;
	
	
}
