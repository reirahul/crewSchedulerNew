package rolustech.communication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;

import rolustech.beans.UserPreferences;
import rolustech.communication.Communicator;
import rolustech.helper.AlertHelper;
import rolustech.helper.DateHelper;
import rolustech.tempStorage.SugarBeanContainer;

public class DBClient implements Communicator {
	private static SQLiteDatabase db;

	public DBClient(Context context) {
		db = DBConnection.getConnection(context);
	}

	/*
	 * login just for consistency b/w communicators	
	 */	
	@Override
	public int login(String username , String passworde){
		if(db == null){
			return 0;
		}
		return 1;
	}

	/*
	 * return array list of all the records' name values pairs found for the provided criteria
	 * if found noting returns empty array list
	 */

	@Override
	public ArrayList<ArrayList<String[]>> getEntryList(String moduleName,String[] select_fields,String where,int total,int offset,String orderBy, int deleted, 
			String[] relatedMods) throws Exception{



		ArrayList<ArrayList<String[]>> data = new  ArrayList<ArrayList<String[]>>();
        if(moduleName.equalsIgnoreCase("DocumentRevisions"))
            moduleName = "document_revisions";
		//Setting where clause
		if(where == null) where = "";

		if(!UserPreferences.useExtendedImport && relatedMods == null) {
			where = where.replace("_cstm", "");
		}

		if(!where.trim().equalsIgnoreCase("")){
			where += " AND ";
		}

		where += moduleName.toLowerCase() + ".deleted='" + deleted + "'";

		if(orderBy == null || orderBy.trim().equalsIgnoreCase(""))
			orderBy = "date_modified DESC";
		if(offset != -1 && total != -1){
			orderBy += " LIMIT " + offset + "," + total;
		}

		//Generate query and execute on DB
		Cursor cur = null;
		if(!UserPreferences.useExtendedImport && relatedMods == null && hasCustomTable(select_fields, where)) {
			cur = db.rawQuery(getExtendedQuery(select_fields, where, moduleName, orderBy), null);
		} else {
			cur = db.query(moduleName.toLowerCase(), select_fields, where, null, null, null, orderBy);
		}

		//remove all fields which are not in table
		if(select_fields != null){
			select_fields = filterFieldList(select_fields, moduleName);
		}

		if (cur != null && cur.getCount() > 0) {
			cur.moveToFirst();
			for (int i = 0; i < cur.getCount(); i++) {
				ArrayList<String[]> tmp = new ArrayList<String[]>();
				for(int j = 0; j < cur.getColumnCount(); j++) {
					String tmp1[] = new String[2];
					tmp1[0] = cur.getColumnName(j);
					tmp1[1] = cur.getString(j);

					//Filtering resultset
					if(select_fields == null || Arrays.asList(select_fields).contains(tmp1[0])){
						tmp.add(tmp1);
					}
				}
				cur.moveToNext();
				data.add(tmp);
			}
		}

		cur.close();

		return data;
	}

	@Override
	public SugarBeanContainer getEntryListV2Sync(
			String moduleName, String[] select_fields, String where, int total,
			int offset, String orderBy, int deleted, 
			String[] relatedMods) throws Exception {

		return new SugarBeanContainer();
	}

	public ArrayList<ArrayList<ArrayList<String[]>>> getEntryWorkOrder(String record_id) throws Exception{

        ArrayList<ArrayList<String[]>> data = new  ArrayList<ArrayList<String[]>>();

//        String where = " ro_crew_work_order.id = '"+record_id+"'";
//
//        //Generate query and execute on DB
//        Cursor cur = db.query("ro_crew_work_order", null, where, null, null, null, "");
//
//        //remove all fields which are not in table
//        if(select_fields != null){
//            select_fields = filterFieldList(select_fields, moduleName);
//        }
//
//        if (cur != null && cur.getCount() > 0) {
//            cur.moveToFirst();
//            for (int i = 0; i < cur.getCount(); i++) {
//                ArrayList<String[]> tmp = new ArrayList<String[]>();
//
//                for (int j = 0; j < cur.getColumnCount(); j++) {
//                    String tmp1[] = new String[2];
//                    tmp1[0] = cur.getColumnName(j);
//                    tmp1[1] = cur.getString(j);
//
//                    //Filtering resultset
//                    if(select_fields == null || Arrays.asList(select_fields).contains(tmp1[0])){
//                        tmp.add(tmp1);
//                    }
//                }
//                cur.moveToNext();
//                data.add(tmp);
//            }
//        }
//
//        cur.close();



		ArrayList records = new ArrayList();
		records.add(data);
//        records.add(list1);
//        records.add(list2);
		return records;
	}

	public String setValueEntry(String moduleName, String field_value, String record_id) throws Exception {

		return "-1";
	}

	public String setDocumentRevision(String documentID, String fileData, String fileName, String revision) throws Exception{

		//Generate query and execute on DB
		ContentValues values = new ContentValues();

		String id = "";
		long returnVal = -1;

		id = UUID.randomUUID().toString();

		values.put("id", id);
		values.put("document_id", documentID);
		values.put("filename", fileName);
		values.put("revision", revision);
        values.put("deleted", "0");

		//Means user created the record
		values.put(DBConnection.SYNC_COLUMN, "1");

		returnVal = db.insert("document_revisions", null, values);

		if(returnVal != -1){
			return id;
		}

		return "-1";
	}

	public String updateDocumentRevision(String documentID, String recordId, String fileName, String revision) throws Exception{

		//Generate query and execute on DB
		ContentValues values = new ContentValues();

		long returnVal = -1;

		values.put("id",recordId);
		values.put("document_id", documentID);
		values.put("filename", fileName);
		values.put("revision", revision);
		values.put("deleted", "0");

		returnVal = db.insert("document_revisions", null, values);

		if(returnVal != -1){
			return recordId;
		}

		return "-1";
	}

	public HashMap<String, String> getDocumentRevision(String revisionID) throws Exception
	{
		HashMap<String, String> values = new HashMap<String, String>();
		return  values;
	}

	/*
	 *  return array list of all the records' name values pairs for the provided select of the record associated with the provided id in the provided module if exists 
	 * otherwise null
	 */

	@Override
	public ArrayList<String[]> getEntry(String moduleName,String[] select_fields,String id, boolean isRelRequest) throws Exception {
		ArrayList<String[]> data = new ArrayList<String[]>();
        if(moduleName.equalsIgnoreCase("DocumentRevisions"))
            moduleName = "document_revisions";
		String where = moduleName.toLowerCase() + ".id='"+id+"'";

		//Generate query and execute on DB
		Cursor cur = null;
		if(UserPreferences.useExtendedImport && !isRelRequest && hasCustomTable(select_fields, where)) {
			cur = db.rawQuery(getExtendedQuery(null, where, moduleName, null), null);
		} else {
			cur = db.query(moduleName.toLowerCase(), null, where, null, null, null, null);
		}

		//remove all fields which are not in table
		if(select_fields != null){
			select_fields = filterFieldList(select_fields, moduleName);
		}

		if(cur != null && cur.getCount()>0){
			cur.moveToFirst();
			for(int j=0;j<cur.getColumnCount();j++){					
				String tmp[] = new String[2];
				tmp[0] = cur.getColumnName(j);
				tmp[1] = cur.getString(j);

				//Filtering resultset
				if(select_fields == null || Arrays.asList(select_fields).contains(tmp[0])){
					data.add(tmp);
				}
			}
		}

		cur.close();

		return data;
	}

	/**
	 * find sync value
	 */
		private String syncValue(String[] name,String[] value){
			String syncValue ="0";
			for (int i = 0; i < name.length; i++) {
				if(name[i].equalsIgnoreCase(DBConnection.SYNC_COLUMN)){
					syncValue = value[i];
				}
			}
			return syncValue;
		}
	
	/*
	 * insert or updates the record in the provided module
	 * returns id if successful otherwise -1
	 */

	@Override
	public String setEntry(String moduleName, String []name, String []value, boolean newWithID, boolean performingsync, boolean isRelRequest) throws Exception {
		if(moduleName == null || name == null || value == null){
			return "-1";
		}

        if(moduleName.equalsIgnoreCase("DocumentRevisions"))
            moduleName = "document_revisions";

		//remove all fields which are not in table
		String newNameValues[][] = filterNameValueList(name, value, moduleName);
		name = newNameValues[0];
		value = newNameValues[1];

		//Generate query and execute on DB
		ContentValues values = new ContentValues();
		ContentValues cstmValues = new ContentValues();

		String id = "";
		boolean update = !newWithID;
		long returnVal = -1;

		for(int j=0;j<name.length;j++){
			if(name[j].equalsIgnoreCase("id")){
				id = value[j];

				if(id.equalsIgnoreCase("")){
					//Create 36 chars UUID in case of create
					id = value[j] = UUID.randomUUID().toString();
					newWithID = true;
					update = false;
				}
			}

			//preparing content values for cstm and main table
			if(UserPreferences.useExtendedImport && !isRelRequest && name[j].endsWith("_c")) {
				//Adding _c fields to cstm table
				cstmValues.put(name[j], value[j]);
			}else{
				values.put(name[j], value[j]);
			}
		}

		if(cstmValues.size() > 0){
			cstmValues.put("id_c", id);
		}

		if(update){
			//For checking if the update is on the record which is already a new record if so then don't change the sync value from 1 to 2,
			//because if we change it then this record would indicate an update on the record which exists in server
			//but actually it does not ,and thus sync will fail
			String syncValue = syncValue(name, value);
			//If new with id is false means user edited the record
			String  syncFlag = getSyncValue(moduleName, id);
			if(!performingsync && !syncValue.equalsIgnoreCase("1") && !syncFlag.equalsIgnoreCase("1")){
				values.put(DBConnection.SYNC_COLUMN, "2");
			}

			returnVal = db.update(moduleName.toLowerCase(), values, "id='" + id + "'", null);

			//if no row affected return -1
			if(returnVal == 0) returnVal = -1;

			//Updating cstm table on success
			if(returnVal != -1 && cstmValues.size() > 0){
				db.update(moduleName.toLowerCase() + "_cstm", cstmValues, "id_c='" + id + "'", null);
			}

			///// SAVE IN ONLINE CASE CHECK
			if(performingsync && returnVal == -1){
				returnVal = db.insert(moduleName.toLowerCase(), null, values);

				//Updating cstm table on success
				if(returnVal != -1 && cstmValues.size() > 0){
					db.insert(moduleName.toLowerCase() + "_cstm", null, cstmValues);
				}
			}


		}else{
			//If new with id is true means user created the record
			if(!performingsync){
				values.put(DBConnection.SYNC_COLUMN, "1");
			}

			returnVal = db.insert(moduleName.toLowerCase(), null, values);

			//Updating cstm table on success
			if(returnVal != -1 && cstmValues.size() > 0){
				db.insert(moduleName.toLowerCase() + "_cstm", null, cstmValues);
			}
		}

		if(returnVal != -1){
			return id;
		}

		return "-1";
	}

	/*
	 * Calls setEntry for all records
	 */
	@Override
	public String[] setEntryList(String moduleName, Vector<String[]> nameLists, Vector<String[]> valueLists, boolean[] isNewWithID, boolean performingSync) throws Exception{
		String ids[] = new String[nameLists.size()];
        if(moduleName.equalsIgnoreCase("DocumentRevisions"))
            moduleName = "document_revisions";
		for (int i=0; i<nameLists.size(); i++) {
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
		//Removing _cstm
		long count;
        if(moduleName.equalsIgnoreCase("DocumentRevisions"))
            moduleName = "document_revisions";
		if(where == null) where = "";

		if(!UserPreferences.useExtendedImport) {
			where = where.replace("_cstm", "");
		}

		if(!where.trim().equalsIgnoreCase("")){
			where += " AND ";
		}

		where += moduleName.toLowerCase() + ".deleted='" + deleted + "'";

		//Generate query and execute on DB
		Cursor cur = null;
		if(UserPreferences.useExtendedImport && hasCustomTable(null, where)) {
			cur = db.rawQuery(getExtendedQuery(null, where, moduleName, null), null);
			} else {
			cur = db.query(moduleName.toLowerCase(),null, where, null, null, null, null);

		}

		count= cur.getCount();
		cur.close();
		return count;
	}

	/*
	 * sets a relationship between the provided records
	 */

	@Override
	public String setRelationship(String moduleName,String moduleID, String relatedModuleName, String relatedID, Vector<String[]> nameLists, Vector<String[]> valueLists, boolean performingSync, int deleted) throws Exception {
		//Getting relationship definitions
		String where = 	"(lhs_module='" + moduleName + "' AND rhs_module='" + relatedModuleName + "')" +
				" OR " +
				"(lhs_module='" + relatedModuleName + "' AND rhs_module='" + moduleName + "')";

		String []fields = new String[UserPreferences.moduleConfiguration.get("Relationships").fields.keySet().size()];
		UserPreferences.moduleConfiguration.get("Relationships").fields.keySet().toArray(fields);

		ArrayList<ArrayList<String[]>> relDefs = getEntryList("Relationships", fields, where, -1, -1, "id desc", 0, null);

		if(relDefs == null || relDefs.size() == 0){
			throw new Exception("No relationship found");
		}

		Hashtable<String, String> relDef = new Hashtable<String, String>();

		for(int i=0; i<relDefs.get(relDefs.size()-1).size(); i++){
			relDef.put(relDefs.get(relDefs.size()-1).get(i)[0], relDefs.get(relDefs.size()-1).get(i)[1]);
		}

		//Adding relationship

		//no join table
		if(relDef.get("join_table").equalsIgnoreCase("NULL") || relDef.get("join_table").equalsIgnoreCase("")){
			String id = moduleID, relId = relatedID;
			if(relDef.get("rhs_module").equalsIgnoreCase(moduleName)){
				id = relatedID;
				relId = moduleID;
			}

			String[] names = new String[]{"id", relDef.get("rhs_key")};
			String[] values = new String[]{relId, id};

			if(!relDef.get("relationship_role_column").equalsIgnoreCase("NULL") && !relDef.get("relationship_role_column").equalsIgnoreCase("")){
				names = new String[]{"id", relDef.get("rhs_key"), relDef.get("relationship_role_column")};
				values = new String[]{relId, id, relDef.get("relationship_role_column_value")};
			}

			return setEntry(relDef.get("rhs_module"), names, values, false, performingSync, false);
		}

		//has join table
		where = "(" + relDef.get("join_key_lhs") + "='" + moduleID + "' AND " + relDef.get("join_key_rhs") + "='" + relatedID + "')" +
				" OR " +
				"(" + relDef.get("join_key_lhs") + "='" + relatedID + "' AND " + relDef.get("join_key_rhs") + "='" + moduleID + "')";

		ArrayList<ArrayList<String[]>> ids = getEntryList(relDef.get("join_table"), new String[]{"id"}, where, -1, -1, "id desc", 0, null);

		ArrayList<String> names = new ArrayList<String>(), values = new ArrayList<String>();

		//Adding field names - order is important
		names.add("id");
		names.add(relDef.get("join_key_lhs"));
		names.add(relDef.get("join_key_rhs"));
		names.add(DBConnection.SYNC_COLUMN);
		names.add("date_modified");
		names.add("deleted");

		//Adding value of id
		if(ids != null && ids.size() > 0){
			values.add(ids.get(0).get(0)[1]);
		}else{
			values.add("");
		}

		//Adding value of join_key_lhs, join_key_rhs
		if(relDef.get("lhs_module").equalsIgnoreCase(moduleName)){
			values.add(moduleID);
			values.add(relatedID);
		}else{
			values.add(relatedID);
			values.add(moduleID);
		}

		//Adding value of sync
		if(!performingSync){
			if(ids != null && ids.size() > 0){
				values.add("2");
			}else{
				values.add("1");
			}
		}else{
			values.add("0");
		}

		//Adding value of date_modified
		values.add(DateHelper.getCurrentDbDateTime());

		//Adding value of deleted
		values.add("" + deleted);

		String[] name = new String[names.size()], value = new String[values.size()];

		names.toArray(name);
		values.toArray(value);


		return setEntry(relDef.get("join_table"), name, value, false, performingSync, true);
	}

	@Override
	public void resetSyncFlag(String moduleName, int deleted) {
		if(moduleName.equalsIgnoreCase("DocumentRevisions"))
			moduleName = "document_revisions";
		db.execSQL("UPDATE " + moduleName.toLowerCase() + " SET " + DBConnection.SYNC_COLUMN + "='0' WHERE deleted='" + deleted + "'");
	}

	@Override
	public void resetId(String moduleName, String recordId, String newRecordId) {
		db.execSQL("UPDATE " + moduleName.toLowerCase() + " SET " + "id" + "='"+ newRecordId + "',"+ DBConnection.SYNC_COLUMN + "='0'" + "WHERE id='" + recordId + "'");
	}

    public void uodateDocRevisionId(String moduleName, String recordId, String revRecId) {
        db.execSQL("UPDATE " + moduleName.toLowerCase() + " SET " + "document_revision_id" + "='"+ revRecId + "',"+ DBConnection.SYNC_COLUMN + "='0'" + "WHERE id='" + recordId + "'");
    }

    public void syncDocRevId(String moduleName, String recordId, String revRecId) {
        db.execSQL("UPDATE " + moduleName.toLowerCase() + " SET " + "document_revision_id" + "='"+ revRecId + "' WHERE id='" + recordId + "'");
    }

	@Override
	public void deleteViaQuery(String moduleName, String where) throws Exception {
		db.delete(moduleName.toLowerCase(), where, null);
	}

	public void creatRelationshipTable(String tableName, String []fields) throws Exception {
		String query = "CREATE TABLE IF NOT EXISTS "+ tableName + " ( id TEXT, " + DBConnection.SYNC_COLUMN + " TEXT,";

		for(String field : fields){
			if(!field.equalsIgnoreCase("NULL") && !field.equalsIgnoreCase("") ){
				query += field + " TEXT,";
			}
		}

		query +=  " date_modified TEXT, deleted TEXT);";

		db.execSQL(query);

	}

	@Override
	public ArrayList<ArrayList<String[]>> getEntryListRelationship(
			String relationshipName, String where, String orderBy, int offset,
			String[] select_fields, int total, int deleted) throws Exception {

		ArrayList<ArrayList<String[]>> relDefs = getEntryList("Relationships", null, "relationship_name='" + relationshipName + "'", -1, -1, "id desc", 0, null);

		if(relDefs == null || relDefs.size() == 0){
			throw new Exception("No relationship found");
		}

		Hashtable<String, String> relDef = new Hashtable<String, String>();

		for(int i=0; i<relDefs.get(0).size(); i++){
			relDef.put(relDefs.get(0).get(i)[0], relDefs.get(0).get(i)[1]);
		}

		if(relDef.get("join_table").equalsIgnoreCase("NULL") || relDef.get("join_table").equalsIgnoreCase("")){
			throw new Exception("No Join table");
		}

		return getEntryList(relDef.get("join_table"), select_fields, where, total, offset, orderBy, deleted, null);
	}

	private boolean hasCustomTable(String[] select_fields, String where) {
		if(where != null && where.contains("_cstm")) {
			return true;
		}

		if(select_fields != null){
			for(int i=0; i<select_fields.length; i++) {
				if(select_fields[i].endsWith("_c")) {
					return true;
				}
			}
		}

		return false;
	}

	private String getExtendedQuery(String[] select_fields, String whereClause, String moduleName, String orderBy) {
		String query = "SELECT ";

		if(select_fields != null){
			String sep = "";
			for(int i=0; i<select_fields.length; i++){
				query += sep + moduleName.toLowerCase();
				if(select_fields[i].endsWith("_c")){
					query += "_cstm";
				}

				query += "." + select_fields[i];

				sep = ",";
			}
		}
		else{
			query += "*";
		}

		query += " FROM " + moduleName.toLowerCase() + 
				" LEFT JOIN " + moduleName.toLowerCase() + "_cstm" +
				" ON " + moduleName.toLowerCase() + ".id=" + moduleName.toLowerCase() + "_cstm.id_c" +
				" WHERE " + whereClause;

		if(orderBy != null){
			query += " ORDER BY " + orderBy;
		}

		return query;
	}

	private String[] filterFieldList(String[] select_fields, String moduleName) {
		ArrayList<String> newFields = new ArrayList<String>();

		//Getting all columns from tables
		ArrayList<String> dbFields = getColumns(moduleName); 

		//Getting all columns from custom tables
		if (hasCustomTable(select_fields, null)) {
			dbFields.addAll(getColumns(moduleName + "_cstm"));
		}

		//Filtering fields
		for (int i=0; i<select_fields.length; i++) {
			if (dbFields.contains(select_fields[i])) {
				newFields.add(select_fields[i]);
			}
		}

		//Converting to array
		String[] fields = new String[newFields.size()];
		newFields.toArray(fields);

		return fields;
	}

	private String[][] filterNameValueList(String[] name, String[] value, String moduleName) {
		//Getting all columns from tables
		ArrayList<String> dbFields = getColumns(moduleName); 

		//Getting all columns from custom tables
		if (hasCustomTable(name, null)) {
			dbFields.addAll(getColumns(moduleName + "_cstm"));
		}

		//Filtering fields
		ArrayList<String> newNames = new ArrayList<String>();
		ArrayList<String> newValues = new ArrayList<String>();

		for (int i=0; i<name.length; i++) {
			if (dbFields.contains(name[i])) {
				newNames.add(name[i]);
				newValues.add(value[i]);
			}
		}

		//Converting to array
		String[] names = new String[newNames.size()];
		newNames.toArray(names);

		String[] values = new String[newValues.size()];
		newValues.toArray(values);

		return new String[][]{names, values};
	}


	private ArrayList<String> getColumns(String tableName) {
		ArrayList<String> columns = new ArrayList<String>();

		Cursor cur = db.rawQuery("pragma table_info(" + tableName + ")", null);
		if (cur != null && cur.getCount() > 0) {
			cur.moveToFirst();
			for (int i=0; i<cur.getCount(); i++) {
				for (int j=0; j<cur.getColumnCount(); j++) {
					if(cur.getColumnName(j).equalsIgnoreCase("name")){
						columns.add(cur.getString(j));
						break;
					}
				}
				cur.moveToNext();
			}
		}
		cur.close();

		return columns;
	}

	/*
	 */
	public void executeRawQuery(String query) {
		db.execSQL(query);
	}

	@Override
	public String[] getRaltionships(String moduleName, String moduleID,
			String relatedModuleName, boolean performingSync, int deleted)
			throws Exception {
		String[] idsArr = null;
		try {
			Cursor cur = db.rawQuery("Select contact_id from calls_contacts where call_id='"+moduleID+"'", null);
			idsArr = new String[cur.getCount()];
			if (cur != null && cur.getCount() > 0) {
				cur.moveToFirst();
				for(int i=0;i<cur.getCount();i++){
					idsArr[i] = cur.getString(i);
					}
				}
			cur.close();
			
		} catch (Exception e) {
			
		}
		return idsArr;
	}

	public String getSyncValue(String moduleName, String moduleID) throws Exception {
		String SyncVal = null;
		try {
			Cursor cur = db.rawQuery("Select " +DBConnection.SYNC_COLUMN+ " from " +moduleName.toLowerCase()+ " where id='"+moduleID+"'", null);
			if (cur != null && cur.getCount() > 0) {
				cur.moveToFirst();
				for(int i=0;i<cur.getCount();i++){
					SyncVal = cur.getString(i);
				}
			}
			cur.close();

		} catch (Exception e) {

		}

		return SyncVal;
	}

}
