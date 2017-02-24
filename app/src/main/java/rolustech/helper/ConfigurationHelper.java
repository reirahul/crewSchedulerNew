package rolustech.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import rolustech.beans.Field;
import rolustech.beans.ModuleConfig;
import com.iconsolutions.helper.UserPreferences;
import rolustech.communication.soap.SOAPClient;
//import rsugarcrm.adapters.ModuleListAdapter;

public class ConfigurationHelper {
	public static void setConfigurationModules(ArrayList<String> modules) {
		boolean fieldsImport = false;
		if(UserPreferences.moduleConfiguration == null){
			UserPreferences.moduleConfiguration = new HashMap<String, ModuleConfig>();
			fieldsImport = true;
		}
		
		//Add all modules here from configuration
		for(int i = 0; i < modules.size(); i++) {
			String name = modules.get(i);
			
			if(UserPreferences.moduleConfiguration.get(name) == null){
				UserPreferences.moduleConfiguration.put(name, new ModuleConfig(name, null, true));
			}else{
				UserPreferences.moduleConfiguration.get(name).available = true;
			}
		}

		//Adding users module
		if(UserPreferences.moduleConfiguration.get("Users") == null){
			UserPreferences.moduleConfiguration.put("Users", new ModuleConfig("Users", null, true));
		}
		
		//Importing fields only once when application was installed
		if(fieldsImport){
			NormalSync.importFields();
		}
		
		addDefaultModules();
	}

	protected static void addDefaultModules() {
		if(UserPreferences.moduleConfiguration.get("Relationships") == null){
			Hashtable<String, Field> fields = new Hashtable<String, Field>();
			
			fields.put("id",new Field("id", "id", "", null, "", "", "", "", "", false));
			fields.put("relationship_name",new Field("relationship_name", "relationship_name", "", null, "", "", "", "", "", false));
			fields.put("lhs_module",new Field("lhs_module", "lhs_module", "", null, "", "", "", "", "", false));
			fields.put("lhs_table",new Field("lhs_table", "lhs_table", "", null, "", "", "", "", "", false));
			fields.put("lhs_key",new Field("lhs_key", "lhs_key", "", null, "", "", "", "", "", false));
			fields.put("rhs_module",new Field("rhs_module", "rhs_module", "", null, "", "", "", "", "", false));
			fields.put("rhs_table",new Field("rhs_table", "rhs_table", "", null, "", "", "", "", "", false));
			fields.put("rhs_key",new Field("rhs_key", "rhs_key", "", null, "", "", "", "", "", false));
			fields.put("join_table",new Field("join_table", "join_table", "", null, "", "", "", "", "", false));
			fields.put("join_key_lhs",new Field("join_key_lhs", "join_key_lhs", "", null, "", "", "", "", "", false));
			fields.put("join_key_rhs",new Field("join_key_rhs", "join_key_rhs", "", null, "", "", "", "", "", false));
			fields.put("relationship_type",new Field("relationship_type", "relationship_type", "", null, "", "", "", "", "", false));
			fields.put("relationship_role_column",new Field("relationship_role_column", "relationship_role_column", "", null, "", "", "", "", "", false));
			fields.put("relationship_role_column_value",new Field("relationship_role_column_value", "relationship_role_column_value", "", null, "", "", "", "", "", false));
			fields.put("reverse",new Field("reverse", "reverse", "", null, "", "", "", "", "", false));
			fields.put("deleted",new Field("deleted", "deleted", "", null, "", "", "", "", "", false));
			
			UserPreferences.moduleConfiguration.put("Relationships", new ModuleConfig("Relationships", fields, false));
		}
	}
	
	public static void addMissingFields() {
		
	}
	
	public static void logModuleFields(String moduleName) {
        SOAPClient sc = new SOAPClient(UserPreferences.url);
        int ret = sc.login(UserPreferences.userName, UserPreferences.password);
        if(ret == 1) {
        	try {
				Hashtable<String,Field> fields = sc.getModulesFields("Users");
				for (int i = 0; i < fields.size(); i++) {
					AlertHelper.logMessage("", "fields.put('" + fields.get(i + "").name + "', new Field("
							+ "'" + fields.get(i + "").name + "', "
							+ "'" + fields.get(i + "").type + "', "
							+ " options, "
							+ "'" + fields.get(i + "").label + "',"
							+ "'" + fields.get(i + "").value + "'));");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
	}
}
