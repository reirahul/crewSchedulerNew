package rolustech.helper;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import rolustech.beans.ModuleConfig;
import rolustech.beans.Relationship;
import rolustech.beans.RelationshipConfig;
import rolustech.beans.SugarBean;
import rolustech.beans.UserPreferences;
import rolustech.communication.db.DBClient;

public class RelationshipHelper {
	/*
	 * Loads Relationship Configuration from server.
	 */
	public static void loadRelationshipConfiguration(Context context) {
		try {
			Object[] modules = UserPreferences.moduleConfiguration.keySet().toArray();
			for(int i = 0; i < modules.length; i++) {
				SugarBean[] relatedBeans = getRelatedDefs((String)modules[i], context);
			}
		} catch(Exception e) {
			UserPreferences.reLoadPrefernces(context);
			AlertHelper.logError(e);
		}
	}
	
	/*
	 * Saves the received relationships beans into preferences.
	 */
	private static void saveRelationshipDefs(Context context, String moduleName, SugarBean[] relatedBeans) {
		if(UserPreferences.relationshipsConfiguration == null){
			UserPreferences.relationshipsConfiguration = new HashMap<String, RelationshipConfig>();
		}
		
		HashMap<String, ModuleConfig> relatedModules = new HashMap<String, ModuleConfig>();
		
		for(int j = 0; j < relatedBeans.length; j++) {
			String relatedModuleName = relatedBeans[j].getFieldValue("lhs_module");
			if(relatedModuleName.equalsIgnoreCase(moduleName)) {
				relatedModuleName = relatedBeans[j].getFieldValue("rhs_module");
			}
			
			ModuleConfig moduleConfig =	new ModuleConfig(
											relatedBeans[j].getFieldValue("relationship_name"), 
											relatedBeans[j].fields, 
											true);
			relatedModules.put(relatedModuleName, moduleConfig);
		}
		
		RelationshipConfig relationshipConfig = new RelationshipConfig(moduleName,	relatedModules);
		
		UserPreferences.relationshipsConfiguration.put(moduleName, relationshipConfig);
		UserPreferences.save(context);
	}
	
	/*
	 * Checks the relationships between two given modules and returns boolean accordingly
	 */
	public static boolean relationshipExist(String moduleName, String relatedModuleName) {
		try {
			RelationshipConfig relationshipConfig = UserPreferences.relationshipsConfiguration.get(moduleName);
			ModuleConfig relatedModule = relationshipConfig.relatedModules.get(relatedModuleName);
			if((relatedModuleName.equalsIgnoreCase(relatedModule.getFieldValue("lhs_module")))
				|| relatedModuleName.equalsIgnoreCase(relatedModule.getFieldValue("rhs_module"))
				&&
				!(relatedModule.getFieldValue("lhs_module").equalsIgnoreCase(relatedModule.getFieldValue("rhs_module")))
				) {
				return true;
			}
		} catch(Exception e) {
			AlertHelper.logError(e);
		}
		
		return false;
	}
	
	/*
	 * Returns all the relationship definitions for provided module name
	 */
	public static SugarBean[] getRelatedDefs(String moduleName, Context context){
		Relationship relationshipBean = new Relationship(context);
		SugarBean[] relatedBeans;
		try {
			//relatedBeans = relationshipBean.retrieveByModule(TempStorage.bean.moduleName);
		} catch (Exception e) {
			AlertHelper.logError(new Exception(moduleName + ": Default Relationships fetched."));
		}
		
		// this function call added to update the related-beans-configuration in User Preferences
//		saveRelationshipDefs(context, moduleName, relatedBeans);
		
		return null;
	}
	
	/*
	 * returns the relationship defs of both modules
	 */
	public static SugarBean getRelationshipDef(String module, String relatedModule, Context context){
		//returns all relationship defs for module
		SugarBean[] relatedDefs = getRelatedDefs(module, context);
		
		if(relatedDefs == null){
			return null;
		}
		
		for(SugarBean sbean : relatedDefs){
			//If related module is returned in the relationships
			if(
				(
					sbean.getFieldValue("lhs_module").equalsIgnoreCase(relatedModule)
					&&
					sbean.getFieldValue("rhs_module").equalsIgnoreCase(module)
				)
				||
				(
					sbean.getFieldValue("rhs_module").equalsIgnoreCase(relatedModule)
					&&
					sbean.getFieldValue("lhs_module").equalsIgnoreCase(module)
				)
			){
				try {
					if(!sbean.getFieldValue("join_table").equalsIgnoreCase("NULL") && !sbean.getFieldValue("join_table").equalsIgnoreCase("") ){
						DBClient com = new DBClient(context);
						com.creatRelationshipTable(
								sbean.getFieldValue("join_table"),
								new String[]{
									sbean.getFieldValue("join_key_lhs"),
									sbean.getFieldValue("join_key_rhs"),
									sbean.getFieldValue("relationship_role_column")
								});
						}

					} catch (Exception e) {
						AlertHelper.logError(e);
					}
				return sbean;
			}
		}
		return null;
	}
	
	/*
	 * returns the module names of all related module for the provided module name
	 */
	public static String[] getRelatedModulesName(String moduleName, Context context){
		SugarBean[] relatedDefs = getRelatedDefs(moduleName, context);
		
		if(relatedDefs == null){
			return null;
		}
		
		ArrayList<String> relatedModule = new ArrayList<String>();
		for(SugarBean sbean : relatedDefs){
			String module = sbean.getFieldValue("lhs_module");
			
			if(sbean.getFieldValue("lhs_module").equalsIgnoreCase(moduleName)){
				module = sbean.getFieldValue("rhs_module");
			}
			
			if(
				!relatedModule.contains(module) && 
				UserPreferences.moduleConfiguration.get(module) != null &&
				UserPreferences.moduleConfiguration.get(module).available
			){
				relatedModule.add(module);
			}
		}
		
		String[] relatedModules = new String[relatedModule.size()];
		
		return relatedModule.toArray(relatedModules);
	}
	
	/*
	 * returns the count of relted records
	 */
	public static long getRelatedCount(String where, String relatedModule, Activity context){
		if(where == null){
			return -1;
		}
		
		SugarBean bean = new SugarBean(context, relatedModule);
		
		try {
			return bean.getRecordsCount(where, 0);
		} catch (Exception e) {
			AlertHelper.logError(e);
		}
		
		return -1;
	}
	
	/*
	 * returns the where clause to get related records
	 */
	public static String getRelationshipWhere(SugarBean relDefBean, String module, String moduleId, String joinTableDate, String syncWhere, int deleted){
		if(relDefBean == null){
			return null;
		}
		
		//related module is always rhs
		if(relDefBean.getFieldValue("join_table").equalsIgnoreCase("NULL") || relDefBean.getFieldValue("join_table").trim().equalsIgnoreCase("")){
			return 	relDefBean.getFieldValue("rhs_table") + "." + 
					relDefBean.getFieldValue("rhs_key") + "='" +moduleId + "'";
/*					"' AND 1=1 AND " +
					relDefBean.getFieldValue("rhs_table") + "." + 
					relDefBean.getFieldValue("rhs_key") + " IS NOT NULL"; */
		}
		
		//relationship is many-to-many
		String where = "";
		if(relDefBean.getFieldValue("lhs_module").equalsIgnoreCase(module)){
			//related module is rhs
			where += relDefBean.getFieldValue("rhs_table") + "." +relDefBean.getFieldValue("rhs_key");
			where += " IN (SELECT " + relDefBean.getFieldValue("join_table")+"."+relDefBean.getFieldValue("join_key_rhs") + " FROM ";
			where += relDefBean.getFieldValue("join_table") + " WHERE deleted='" + deleted + "' AND ";
			if(joinTableDate != null){
				where += "date_modified>='" + joinTableDate + "' AND ";
			}
			if(syncWhere != null){
				where += syncWhere + " AND ";
			}
			where += relDefBean.getFieldValue("join_key_lhs") + "='" + moduleId + "')";
/*			where += " AND 1=1 AND " + relDefBean.getFieldValue("rhs_table") + "." +relDefBean.getFieldValue("rhs_key") + 
					" IS NOT NULL";*/
		}else{
			//related module is lhs
			where += relDefBean.getFieldValue("lhs_table") + "." +relDefBean.getFieldValue("lhs_key");
			where += " IN (SELECT " + relDefBean.getFieldValue("join_table")+"."+relDefBean.getFieldValue("join_key_lhs") + " FROM ";
			where += relDefBean.getFieldValue("join_table") + " WHERE deleted='" + deleted + "' AND ";
			if(joinTableDate != null){
				where += "date_modified>='" + joinTableDate + "' AND ";
			}
			if(syncWhere != null){
				where += syncWhere + " AND ";
			}
			where += relDefBean.getFieldValue("join_key_rhs") + "='" + moduleId + "')";
/*			where += " AND 1=1 AND " + relDefBean.getFieldValue("lhs_table") + "." +relDefBean.getFieldValue("lhs_key") +
					" IS NOT NULL";*/
		}
		
		return where;
	}
}
