package rolustech.communication.soap;

import android.util.Log;

import com.iconsolutions.helper.UserPreferences;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import rolustech.beans.Field;
import rolustech.beans.SyncData;
import rolustech.communication.Communicator;
import rolustech.helper.AlertHelper;
import rolustech.tempStorage.SugarBeanContainer;


/*
 * SOAP Client
 * generic class to invoke soap methods on server using ksoap api
 * static members
 * URL holds server url
 * sess_id holds session id returned by server after successful login
 */

@SuppressWarnings("unchecked")
public class SOAPClient implements Communicator {
    private static final String NAMESPACE = "zzz";
    public static final String SOAP_URL = "/soap.php";
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    private static String URL;
    private static String sess_id;

    Integer c;

    public SOAPClient() {

    }

    public SOAPClient(String url) {
        URL = url;
    }

    public static String getNamespace() {
        return NAMESPACE;
    }

    public static String getSess_id() {
        return sess_id;
    }

    public static void reset() {
        sess_id = null;
    }


    /*
     * Converts bytes array into hex string
     */
    private String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /*
     * returns md5 of the given string
     */
    private String getMD5(String value) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            AlertHelper.logError(e);
            return value;
        }
        md5.reset();
        try {
            md5.update(value.getBytes("iso-8859-1"), 0, value.length());
        } catch (Exception e) {
            AlertHelper.logError(e);
            md5.update(value.getBytes());
        }
        byte[] messageDigest = md5.digest();

        return convertToHex(messageDigest);
    }

    /*
     * Login to sugar using the given username and password
     * first use plain password if unsuccessful then use md5 of the password
     */
    @Override
    public int login(String username, String password) {
//		int res = soapLogin(username ,password);
//		if(res != 1){
        int res = soapLogin(username, getMD5(password.trim()).trim());
//		}
        return res;
    }

    /*
     * prepare and call soapobject for login request
     */
    private int soapLogin(String username, String password) {
        try {
            SoapObject request = new SoapObject(NAMESPACE, "login");
            Log.v("SOAPClient","Crew_App NameSpace = >  "+NAMESPACE);
			/* Creating input parameters*/
            SoapObject userAuth = new SoapObject(NAMESPACE, "user_auth");
            userAuth.addProperty("user_name", username);
            userAuth.addProperty("password", password);
            userAuth.addProperty("version", "1.0");

            //Add Authentication of user to Request Soap Object

            request.addProperty("user_auth", userAuth);

            SoapObject response = (SoapObject) call("login", request, false);

            if (response != null) {
//				SoapObject error = (SoapObject) response.getProperty("error");
//				if(error != null && error.getPropertyCount() >=2){
//					String errorNumber = (String) error.getProperty("number");
//					if (errorNumber != null && !errorNumber.equals("0")) {
//						return 0;
//					}
//				}
            }
            if (response != null && response.getProperty("id") != null) {
                sess_id = (String) response.getProperty("id");
                return sess_id.equalsIgnoreCase("-1") ? 0 : 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            AlertHelper.logError(e);
            if (e.getMessage().contains("Could not validate certificate signature")) {
                return 3;
            }
            return 2;
        }
    }

    public String setDocumentRevision(String documentID, String fileData, String fileName, String revision) throws Exception {

        SoapObject request = new SoapObject(NAMESPACE, "set_document_revision");

        SoapObject note = new SoapObject(NAMESPACE, "document_revision");
        note.addProperty("id", documentID);
        note.addProperty("file", fileData);
        note.addProperty("filename", fileName);
        note.addProperty("revision", revision);

        request.addProperty("session", sess_id);

        request.addProperty("note", note);

        SoapObject response = (SoapObject) call("set_document_revision", request, false);

        if (response != null) {
            SoapObject error = (SoapObject) response.getProperty("error");
            if (error != null && error.getPropertyCount() >= 2) {
                String errorNumber = (String) error.getProperty("number");
                if (errorNumber != null && !errorNumber.equals("0")) {
                    throw new Exception((String) error.getProperty("name") + ": " + (String) error.getProperty("description"));
                }
            }

            if (response.getProperty("id") != null) {
                String id = (String) response.getProperty("id");
                if (id == null || id.equalsIgnoreCase("")) id = "-1";
                return id;
            }
        }

        return "-1";
    }

    public HashMap<String, String> getDocumentRevision(String revisionID) throws Exception {

        SoapObject request = new SoapObject(NAMESPACE, "get_document_revision");

        request.addProperty("session", sess_id);
        request.addProperty("i", revisionID);

        SoapObject response = (SoapObject) call("get_document_revision", request, false);
        HashMap<String, String> values = new HashMap<String, String>();

        if (response != null) {
            SoapObject obj = (SoapObject) response.getProperty("document_revision");

            values.put("id", (String) obj.getProperty("id"));
            values.put("filename", (String) obj.getProperty("filename"));
            values.put("file", (String) obj.getProperty("file"));
            values.put("revision", (String) obj.getProperty("revision"));

        }

        return values;
    }

    public String setValueEntry(String moduleName, String field_value, String record_id) throws Exception {

        SoapObject request = new SoapObject(NAMESPACE, "set_value_entry");

        request.addProperty("session", sess_id);
        request.addProperty("module_name", moduleName);
        request.addProperty("field_value", field_value);
        request.addProperty("record_id", record_id);
        request.addProperty("current_user_id", UserPreferences.userID);

        SoapObject response = (SoapObject) call("set_value_entry", request, false);

        if (response != null) {
            SoapObject error = (SoapObject) response.getProperty("error");
            if (error != null && error.getPropertyCount() >= 2) {
                String errorNumber = (String) error.getProperty("number");
                if (errorNumber != null && !errorNumber.equals("0")) {
//                    throw new Exception((String) error.getProperty("name") + ": " + (String) error.getProperty("description"));
                }
            }

            if (response.getProperty("id") != null) {
                String id = (String) response.getProperty("id");
                if (id == null || id.equalsIgnoreCase("")) id = "-1";
                return id;
            }
        }

        return "-1";

    }

    /*
     * return array list of all the records' name values pairs found for the provided criteria
     * if found noting returns empty arraylist
     */
    @Override
    public ArrayList<ArrayList<String[]>> getEntryList(String moduleName, String[] select_fields, String where, int total, int offset, String orderBy, int deleted,
                                                       String[] relatedMods) throws Exception {

        SoapObject request = new SoapObject(NAMESPACE, "get_entry_list");
        request.addProperty("session", sess_id);
        request.addProperty("module_name", moduleName);

        if (where == null) where = "";
        /******************Code for Project Task included*************************/
        if (where.contains("projecttask")) {
            where = where.replace("projecttask", "project_task");
        }
        if (orderBy != null && orderBy.contains("projecttask")) {
            orderBy = orderBy.replace("projecttask", "project_task");
        }
        /******************Code for Project Task included*************************/
        request.addProperty("query", where);

        if (orderBy == null || orderBy.trim().equalsIgnoreCase("")) orderBy = "date_modified desc";
        request.addProperty("order_by", orderBy);
        request.addProperty("offset", offset);

        Vector<String> sel = new Vector<String>();
        for (int i = 0; i < select_fields.length; i++) {
            sel.add(select_fields[i]);
        }
        request.addProperty("select_fields", sel);
        request.addProperty("max_results", total);

        request.addProperty("deleted", "" + deleted);

        SoapObject response = (SoapObject) call("get_entry_list", request, false);

        //If found error throw exception
        SoapObject error = (SoapObject) response.getProperty("error");
        if (error != null && error.getPropertyCount() >= 2) {
            String errorNumber = (String) error.getProperty("number");
            if (errorNumber != null && !errorNumber.equals("0")) {
                throw new Exception((String) error.getProperty("name") + ": " + (String) error.getProperty("description"));
            }
        }

        Vector<SoapObject> records = (Vector<SoapObject>) response.getProperty("entry_list");

        return parseGetEntryListData(records);
    }

    public ArrayList<ArrayList<ArrayList<String[]>>> getEntryWorkOrder(String record_id) throws Exception {
        SoapObject request = new SoapObject(NAMESPACE, "get_crew_workorder_entry_list");
        request.addProperty("session", sess_id);
        request.addProperty("workorder_id", record_id);

        SoapObject response = (SoapObject) call("get_crew_workorder_entry_list", request, false);

        //If found error throw exception
        SoapObject error = (SoapObject) response.getProperty("error");
        if (error != null && error.getPropertyCount() >= 2) {
            String errorNumber = (String) error.getProperty("number");
            if (errorNumber != null && !errorNumber.equals("0")) {
                throw new Exception((String) error.getProperty("name") + ": " + (String) error.getProperty("description"));
            }
        }

        Vector<SoapObject> line_items = (Vector<SoapObject>) response.getProperty("entry_list");
        Vector<SoapObject> work_order = (Vector<SoapObject>) response.getProperty("field_list");


        ArrayList<ArrayList<String[]>> list1 = parseGetEntryListData(line_items);
        ArrayList<ArrayList<String[]>> list2 = parseGetEntryListData(work_order);

        ArrayList records = new ArrayList();
        records.add(list1);
        records.add(list2);

        return records;
    }

    /*
     * Returns ArrayList for Array of fields for related records after parsing from soap object.
     */
    protected ArrayList<ArrayList<String[]>> parseGetEntryListData(Vector<SoapObject> records) {
        SoapObject response;
        ArrayList<ArrayList<String[]>> list = new ArrayList<ArrayList<String[]>>();

        for (int j = 0; j < records.size(); j++) {
            response = records.get(j);
            Vector<SoapObject> record = (Vector<SoapObject>) response.getProperty("name_value_list");

            ArrayList<String[]> tmp = new ArrayList<String[]>();
            for (int i = 0; i < record.size(); i++) {
                response = record.get(i);
                String tmp1[] = new String[2];

                tmp1[0] = (String) response.getProperty("name");
                if (response.getProperty("value") != null) {
                    tmp1[1] = (String) response.getProperty("value");
                } else {
                    tmp1[1] = "";
                }
                tmp.add(tmp1);
            }
            list.add(tmp);
        }

        return list;
    }

    @Override
    public SugarBeanContainer getEntryListV2Sync(String moduleName, String[] select_fields, String where, int total, int offset,
                                                 String orderBy, int deleted, String[] relatedModules)
            throws Exception {

        SugarBeanContainer container = new SugarBeanContainer();

        SoapObject request = new SoapObject(NAMESPACE, "get_entry_list");
        request.addProperty("session", sess_id);
        request.addProperty("module_name", moduleName);

        if (where == null) where = "";
        /******************Code for Project Task included*************************/
        if (where.contains("projecttask")) {
            where = where.replace("projecttask", "project_task");
        }
        if (orderBy.contains("projecttask")) {
            orderBy = orderBy.replace("projecttask", "project_task");
        }
        /******************Code for Project Task included*************************/
        request.addProperty("query", where);

        if (orderBy == null || orderBy.trim().equalsIgnoreCase("")) orderBy = "date_modified desc";
        request.addProperty("order_by", orderBy);
        request.addProperty("offset", offset);

        Vector<String> sel = new Vector<String>();
        for (int i = 0; i < select_fields.length; i++) {
            sel.add(select_fields[i]);
        }
        request.addProperty("select_fields", sel);

        /*******/
        Vector<SoapObject> relatedNameValue = new Vector<SoapObject>();

        SoapObject nameValue = new SoapObject(SOAPClient.getNamespace(), "link_name_to_fields_array");

        if (relatedModules != null) {
            for (int l = 0; l < relatedModules.length; l++) {
                Vector<String> vals = new Vector<String>();
                for (int j = 0; j < UserPreferences.moduleConfiguration.get(relatedModules[l]).fields.keySet().toArray().length; j++) {
                    vals.add((String) UserPreferences.moduleConfiguration.get(relatedModules[l]).fields.keySet().toArray()[j]);
                }
                nameValue.addProperty("name", relatedModules[l].toLowerCase());
                nameValue.addProperty("value", vals);
                relatedNameValue.add(nameValue);
            }
        } else {
            nameValue.addProperty("name", "");
            nameValue.addProperty("value", "");
            relatedNameValue.add(nameValue);
        }
        /*******/

        request.addProperty("link_name_to_fields_array", relatedNameValue);
        request.addProperty("max_results", total);
        request.addProperty("deleted", "" + deleted);

        SoapObject response = (SoapObject) call("get_entry_list", request, false);


        // Relationships
        Vector<SoapObject> records = (Vector<SoapObject>) response.getProperty("entry_list");
        Vector<Vector<SoapObject>> relationshipList = (Vector<Vector<SoapObject>>) response.getProperty("relationship_list");

        ArrayList<ArrayList<String[]>> list = parseGetEntryListData(records);
        HashMap<String, HashMap<String, ArrayList<ArrayList<String[]>>>> relationships = parseRelationshipsV2Sync(relationshipList, relatedNameValue);

        container.list = list;
        container.relationships = relationships;

        return container;
    }

    /*
     * Returns ArrayList for Array of fields for related records after parsing from soap object.
     */
    protected static HashMap<String, HashMap<String, ArrayList<ArrayList<String[]>>>> parseRelationshipsV2Sync(Vector<Vector<SoapObject>> relationshipList, Vector<SoapObject> relatedNameValue) {

        HashMap<String, HashMap<String, ArrayList<ArrayList<String[]>>>> hm2 =
                new HashMap<String, HashMap<String, ArrayList<ArrayList<String[]>>>>();

        for (int i = 0; i < relationshipList.size(); i++) {
            HashMap<String, ArrayList<ArrayList<String[]>>> relHash =
                    new HashMap<String, ArrayList<ArrayList<String[]>>>();

            Vector<SoapObject> relatedRecArr = relationshipList.get(i);

            for (int j = 0; j < relatedRecArr.size(); j++) {
                SoapObject relObj = relatedRecArr.get(j);

                Vector<Vector<SoapObject>> relatedRecords = (Vector<Vector<SoapObject>>) relObj.getProperty("records");

                ArrayList<ArrayList<String[]>> relationships = new ArrayList<ArrayList<String[]>>();
                for (int k = 0; k < relatedRecords.size(); k++) {
                    ArrayList<String[]> tmp = new ArrayList<String[]>();
                    Vector<SoapObject> nameValueList = relatedRecords.get(k);

                    for (int l = 0; l < nameValueList.size(); l++) {
                        SoapObject nameValue = nameValueList.get(l);

                        String name_value[] = new String[2];

                        name_value[0] = (String) nameValue.getProperty("name");
                        if (nameValue.getProperty("value") != null) {
                            name_value[1] = (String) nameValue.getProperty("value");
                        } else {
                            name_value[1] = "";
                        }
                        tmp.add(name_value);
                    }
                    relationships.add(tmp);
                }

                relHash.put((String) relObj.getProperty("name"), relationships);

            }

            hm2.put(i + "", relHash);

            for (int k = 0; k < relatedNameValue.size(); k++) {

            }
        }
        return hm2;
    }

    /*
     *  return array list of all the records' name values pairs for the provided select of the record associated with the provided id in the provided module if exists
     * otherwise null
     */
    @Override
    public ArrayList<String[]> getEntry(String moduleName, String[] select_fields, String id, boolean isRelRequest) throws Exception {
        ArrayList<String[]> data = new ArrayList<String[]>();

        SoapObject request = new SoapObject(NAMESPACE, "get_entry");
        /* Creating input parameters*/
        request.addProperty("session", sess_id);
        request.addProperty("module_name", moduleName);
        request.addProperty("id", id);

        Vector<String> sel = new Vector<String>();
        for (int i = 0; i < select_fields.length; i++) {
            sel.add(select_fields[i]);
        }
        request.addProperty("select_fields", sel);

        SoapObject response = (SoapObject) call("get_entry", request, false);

        //If found error throw exception//
        SoapObject error = (SoapObject) response.getProperty("error");
        if (error != null && error.getPropertyCount() >= 2) {
            String errorNumber = (String) error.getProperty("number");
            if (errorNumber != null && !errorNumber.equals("0")) {
                throw new Exception((String) error.getProperty("name") + ": " + (String) error.getProperty("description"));
            }
        }

        Vector<SoapObject> records = (Vector<SoapObject>) response.getProperty("entry_list");
        response = records.get(0);

        Vector<SoapObject> record = (Vector<SoapObject>) response.getProperty("name_value_list");
        for (int i = 0; i < record.size(); i++) {
            response = record.get(i);
            String tmp1[] = new String[2];
            tmp1[0] = (String) response.getProperty("name");

            if (response.getProperty("value") != null) {
                tmp1[1] = (String) response.getProperty("value");
            } else {
                tmp1[1] = "";
            }
            data.add(tmp1);
        }

        return data;
    }

    /*
     * insert or updates the record in the provided module
     * returns id if successful otherwise -1
     */
    @Override
    public String setEntry(String moduleName, String[] name, String[] value, boolean newWithID, boolean performingsync, boolean isRelRequest) throws Exception {
        if (moduleName == null || name == null || value == null) {
            return "-1";
        }

        String soapNameSpace = "set_entry";
        if (moduleName.equalsIgnoreCase("AOS_Products_Quotes"))
            soapNameSpace = "custom_line_item_set_entry";

        SoapObject request = new SoapObject(NAMESPACE, soapNameSpace);

        request.addProperty("session", sess_id);
        request.addProperty("module_name", moduleName);

        Vector<SoapObject> nameValueList = getNameValueList(name, value, newWithID);

        request.addProperty("name_value_list", nameValueList);
        SoapObject response = (SoapObject) call(soapNameSpace, request, false);

        if (response != null) {
            SoapObject error = (SoapObject) response.getProperty("error");
            if (error != null && error.getPropertyCount() >= 2) {
                String errorNumber = (String) error.getProperty("number");
                if (errorNumber != null && !errorNumber.equals("0")) {
                    throw new Exception((String) error.getProperty("name") + ": " + (String) error.getProperty("description"));
                }
            }

            if (response.getProperty("id") != null) {
                String id = (String) response.getProperty("id");
                if (id == null || id.equalsIgnoreCase("")) id = "-1";
                return id;
            }
        }

        return "-1";
    }


    private Vector<SoapObject> getNameValueList(String[] name, String[] value, boolean newWithID) {
        Vector<SoapObject> nameValueList = new Vector<SoapObject>();

        for (int i = 0; i < name.length; i++) {
            SoapObject nameValue = new SoapObject(NAMESPACE, "name_value");

            nameValue.addProperty("name", name[i]);
            nameValue.addProperty("value", value[i]);

            nameValueList.add(nameValue);
        }

        //Setting new_with_id parameter to true for sync
        if (newWithID) {
            SoapObject nameValue = new SoapObject(NAMESPACE, "name_value");

            nameValue.addProperty("name", "new_with_id");
            nameValue.addProperty("value", "1");

            nameValueList.add(nameValue);
        }

        return nameValueList;
    }

    /*
     * Calls setEntry for all records
     */
    @Override
    public String[] setEntryList(
            String moduleName, Vector<String[]> nameLists,
            Vector<String[]> valueLists, boolean[] isNewWithID,
            boolean performingSync
    ) throws Exception {

        String soapNameSpace = "set_entries";
        if (moduleName.equalsIgnoreCase("AOS_Products_Quotes"))
            soapNameSpace = "custom_set_entries";

        SoapObject request = new SoapObject(NAMESPACE, soapNameSpace);

        request.addProperty("session", sess_id);
        request.addProperty("module_name", moduleName);

        Vector<Vector<SoapObject>> nameValueLists = new Vector<Vector<SoapObject>>();

        for (int i = 0; i < nameLists.size(); i++) {
            Vector<SoapObject> nameValueList = getNameValueList(nameLists.get(i), valueLists.get(i), isNewWithID[i]);

            nameValueLists.add(nameValueList);
        }

        request.addProperty("name_value_lists", nameValueLists);

        SoapObject response = (SoapObject) call(soapNameSpace, request, false);

        //Checking error
        SoapObject error = (SoapObject) response.getProperty("error");
        if (error != null && error.getPropertyCount() >= 2) {
            String errorNumber = (String) error.getProperty("number");
            if (errorNumber != null && !errorNumber.equals("0")) {
                throw new Exception((String) error.getProperty("description"));
            }
        }

        String[] ret = null;

        if (response != null) {
            Vector<String> ids = (Vector<String>) response.getProperty("ids");
            if (ids != null) {
                ret = new String[ids.size()];

                for (int i = 0; i < ids.size(); i++) {
                    ret[i] = ids.get(i);
                }

                return ret;
            }
        }

        ret = new String[nameLists.size()];
        for (int i = 0; i < nameLists.size(); i++) {
            ret[i] = "-1";
        }

        return ret;
    }

    /*
     * returns the total record for the specified criteria
     */
    @Override
    public long getEntriesCount(String moduleName, String where, int deleted) throws Exception {
        SoapObject request = new SoapObject(NAMESPACE, "get_entries_count");
        /* Creating input parameters*/
        request.addProperty("session", sess_id);
        request.addProperty("module_name", moduleName);

        if (where == null) where = "";
        /******************Code for Project Task included*************************/
        if (where.contains("projecttask")) {
            where = where.replace("projecttask", "project_task");
        }

        /******************Code for Project Task included*************************/
        request.addProperty("query", where);

        request.addProperty("deleted", "" + deleted);
        try {
            SoapObject response = (SoapObject) call("get_entries_count", request, false);

            //Checking error
            SoapObject error = (SoapObject) response.getProperty("error");
            if (error != null && error.getPropertyCount() >= 2) {
                String errorNumber = (String) error.getProperty("number");
                if (errorNumber != null && !errorNumber.equals("0")) {
                    throw new Exception((String) error.getProperty("description"));
                }
            }

            Integer count = (Integer) response.getProperty("result_count");

            if (count == null) return 0;
            c = count;
        } catch (Exception e) {
            // TODO: handle exception
        }

        return c.longValue();
    }

    /*
     * sets a relationship between the provided records
     */
    @Override
    public String setRelationship(String module1, String module1_id, String module2, String module2_id, Vector<String[]> nameLists, Vector<String[]> valueLists, boolean performingSync, int deleted) throws Exception {
        SoapObject request = new SoapObject(NAMESPACE, "set_relationship");
        SoapObject set_relationship_value = new SoapObject(NAMESPACE, "set_relationship_value");
        set_relationship_value.addProperty("module1", module1);
        set_relationship_value.addProperty("module1_id", module1_id);
        set_relationship_value.addProperty("module2", module2);
        set_relationship_value.addProperty("module2_id", module2_id);
        request.addProperty("session", sess_id);
        request.addProperty("set_relationship_value", set_relationship_value);

        SoapObject error = (SoapObject) call("set_relationship", request, false);

        //Checking error
        if (error != null && error.getPropertyCount() >= 2) {
            String errorNumber = (String) error.getProperty("number");
            if (errorNumber != null && !errorNumber.equals("0")) {
                throw new Exception((String) error.getProperty("name") + ": " + (String) error.getProperty("description"));
            }
        }

        return "1";
    }

    public String setRelationshipCustom(String moduleName, String moduleID,
                                        String relatedModuleName, String relatedIDs, String index,
                                        boolean performingSync, int deleted) throws Exception {

        SoapObject request = new SoapObject(NAMESPACE, "set_relationship_custom");

        request.addProperty("session", sess_id);

        request.addProperty("module", moduleName);
        request.addProperty("module_id", moduleID);
        request.addProperty("related_module", relatedModuleName);
        request.addProperty("related_module_id", relatedIDs);
        request.addProperty("product_index", index);
        //request.addProperty("set_relationship_value",set_relationship_value);

        String response = (String) call("set_relationship_custom", request, false);

        return response;
    }

    /*
     * returns the user id of the logged in user
     */
    public String getCurrentUserID() throws Exception {
        SoapObject request = new SoapObject(NAMESPACE, "get_user_id");

		/* Creating input parameters*/
        request.addProperty("session", sess_id);
        String id = (String) call("get_user_id", request, false);
        if (id == null || id.equalsIgnoreCase("-1")) {
            return null;
        }
        return id;
    }

    public Hashtable<String, Field> getModulesFields(String module) throws Exception {
        SoapObject request = new SoapObject(NAMESPACE, "get_module_fields");

		/* Creating input parameters*/
        request.addProperty("session", sess_id);
        request.addProperty("module_name", module);

        request.addProperty("fields", "");

        SoapObject response = (SoapObject) call("get_module_fields", request, false);

        Hashtable<String, Field> table = new Hashtable<String, Field>();

        Vector<SoapObject> fields = (Vector<SoapObject>) response.getProperty("module_fields");
        String[][] options = null;

        for (int k = 0; k < fields.size(); k++) {
            Vector<SoapObject> opt = (Vector<SoapObject>) fields.get(k).getProperty("options");
            if (opt.size() > 0) {
                options = new String[2][opt.size()];

                for (int j = 0; j < opt.size(); j++) {
                    options[0][j] = (String) opt.get(j).getProperty("name");
                    options[1][j] = (String) opt.get(j).getProperty("value");
                }
            } else {
                options = null;
            }
            String name = (String) fields.get(k).getProperty("name");
            String type = (String) fields.get(k).getProperty("type");
            String dbType = "";

            try {
                dbType = (String) fields.get(k).getProperty("dbType");
            } catch (Exception e) {
            }

            String relModule = "";
            try {
                relModule = (String) fields.get(k).getProperty("module");
            } catch (Exception e) {
            }

            String label = "";
            try {
                label = (String) fields.get(k).getProperty("label");
            } catch (Exception e) {
            }

            String idName = "";
            try {
                idName = (String) fields.get(k).getProperty("id_name");
            } catch (Exception e) {
            }

            String typeName = "";
            try {
                typeName = (String) fields.get(k).getProperty("type_name");
            } catch (Exception e) {
            }

            boolean isRequired = false;
            try {
                isRequired = (Integer) fields.get(k).getProperty("required") == 1 ? true : false;
            } catch (Exception e) {
            }

            table.put(name, new Field(name, type, dbType, options, label, "", relModule, idName, typeName, isRequired));
        }

        return table;
    }

    public ArrayList<String> getAvailableModules() {
        ArrayList<String> modules = new ArrayList<String>();

        try {
            SoapObject request = new SoapObject(NAMESPACE, "get_available_modules ");

			/* Creating input parameters*/
            request.addProperty("session", sess_id);

            SoapObject response = (SoapObject) call("get_available_modules", request, false);

            if (response != null && response.getProperty("modules") != null) {
                Vector<String> modulesList = (Vector<String>) response.getProperty("modules");

                for (int i = 0; i < modulesList.size(); i++) {
                    modules.add(modulesList.get(i));
                }
            }
        } catch (Exception e) {
            AlertHelper.logError(e);
        }

        return modules;
    }

    /*
     * Get path to download database for quick import
     */
    public String getDownloadLink() throws Exception {

        SoapObject response = null;
        try {
            SoapObject request = new SoapObject(NAMESPACE, "get_db_file");

            SoapObject userAuth = new SoapObject(NAMESPACE, "user_auth");
            userAuth.addProperty("user_name", UserPreferences.userName);
            userAuth.addProperty("password", getMD5(UserPreferences.password).trim());
            request.addProperty("user_auth", userAuth);

            response = (SoapObject) call("get_db_file", request, false);
            UserPreferences.lastSync = (String) response.getProperty("file_creation_date");
            return (String) response.getProperty("path");
        } catch (Exception e) {
            AlertHelper.logError(e);
        }

        return "";
    }


    /*
     * Get path to download files for File-based Sync
     */
    public ArrayList<SyncData> getSyncFilePath(String query, String modulesList) throws Exception {
        SoapObject request = new SoapObject(NAMESPACE, "get_sync");

		/* Creating input parameters*/
        SoapObject userAuth = new SoapObject(NAMESPACE, "user_auth");
        userAuth.addProperty("user_name", UserPreferences.userName);
        userAuth.addProperty("password", getMD5(UserPreferences.password).trim());

        request.addProperty("user_auth", userAuth);
        request.addProperty("date", UserPreferences.lastSync);
//        request.addProperty("date", "2016-05-19 10:07:00");

        SoapObject response = (SoapObject) call("get_sync", request, false);

        ArrayList<SyncData> syncData = new ArrayList<SyncData>();

        SyncData data = new SyncData();
        String path = (String) response.getProperty("path");
        data.path = path;
        String name = path.substring(path.lastIndexOf('/') + 1);
        data.name = name.replace(".zip", "");
        syncData.add(data);

        return syncData;
    }

    /*
     * Returns string for server version no
     */
    public static String getServerVersion() throws Exception {
        SoapObject request = new SoapObject(NAMESPACE, "get_server_version");

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        HttpTransportR androidHttpTransport = new HttpTransportR(URL, 20000);
        androidHttpTransport.debug = true;

        try {
            androidHttpTransport.call(NAMESPACE + "/" + "get_server_version", envelope);
        } catch (Exception e) {
            throw new Exception("Response Dump: " + androidHttpTransport.responseDump);
        }

        return (String) envelope.getResponse();
    }

    /*
     * calls the soap function of Ksoap library to invoke soap on server
     */
    private Object call(String method, SoapObject request, boolean retry) throws Exception {

        if (sess_id == null && !method.equalsIgnoreCase("login")) {
            if (login(UserPreferences.userName, UserPreferences.password) == 1) {
                request.setProperty(0, sess_id);
            }
            if (sess_id == null) {
                throw new Exception("Session is null");
            }
        }
        Log.v("SOAPClient","Crew_App Request = >  "+URL+" Parameter :> "+request.toString());


        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        HttpTransportR androidHttpTransport = new HttpTransportR(URL, 60000);
//		androidHttpTransport.debug = true;

        try {
            androidHttpTransport.call(NAMESPACE + "/" + method, envelope);
        } catch (Exception e) {
//            Log.e("Error",e.getMessage());
            if (!method.equalsIgnoreCase("set_value_entry"))
                throw new Exception("Response Dump: " + androidHttpTransport.responseDump);

        }

        String bodyIn = envelope.bodyIn.toString();
        if (!retry && (!method.equalsIgnoreCase("login")|| bodyIn.contains("Login attempt failed")) && (bodyIn.contains("Invalid Session ID") || isSessionExpired(envelope.getResponse())))
        {
            //if session expired
            reset();
            return call(method, request, true);
        }
        Log.e("SOAPClient","Crew_App Response from  "+method+"  = >  "+envelope.getResponse().toString());
        return envelope.getResponse();
    }

    private boolean isSessionExpired(Object res) {
        try {
            if (res instanceof SoapObject) {
                SoapObject error = (SoapObject) res;
                if (error != null) {
                    if (!error.getName().equalsIgnoreCase("error")) {
                        error = (SoapObject) error.getProperty("error");
                    }

                    if (error != null && error.getPropertyCount() >= 2) {
                        String errorNumber = (String) error.getProperty("number");
                        if (errorNumber != null && errorNumber.equals("10")) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            AlertHelper.logError(e);
        }

        return false;
    }

    @Override
    public void resetSyncFlag(String moduleName, int deleted) {

    }

    @Override
    public void resetId(String moduleName, String recordId, String newRecordId) {

    }

    @Override
    public void deleteViaQuery(String moduleName, String where) throws Exception {
        throw new Exception("Removing records is not allowed in online mode");
    }

    @Override
    public ArrayList<ArrayList<String[]>> getEntryListRelationship(
            String relationshipName, String where,
            String orderBy, int offset, String[] select_fields, int total,
            int deleted) throws Exception {
        ArrayList<ArrayList<String[]>> data = new ArrayList<ArrayList<String[]>>();

        SoapObject request = new SoapObject(NAMESPACE, "get_entry_list_relationship");
        request.addProperty("session", sess_id);
        request.addProperty("relationship_name", relationshipName);

        if (where == null) where = "";
        request.addProperty("query", where);

        if (orderBy == null || orderBy.trim().equalsIgnoreCase("")) orderBy = "date_modified desc";
        request.addProperty("order_by", orderBy);
        request.addProperty("offset", offset);

        request.addProperty("select_fields", "");
        request.addProperty("max_results", total);

        request.addProperty("deleted", "" + deleted);

        SoapObject response = (SoapObject) call("get_entry_list_relationship", request, false);

        //If found error throw exception
        SoapObject error = (SoapObject) response.getProperty("error");
        if (error != null && error.getPropertyCount() >= 2) {
            String errorNumber = (String) error.getProperty("number");
            if (errorNumber != null && !errorNumber.equals("0")) {
                throw new Exception((String) error.getProperty("name") + ": " + (String) error.getProperty("description"));
            }
        }

        Vector<SoapObject> records = (Vector<SoapObject>) response.getProperty("entry_list");

        for (int j = 0; j < records.size(); j++) {
            response = records.get(j);
            Vector<SoapObject> record = (Vector<SoapObject>) response.getProperty("name_value_list");

            ArrayList<String[]> tmp = new ArrayList<String[]>();
            for (int i = 0; i < record.size(); i++) {
                response = record.get(i);
                String tmp1[] = new String[2];

                tmp1[0] = (String) response.getProperty("name");
                if (response.getProperty("value") != null) {
                    tmp1[1] = (String) response.getProperty("value");
                } else {
                    tmp1[1] = "";
                }
                tmp.add(tmp1);
            }
            data.add(tmp);
        }

        return data;
    }

    public String setNoteAttachment(String moduleId, String fileName, String actualFile) {
        SoapObject request = new SoapObject(NAMESPACE, "set_note_attachment");

        request.addProperty("session", sess_id);
        SoapObject noteAttachment = new SoapObject(NAMESPACE, "note_attachment");
        noteAttachment.addProperty("id", moduleId);
        noteAttachment.addProperty("fileName", fileName);
        noteAttachment.addProperty("file", actualFile);
        request.addProperty("note_attachment", noteAttachment);


        try {
            SoapObject response = (SoapObject) call("set_note_attachment", request, false);
            if (response != null) {
                SoapObject error = (SoapObject) response.getProperty("error");
                if (error != null && error.getPropertyCount() >= 2) {
                    String errorNumber = (String) error.getProperty("number");
                    if (errorNumber != null && !errorNumber.equals("0")) {
                        return "0";
                    }
                }
            }
        } catch (Exception e) {
            AlertHelper.logError(e);
        }

        return "-1";
    }

    public String[] getNoteAttachment(String Id) {

        String[] files = new String[2];
        SoapObject request = new SoapObject(NAMESPACE, "get_note_attachment");

        request.addProperty("session", sess_id);
        request.addProperty("id", Id);

        try {
            SoapObject response = (SoapObject) call("get_note_attachment", request, false);
            if (response != null) {
                SoapObject noteAttachment = (SoapObject) response.getProperty("note_attachment");

                files[0] = (String) noteAttachment.getProperty("file");
                files[1] = (String) noteAttachment.getProperty("filename");
                SoapObject error = (SoapObject) response.getProperty("error");
                if (error != null && error.getPropertyCount() >= 2) {
                    String errorNumber = (String) error.getProperty("number");
                    if (errorNumber != null && !errorNumber.equals("0")) {
                        return files;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return files;
    }

    /*
     * returns array of IDs for related array of mentioned module for a given record
     */
    @Override
    public String[] getRaltionships(String moduleName, String moduleID, String relatedModuleName, boolean performingSync, int deleted) throws Exception {
        SoapObject request = new SoapObject(NAMESPACE, "get_relationships");

        String where = "";
        if (performingSync && UserPreferences.lastSyncRelationShip != null) {
            where = "date_modified >=" + UserPreferences.lastSyncRelationShip;
        }

		/* Creating input parameters*/
        request.addProperty("session", sess_id);
        request.addProperty("module_name", moduleName);
        request.addProperty("module_id", moduleID);
        request.addProperty("related_module", relatedModuleName);
        request.addProperty("related_module_query", where);
        request.addProperty("deleted", deleted);

        SoapObject response = (SoapObject) call("get_relationships", request, false);

        //Checking error
        if (response != null && response.getPropertyCount() >= 2) {
			/*SoapObject error = (SoapObject) response.getProperty("error");
			String errorNumber = (String) error.getProperty("number");
			if (errorNumber != null && !errorNumber.equals("0")) {
				throw new Exception((String)error.getProperty("name") + ": " + (String)error.getProperty("description"));
			}*/
        }

        Vector<SoapObject> ids = (Vector<SoapObject>) response.getProperty("ids");
        String[] idsArr = new String[ids.size()];
        for (int k = 0; k < ids.size(); k++) {
            String id = (String) ids.get(k).getProperty("id");
            idsArr[k] = id;
        }

        return idsArr;
    }

}
