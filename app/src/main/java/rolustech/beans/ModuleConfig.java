package rolustech.beans;

import java.io.Serializable;
import java.util.Hashtable;

import rolustech.helper.AlertHelper;

public class ModuleConfig implements Serializable {
	protected static final long serialVersionUID = 753756371974218870L;
	public String name = null;
	public Hashtable<String, Field> fields = null;
	public boolean available = true;
	
	public ModuleConfig(String name, Hashtable<String, Field> fields, boolean available){
		this.name = name;
		this.fields = fields;
		this.available = available;
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

}
