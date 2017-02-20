/*
 * Copyright (C) 2013 Customer Focused Marketing, Inc
 */

package rolustech.beans;

import android.content.Context;

import java.util.Hashtable;

import rolustech.communication.soap.SOAPClient;


/**
 * @author Rolustech
 */

public class Lead extends SugarBean {
	public Lead(Context context) {
	//	super(context, "Leads");
		super();
		 moduleName = "Leads";
			fields = new Hashtable<String, Field>();
		
		String options[][] = null;
		
		// Add Fields
//		fields.put("id", new Field("id", "id", "id", options, "LBL_ID", "", "", "", "", false));
		fields.put("last_name", new Field("last_name","varchar", "varchar", options, "LBL_FIRST_NAME",  "", "", "", "", false));
		fields.put("status_description", new Field("status_description","text", "text", options, "LBL_DESCRIPTION", "", "", "", "", false));
		fields.put("lead_source_description", new Field("lead_source_description","text", "text", options, "LBL_DESCRIPTION", "", "", "", "", false));
		fields.put("account_name", new Field("account_name", "text","text", options, "LBL_DESCRIPTION", "", "", "", "", false));
		fields.put("email1", new Field("email1", "text","text", options, "LBL_DESCRIPTION","", "", "", "", false));
		
/*		fields.put("date_entered", new Field("date_entered", "datetime","datetime", options, "LBL_DATE_ENTERED","", "", "", "", false));
		fields.put("date_modified", new Field("date_modified", "datetime","datetime", options, "LBL_DATE_MODIFIED","", "", "", "", false));
		fields.put("modified_user_id", new Field("modified_user_id", "id","id", options, "LBL_MODIFIED_ID", "", "", "", "", false));
		fields.put("modified_by_name", new Field("modified_by_name","relate", "relate", options, "LBL_MODIFIED_NAME", "", "", "", "", false));
		fields.put("created_by", new Field("created_by","id", "id", options, "LBL_CREATED_ID", "", "", "", "", false));
		fields.put("created_by_name", new Field("created_by_name","relate", "relate", options, "LBL_CREATED", "", "", "", "", false));
		
		fields.put("assigned_user_id", new Field("assigned_user_id","relate",  "relate", options, "LBL_ASSIGNED_TO_ID", "", "", "", "", false));
		fields.put("assigned_user_name", new Field("assigned_user_name","relate", "relate", options, "LBL_ASSIGNED_TO_NAME","", "", "", "", false));
		
		fields.put("deleted", new Field("deleted","bool", "bool", options, "LBL_FIRST_NAME",  "0", "", "", "", false));
*/		
		options = new String[2][5];
		//keys
		options[0][0] = "Android";
		//Setting Display Values
		options[1][0] = "Android";
		
		fields.put("lead_source", new Field("lead_source","enum", "enum", null, "LBL_STATUS", "Android", "", "", "", false));
	}


	/*
	 * Save data of the bean in DB/Server
	 */
	public String save(boolean performingSync,SOAPClient soap) throws Exception  {
		if(soap == null) throw new Exception("com is null");

		//Updates the id value in beans for viewing
		String id = soap.setEntry(
				moduleName, 
				getNameArray(performingSync), 
				getValueArray(performingSync), 
				isNewWithID(performingSync), 
				performingSync,
				false
				);

		if(!id.equalsIgnoreCase("-1")){
		//	updateFieldValue("id", id);
		} 

		return id;
	}

}
