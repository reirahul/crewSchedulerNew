package rolustech.beans;

import android.content.Context;

import com.iconsolutions.helper.UserPreferences;

public class Relationship extends SugarBean {
	public Relationship(Context context){
		super(context, "Relationships");
	}
	
	public SugarBean[] retrieveByModule(String moduleName) throws Exception {
		// TODO Auto-generated method stub
		if(moduleName == null){
			return null;
		}
		
		String modules = "", sep = "";
		Object[] mods = UserPreferences.moduleConfiguration.keySet().toArray();
		for(int i=0; i<mods.length; i++){
			if(UserPreferences.moduleConfiguration.get(mods[i]).available){
				modules += sep + "'" + (String) mods[i] + "'";
				sep = ",";
			}
		}
		
		String where = 	"(" +
							"lhs_module IN (" + modules + ") AND rhs_module IN (" + modules + ")" +
						") AND (" +
							"(lhs_module='" + moduleName + "' AND relationship_type='one-to-many')" +
							" OR " +
							"(" +
								"(rhs_module='" + moduleName + "' OR lhs_module='" + moduleName + "')" +
								" AND " +
								"relationship_type='many-to-many'" +
							")" +
						")";
		
		int size = (int) getRecordsCount(where, 0);

		return retrieveAll(where, "id asc", -1, size, 0, null);
	}
}
