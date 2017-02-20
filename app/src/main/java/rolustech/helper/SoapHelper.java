package rolustech.helper;

import android.content.Context;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.Vector;

import rolustech.beans.SugarBean;
import rolustech.communication.soap.SOAPClient;
import rolustech.communication.soap.SOAPClientV2;

public class SoapHelper {

/*
 * Returns ArrayList for Array of fields for related records after parsing from soap object.
 */
	public static ArrayList<ArrayList<String[]>> parseRelationshipsV2(Vector<Vector<SoapObject>> relationshipList) {
		ArrayList<ArrayList<String[]>> relationships = new  ArrayList<ArrayList<String[]>>();
		
		for (int i = 0; i < relationshipList.size(); i++) {
			Vector<SoapObject> link_list2 = relationshipList.get(i);

			for (int j = 0; j < link_list2.size(); j++) {
				SoapObject link_name_value = link_list2.get(j);

				//String relatedModuleName = (String) link_name_value.getProperty("name");
				@SuppressWarnings("unchecked")
				Vector<Vector<SoapObject>> relatedRecords = (Vector<Vector<SoapObject>>) link_name_value.getProperty("records");

				for (int k = 0; k < relatedRecords.size(); k++) {
					Vector<SoapObject> nameValueList = relatedRecords.get(k);

					ArrayList<String[]> tmp = new ArrayList<String[]>();
					
					for (int l = 0; l < nameValueList.size(); l++) {
						SoapObject nameValue = nameValueList.get(l);
						
						String name_value[] = new String[2];

						name_value[0] = (String) nameValue.getProperty("name");
						if (nameValue.getProperty("value") != null){
							name_value[1] = (String) nameValue.getProperty("value");
						} else {
							name_value[1] = "";
						}
						tmp.add(name_value);
					}
					relationships.add(tmp);
				}
			}
		}
		
		return relationships;
	}
		
/*
 * 
 */
	public static SugarBean[] getRelatedBeans(String moduleName, Context context, ArrayList<ArrayList<String[]>> records, int index) {
		SugarBean beans[] = new SugarBean[0];
		
		if(records != null){
			beans = new SugarBean[records.size()];
			for(int i=0; i<records.size(); i++){
				ArrayList<String[]> record = records.get(i);
				beans[i] = new SugarBean(context, moduleName);
				for(int j=0; j<record.size(); j++){
					String name = record.get(j)[0];
					
					if(record.get(j)[1] != null){
						beans[i].setFieldValue(name, record.get(j)[1]);
					}
				}
				/*
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
				*/
			}
					
		}
		return beans;
	}
	
	/*
	 * Appends "http://" and "/soap.php" strings if missing
	 */
		public static String generateUrl(String displayUrl) {
			if (displayUrl == null) {
				return null;
			}
			
			if (!displayUrl.startsWith(SOAPClient.HTTP)
				&& !displayUrl.startsWith(SOAPClient.HTTPS)) {
				displayUrl = SOAPClient.HTTP + displayUrl;
			}
			
			if (!displayUrl.endsWith(SOAPClient.SOAP_URL)) {
				displayUrl = displayUrl + SOAPClient.SOAP_URL;
			}
			
			return displayUrl;
		}
		public static String generateUrlV2(String displayUrl) {
			if (displayUrl == null) {
				return null;
			}
			
			if (!displayUrl.startsWith(SOAPClient.HTTP)
				&& !displayUrl.startsWith(SOAPClient.HTTPS)) {
				displayUrl = SOAPClient.HTTP + displayUrl;
			}
			
			if (!displayUrl.endsWith(SOAPClient.SOAP_URL)) {
				displayUrl = displayUrl + SOAPClientV2.SOAP_URL;
			}
			
			return displayUrl;
		}
}
