package rolustech.beans;

import java.io.Serializable;
import java.util.HashMap;

public class RelationshipConfig implements Serializable{

	/**
	 * Auto-generated Serial Version ID
	 */
	private static final long serialVersionUID = 4864176039331814292L;
	
	public String name = null;
	public HashMap<String, ModuleConfig> relatedModules = null;

	public RelationshipConfig(String name, HashMap<String, ModuleConfig> relatedModules){
		this.name = name;
		this.relatedModules = relatedModules;
	}
}
