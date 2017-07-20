package rolustech.beans;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;

import com.iconsolutions.helper.UserPreferences;

import org.kobjects.base64.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import rolustech.communication.CommunicationFactory;
import rolustech.communication.Communicator;
import rolustech.communication.db.DBClient;
import rolustech.communication.db.DBConnection;
import rolustech.communication.soap.SOAPClient;
import rolustech.communication.soap.SOAPClientV2;
import rolustech.helper.AlertHelper;
import rolustech.helper.DateHelper;
import rolustech.helper.ImageCacheHelper;
import rolustech.helper.NetworkHelper;
import rolustech.helper.RelationshipHelper;
import rolustech.tempStorage.SugarBeanContainer;

public class SugarBean{
	protected static Communicator com = null;
	protected transient Context context = null;

	public Hashtable<String, Field> fields;
	public String moduleName;

	public SugarBean(){
		
	}
	/*
	 * Constructor to set communicator from factory
	 */
	public SugarBean(Context context, String moduleName){
		if(com == null || (NetworkHelper.isAvailable(context) && com instanceof DBClient) || (!NetworkHelper.isAvailable(context) && com instanceof SOAPClient)){
			com = CommunicationFactory.getCommunicator(context, false, false);
		}

		this.context = context;
		this.moduleName = moduleName;
		fields = new Hashtable<String, Field>();

		// Add Fields
		fields.put(DBConnection.SYNC_COLUMN, new Field(DBConnection.SYNC_COLUMN, "bool", "", null, "", "0", "", "", "", false));

		//Copying fields from UserPrefernces
		ModuleConfig config = UserPreferences.moduleConfiguration.get(moduleName);
		if(config != null && config.fields != null){
			Enumeration<Field> tmp = config.fields.elements();
			while(tmp.hasMoreElements()){
				Field field = tmp.nextElement();
				fields.put(field.name, new Field(field.name, field.type, field.dbType, field.options, field.label, "", field.relatedModule, field.idName, field.typeName, field.required));
			}
		}
	}

	/*
	 * initiate soap communicatior
	 */
	public static void loadCom(Context context, boolean liveCom, boolean offlineCom){
		com = CommunicationFactory.getCommunicator(context, liveCom, offlineCom);
	}

	/*
	 * Delete All records in the module
	 */
	public void removeAll() throws Exception {
		com.deleteViaQuery(moduleName, null);
	}

	/*
	 * Delete record with provided Id
	 */
	public boolean markDeleted(boolean performingSync) throws Exception {
		if(moduleName != null && com !=  null){
			if(fields.get("id") != null && !fields.get("id").value.equalsIgnoreCase("")){
				if(fields.get("deleted") != null){
					updateFieldValue("deleted", "1");
					return !save(performingSync).equalsIgnoreCase("-1");
				}
			}
		}

		if(moduleName == null) throw new Exception("Module is null");
		else if(com == null) throw new Exception("Com is null");
		else if(fields.get("id") == null || fields.get("id").value.equalsIgnoreCase("")) throw new Exception("Bean is empty");
		else  throw new Exception("Missing deleted field");
	}

	/*
	 * Retirve and set values of all fields for the provided id
	 */
	public void retrieve(String id) throws Exception{
		if(moduleName != null && com !=  null){
			if(id != null){
				String sel[] = new String[fields.size()];

				int i=0;
				Enumeration<Field> tmp = fields.elements();
				while(tmp.hasMoreElements()){
					sel[i++] = tmp.nextElement().name;
				}

				ArrayList<String[]> nameValPair = com.getEntry(moduleName, sel, id, false);

				for(i=0; i<nameValPair.size(); i++){
					String name = nameValPair.get(i)[0];

					if(nameValPair.get(i)[1] != null){
						setFieldValue(name, nameValPair.get(i)[1]);
					}
				}

				//if loading from DB
				if(com instanceof DBClient && !moduleName.equalsIgnoreCase("Users")){
					Field field = fields.get("assigned_user_id");
					if(field != null)
						setFieldValue("assigned_user_name", getUserFromID(field.value));
					field = fields.get("modified_user_id");
					if(field != null)
						setFieldValue("modified_by_name", getUserFromID(field.value));
					field = fields.get("created_by");
					if(field != null)
						setFieldValue("created_by_name", getUserFromID(field.value));
				}

				return;
			}
		}
		if(moduleName == null) throw new Exception("Module is null");
		else if(com == null) throw new Exception("Com is null");
		else throw new Exception("Id is null");
	}

	public String setRelationship(String relatedModule, String relatedId, boolean performingSync, int deleted){
		try{
			return com.setRelationship(moduleName, fields.get("id").value, relatedModule, relatedId, null, null, performingSync, deleted);
		}catch(Exception e){
			AlertHelper.logError(e);
			try{
				return com.setRelationship(relatedModule, relatedId, moduleName, fields.get("id").value, null, null, performingSync, deleted);
			}catch(Exception e1){
				AlertHelper.logError(e1);
				return null;
			}
		}
	}

	/*
	 * Returns all the beans which satisfies the provided criteria
	 */
	public SugarBean[] retrieveAll(String where, String orderBy,int offset,int size, int deleted, String relatedModule) throws Exception {
		if(moduleName != null && com !=  null){
			String sel[] = new String[fields.size()];

			int i=0;
			Enumeration<Field> tmp = fields.elements();
			while(tmp.hasMoreElements()){
				sel[i++] = tmp.nextElement().name;
			}

			if(orderBy != null && orderBy.trim().length() > 0){
				orderBy += ", id ASC";
			}

			String modName = moduleName;
			String [] relatedModules = null;
			if(relatedModule != null && !relatedModule.trim().equalsIgnoreCase("")) {
				relatedModules = new String[]{relatedModule};
				modName = relatedModule;
			}
			
			ArrayList<ArrayList<String[]>> records = com.getEntryList(moduleName, sel, where, size, offset, orderBy, deleted, relatedModules);
			if(records != null && records.size() > 0){
				SugarBean beans[] = new SugarBean[records.size()];
				for(i=0; i<records.size(); i++){
					ArrayList<String[]> record = records.get(i);
					beans[i] = new SugarBean(context, modName);
					for(int j=0; j<record.size(); j++){
						String name = record.get(j)[0];

						if(record.get(j)[1] != null){
							Field fiel = beans[i].fields.get("related_to");
							beans[i].setFieldValue(name, record.get(j)[1]);
						}
					}
					
					//if loading from DB
					if(com instanceof DBClient && !modName.equalsIgnoreCase("Users")){
						Field field = beans[i].fields.get("assigned_user_id");
						if(field != null)
							beans[i].setFieldValue("assigned_user_name", getUserFromID(field.value));
						field = beans[i].fields.get("modified_user_id");
						if(field != null)
							beans[i].setFieldValue("modified_by_name", getUserFromID(field.value));
						field = beans[i].fields.get("created_by");
						if(field != null)
							beans[i].setFieldValue("created_by_name", getUserFromID(field.value));
					}
				}

				return beans;
			}
		}

		if(moduleName == null) throw new Exception("Module is null");
		else if(com == null) throw new Exception("Com is null");
//		else throw new Exception("No record Found");
		else return new SugarBean[0];
	}

	/*
	 * Returns all the beans which satisfies the provided criteria
	 */	

	/****************************************************************************************************************/
	public SugarBeanContainer retrieveAllV2Sync(String where, String orderBy,int offset,int size, int deleted, String[] relatedModules) throws Exception {
		if(moduleName != null && com !=  null){
			String sel[] = new String[fields.size()];

			int i=0;
			Enumeration<Field> tmp = fields.elements();
			while(tmp.hasMoreElements()){
				sel[i++] = tmp.nextElement().name;
			}

			if(orderBy != null && orderBy.trim().length() > 0){
				orderBy += ", id ASC";
			}
			
			

			//ArrayList<ArrayList<String[]>> records = com.getEntryListV2(moduleName, sel, where, size, offset, orderBy, deleted, isRelated, relateModuleFields);
			SugarBeanContainer beansContainer = com.getEntryListV2Sync(moduleName, sel, where, size, offset, orderBy, deleted, relatedModules);
			ArrayList<ArrayList<String[]>> records = beansContainer.list;

			if(records != null){
				SugarBean beans[] = new SugarBean[records.size()];
				for(i=0; i<records.size(); i++){
					ArrayList<String[]> record = records.get(i);
					beans[i] = new SugarBean(context, moduleName);
					for(int j=0; j<record.size(); j++){
						String name = record.get(j)[0];

						if(record.get(j)[1] != null){
							beans[i].setFieldValue(name, record.get(j)[1]);
						}
					}

					//if loading from DB
					if(com instanceof DBClient && !moduleName.equalsIgnoreCase("Users")){
						Field field = beans[i].fields.get("assigned_user_id");
						if(field != null)
							beans[i].setFieldValue("assigned_user_name", getUserFromID(field.value));
						field = beans[i].fields.get("modified_user_id");
						if(field != null)
							beans[i].setFieldValue("modified_by_name", getUserFromID(field.value));
						field = beans[i].fields.get("created_by");
						if(field != null)
							beans[i].setFieldValue("created_by_name", getUserFromID(field.value));
					}

				}

				beansContainer.beans = beans;

				return beansContainer;
			}
		}

		if(moduleName == null) throw new Exception("Module is null");
		else if(com == null) throw new Exception("Com is null");
		else throw new Exception("No record Found");
	}

	
	/*
	 * Returns all the beans of relationship
	 */
	public SugarBean[] retrieveAllRelationship(String relationshipName,String where, int deleted) throws Exception {
		if(com !=  null){
			ArrayList<ArrayList<String[]>> records = com.getEntryListRelationship(relationshipName, where, null, -1, null, -1, deleted);

			if(records != null){
				SugarBean beans[] = new SugarBean[records.size()];
				for(int i = 0; i<records.size(); i++){
					ArrayList<String[]> record = records.get(i);

					beans[i] = new SugarBean(context, null);

					for(int j=0; j<record.size(); j++){
						String name = record.get(j)[0];
						String value = record.get(j)[1];
						if(value == null){
							value= "";
						}

						beans[i].fields.put(name, new Field(name, "", "", null, "", value, "", "", "", false));
					}
				}

				return beans;
			}
		}

		if(com == null) throw new Exception("Com is null");
		else throw new Exception("No records Found");
	}

	/*
	 * Returns the count of records for the provided criteria 
	 */
	public long getRecordsCount(String where, int deleted) throws Exception{
		return com.getEntriesCount(moduleName, where, deleted);

	}

	/*
	 * Updates additional fields
	 */
	public void updateAdditionalFields(boolean performingSync){
		//Setting date_modified to today if record is saved not synchronized
		if(!performingSync){
			updateFieldValue("date_modified", DateHelper.getCurrentDbDateTime());

			if(UserPreferences.userID != null){
				updateFieldValue("modified_user_id", UserPreferences.userID);
			}
		}

		//Setting fields if record is new
		if(fields.get("id").value.trim().equalsIgnoreCase("")){
			//Adding date entered
			updateFieldValue("date_entered", DateHelper.getCurrentDbDateTime());

			//Adding created_by
			if(UserPreferences.userID != null){
				updateFieldValue("created_by", UserPreferences.userID);
			}

			//Adding deleted
			updateFieldValue("deleted", "0");
		}
	}

	/*
	 * returns name arrays of a bean
	 */
	public String[] getNameArray(boolean performingSync){
		String name_tmp[] = new String[fields.size()];

		int i=0;
		Enumeration<Field> tmp = fields.elements();
		while(tmp.hasMoreElements()){
			Field field = tmp.nextElement();
			if(field.updated || performingSync || field.name.equalsIgnoreCase("id") || field.name.equalsIgnoreCase("sync")){
				name_tmp[i] = field.name;
				i++;
			}
		}

		String name[] = new String[i];

		for(int j=0; j<i; j++){
			name[j] = name_tmp[j];
		}

		return name;
	}

	/*
	 * returns name arrays of a bean
	 */
	public String[] getValueArray(boolean performingSync){
		String value_tmp[] = new String[fields.size()];

		int i=0;
		Enumeration<Field> tmp = fields.elements();
		while(tmp.hasMoreElements()){
			Field field = tmp.nextElement();
			if(field.updated || performingSync || field.name.equalsIgnoreCase("id") || field.name.equalsIgnoreCase("sync")){
				if(field.type.contains("enum") && field.options != null){
					value_tmp[i] = field.getKey();
				}else{
					value_tmp[i] = field.value;
				}

				i++;
			}
		}

		String value[] = new String[i];

		for(int j=0; j<i; j++){
			value[j] = value_tmp[j];
		}

		return value;
	}

	/*
	 * Save data of the bean in DB/Server
	 */
	public String save(boolean performingSync) throws Exception  {
		if(com == null) throw new Exception("com is null");

		updateAdditionalFields(performingSync);

		//Updates the id value in beans for viewing
		String id = com.setEntry(
				moduleName, 
				getNameArray(performingSync), 
				getValueArray(performingSync), 
				isNewWithID(performingSync), 
				performingSync,
				false
				);

		if(NetworkHelper.isAvailable(context) && com instanceof SOAPClient){
			///// SAVING IN OFFLINE DATABASE TOO
			loadCom(context, false, true);
			//Updates the id value in beans for viewing
			String id1 = com.setEntry(
					moduleName,
					getNameArray(performingSync),
					getValueArray(performingSync),
					isNewWithID(performingSync),
					true,
					false
			);

			loadCom(context, true, false);
		}

		if(!id.equalsIgnoreCase("-1")){
			updateFieldValue("id", id);
		} 

		return id;
	}

	/*
	 * Set the note attachment of the bean in DB/Server
	 */
	public String setNoteAttachment(File file) throws Exception {
		if(com == null) throw new Exception("com is null");

		if (file == null) {
			return "-1";
		}

		String id = "";

		Bitmap bitmap = ImageCacheHelper.readFromCache(file);
		bitmap = Bitmap.createScaledBitmap(bitmap, 480, 500, true);
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 95, bao);
		byte [] ba = bao.toByteArray();
		updateAdditionalFields(false);

		//Updates the id value in beans for viewing
		if(UserPreferences.mode == UserPreferences.LIVEDATA) {
			id = ((SOAPClient) com).setNoteAttachment(getFieldValue("id"), file.getName(), Base64.encode(ba));
		} else {
			ImageCacheHelper.writeToCache(bitmap, UserPreferences.APP_NAME, file.getName());
			id = "1";
		}
		System.gc();
		bao.close();

		if(!id.equalsIgnoreCase("-1")){
			updateFieldValue("filename", file.getName());
		}

		return id;
	}

	/*
	 * Get note attachment of the bean in DB/Server
	 */
	public String[] getNoteAttachment(String moduleId) throws Exception  {
		if(com == null) throw new Exception("com is null");
		
		updateAdditionalFields(false);
		
		String[] file = new String[2];
		
		switch (UserPreferences.mode) {
		case UserPreferences.LIVEDATA:
			if(UserPreferences.usingV2Soap){
				file = ((SOAPClientV2) com).getNoteAttachment(moduleId);
			}else{
				file = ((SOAPClient) com).getNoteAttachment(moduleId);
			}
			break;
		case UserPreferences.OFFLINEDATA :case UserPreferences.CACHEDATA:
			byte[] ba = ImageCacheHelper.readFileFromCache(UserPreferences.APP_NAME, getFieldValue("id")+"-"+getFieldValue("filename"));

			if(ba != null){
				file[0] = Base64.encode(ba);
				file[1] = getFieldValue("filename");
			}
			else if(NetworkHelper.isAvailable(context)) {
				if(UserPreferences.usingV2Soap){
					SOAPClientV2 com = new SOAPClientV2(UserPreferences.url);
					file = com.getNoteAttachment(moduleId);
			}
			else{
				SOAPClient com = new SOAPClient(UserPreferences.url);
				file = com.getNoteAttachment(moduleId);
			}
				/*SOAPClient com = new SOAPClient(UserPreferences.url);
				file = com.getNoteAttachment(moduleId);*/
			}
			break;

/*		case UserPreferences.OFFLINEDATA:
			Bitmap bitmap = ImageCacheHelper.readFromCache(UserPreferences.APP_NAME, getFieldValue("filename"));

			if(bitmap != null){
				ByteArrayOutputStream bao = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 95, bao);
				byte [] ba = bao.toByteArray();	
				file[0] = Base64.encode(ba);
				file[1] = moduleId+".jpg";
			} else if(NetworkHelper.isAvailable(context)) {
				if(UserPreferences.usingV2Soap){
					SOAPClientV2 com = new SOAPClientV2(UserPreferences.url);
					file = com.getNoteAttachment(moduleId);
				}else{
					SOAPClient com = new SOAPClient(UserPreferences.url);
					file = com.getNoteAttachment(moduleId);
				}
	//			SOAPClient com = new SOAPClient(UserPreferences.url);
	//			file = com.getNoteAttachment(moduleId);
			}
			break;*/
		}
		String temp = getFieldValue("id")+"-"+getFieldValue("filename");
		file[1] = temp;
		return file;
	}

	/*
	 * Save All provided beans at-once
	 */
	public String[] saveAll(SugarBean[] beans, boolean performingSync) throws Exception{
		Vector<String[]> nameLists = new Vector<String[]>();
		Vector<String[]> valueLists = new Vector<String[]>();
		boolean[] isNewWithID = new boolean[beans.length];

		for(int i=0; i<beans.length; i++){
			beans[i].updateAdditionalFields(performingSync);

			nameLists.add(beans[i].getNameArray(performingSync));
			valueLists.add(beans[i].getValueArray(performingSync));

			isNewWithID[i] = beans[i].isNewWithID(performingSync);

			if(com instanceof SOAPClient && moduleName.equalsIgnoreCase("Notes")){
				Bitmap bitmap = ImageCacheHelper.readFromCache("rSugarCRM",beans[i].getFieldValue("filename"));		
				if(bitmap!= null){
					ByteArrayOutputStream bao = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.JPEG, 95, bao);
					byte [] ba = bao.toByteArray();
					((SOAPClient)com).setNoteAttachment(beans[i].getFieldValue("id"), beans[i].getFieldValue("filename"), Base64.encode(ba));
					System.gc();
					bao.close();
				}
			}
		}

		String[] ids = com.setEntryList(
				moduleName, 
				nameLists, 
				valueLists, 
				isNewWithID, 
				performingSync
				);

		return ids;
	}

	/*
	 * checks if the record is created after the lastsync or not
	 */
	public boolean isNewWithID(boolean performingSync) {
		//return false if not performing sync or id is empty
		if(!performingSync || fields.get("id").value.trim().equalsIgnoreCase("")) return false;

		//If save will be performed on local DB and coming from server
		if(com instanceof DBClient){
			try {
				SugarBean tmp = new SugarBean(context, moduleName);
				tmp.retrieve(fields.get("id").value.trim());
				//return true if record does not exists
				return tmp.fields.get("id").value.trim().equalsIgnoreCase("");
			} catch (Exception e) {
				AlertHelper.logError(e);
				return false;
			}
		}

		//return true if record is marked with newly created if save will be performed on Server.
		//not checking on date as it might come from server while sync and while sync it will save with 0 flag
		return fields.get(DBConnection.SYNC_COLUMN).value.trim().equalsIgnoreCase("Yes") ||
				fields.get(DBConnection.SYNC_COLUMN).value.trim().equalsIgnoreCase("1") ;
	}

	/*
	 * update string field value
	 */
	public boolean updateFieldValue(String name, String newValue){
		Field field = fields.get(name);
		if(field != null && (!newValue.trim().equalsIgnoreCase("") || ((name.equalsIgnoreCase("id")
				|| name.equalsIgnoreCase("start_time") || name.equalsIgnoreCase("stop_time"))
				&& moduleName.equalsIgnoreCase("aos_products_quotes")))){
			field.value = newValue;
			field.updated = true;
			fields.put(name, field);
			return true;
		}else
			return false;
	}

	/*
	 * update string field value
	 */
	public boolean updateFieldValue(String name, int optionIndex){
		Field field = fields.get(name);
		if(field != null && field.options != null){
			field.value = field.options[1][optionIndex];
			field.updated = true;
			fields.put(name, field);
			return true;
		}

		return false;
	}

	/*
	 * update string field value
	 */
	public void setFieldValue(String name, String value){
		Field field = fields.get(name);
		if(field != null){
			if(field.type.contains("enum") && field.options != null){
				field.loadValue(value);
			}else{
				field.value = Html.fromHtml(value).toString();
			}

			fields.put(name, field);
		}
	}
	/*
	 * Returns user's full name if user list has been loaded
	 */

	protected String getUserFromID(String userID) {
		SugarBean user = new SugarBean(context, "Users");

		try {
			user.retrieve(userID);
			return user.getListTitle();
		} catch (Exception e) {
			AlertHelper.logError(e);
		}

		return "";
	}

	public boolean usingDB() throws Exception {
		if(com == null) throw new Exception("com is null");

		return com instanceof DBClient;
	}

	/*
	 * returns the where clause for the list view
	 */
	public String getListWhere(String searchText) {
		String where = "(" ;
		if(fields.get("document_name") != null){
			where += moduleName.toLowerCase() + ".document_name";
		}
		else
		{
			if(fields.get("first_name") != null || fields.get("last_name") != null){
				if(UserPreferences.mode == UserPreferences.LIVEDATA){
					where += "CONCAT(IFNULL(" + moduleName.toLowerCase() + ".first_name,''), ' ', IFNULL(" + moduleName.toLowerCase() + ".last_name,''))";
				}else{
					where += moduleName.toLowerCase() + ".first_name||' '||" + moduleName.toLowerCase() + ".last_name";
				}
			}else{
				where += moduleName.toLowerCase() + ".name";
			}
		}
		return where += " like '%" + searchText + "%')";
	}

	/*
	 * returns the title text to be displayed in list-view for corresponding module
	 */
	public String getListTitle() {
		String salutation = getFieldValue("salutation");
		if(fields.get("document_name") != null){
			return fields.get("document_name").value;
		}

		if(fields.get("first_name") != null || fields.get("last_name") != null){
			return salutation+" "+(fields.get("first_name").value + " " + fields.get("last_name").value).trim();
		}

		if(fields.get("name") != null){
			return fields.get("name").value;
		}

		return "";
	}

	/*
	 * returns array of the options for given field
	 * returns blank array if options for given field does not exist or not received properly
	 */
	public String[] getFieldOptions(String fieldName) {
		try {
			return fields.get(fieldName).options[1];
		} catch (Exception e) {
			AlertHelper.logError(e);
			try {
				return new String []{"", fields.get(fieldName).value};
			} catch (Exception e1) {
				AlertHelper.logError(e1);
				return new String []{""};
			}
		}
	}

	/*
	 * returns value for given field
	 * returns blank string if given field does not exist or not received properly
	 */
	public String getFieldValue(String fieldName) {
		try {
			return fields.get(fieldName).value;
		} catch (Exception e1) {
			AlertHelper.logError(e1);
			return "";
		}
	}

	/*
	 * returns the detail text for list view
	 */
	public String getListDetail() {
		return "";
	}

	/*
	 * returns the icon to be displayed in list view	
	 */
	public Drawable getListIcon() {
		return null;
	}

	/*
	 * Resets the Sync on save in DB to 0
	 * execute only if DBClient is loaded	
	 */
	public void resetSyncFlag(int deleted) {
		loadCom(context, false, true);
		com.resetSyncFlag(moduleName, deleted);
	}

	/*
	 * Resets the Id of old Document Revision to new Id
	 * execute only if DBClient is loaded
	 */
	public void resetID(String recordId, String newId) {
		loadCom(context, false, true);
		com.resetId("document_revisions", recordId, newId);
	}
	
/*
 * Retrieves related beans to make a call from its parent.
 */
	public SugarBean[] getCallNumbers(String parentModule){

		SugarBean[] defs = RelationshipHelper.getRelatedDefs(parentModule, context);
		SugarBean[] relContacts;
		
		String whr = null;
		for (int i = 0; i < defs.length; i++) {
			if (defs[i].getFieldValue("rhs_module").equalsIgnoreCase(moduleName) || defs[i].getFieldValue("lhs_module").equalsIgnoreCase(moduleName)) {


				whr = RelationshipHelper.getRelationshipWhere(defs[i], moduleName, getFieldValue("id"), null, null, 0);
			}
		}

		final SugarBean contactperson = new SugarBean(context, parentModule);

		try {
			relContacts = contactperson.retrieveAll(whr, null, -1, -1, 0,null);
		} catch (Exception e) {
			relContacts = new SugarBean[0];
			e.printStackTrace();
			AlertHelper.logError(e);
		}

		return relContacts;
	}
	
	
	public String getDateTime() {
		if(fields.get("date_entered") != null){
			return fields.get("date_entered").value;
		}
		return "";
		
	}
	
	/*
	 * returns the phone text for list view
	 */
	public String getContactNo(String moduleName) {
		if(moduleName.equalsIgnoreCase("Accounts") && fields.get("phone_office") != null){
			return fields.get("phone_office").value;
		}
		else if((moduleName.equalsIgnoreCase("Leads") || moduleName.equalsIgnoreCase("Prospects")) && fields.get("phone_work") != null){
			return fields.get("phone_work").value;
		}
		
		return "";
		
	}

	public String uploadDocument(String data, String fileName, Boolean performingSync) throws Exception
	{
		if(com == null) throw new Exception("com is null");

		updateAdditionalFields(performingSync);

		//Updates the id value in beans for viewing
		String id = com.setEntry(
				"Documents",
				getNameArray(performingSync),
				getValueArray(performingSync),
				isNewWithID(performingSync),
				performingSync,
				false
		);

		if(!id.equalsIgnoreCase("-1")){
			updateFieldValue("id", id);
		}

		String documentId = null;
		if(!id.equals("-1")) {
			documentId = com.setDocumentRevision(id, data, fileName, "1");

			if(NetworkHelper.isAvailable(context) && com instanceof SOAPClient) { ///// SAVING IN OFFLINE DATABASE TOO
				loadCom(context, false, true);

				//Updates the id value in beans for viewing
				String id1 = com.setEntry(
						"Documents",
						getNameArray(performingSync),
						getValueArray(performingSync),
						isNewWithID(performingSync),
						true,
						false
				);

				DBClient client = new DBClient(context);
				documentId = client.updateDocumentRevision(id1, documentId, fileName, "1");

				loadCom(context, true, false);
			}

			return documentId;
		}

		return "-1";
	}

	public String setDocumentRevision(String docId, String data, String fileName, String revision) throws Exception{
		String documentId = null;
		if(!docId.equals("-1")) {
			documentId = com.setDocumentRevision(docId, data, fileName, revision);
			return documentId;
		}

		return "-1";
	}
}
