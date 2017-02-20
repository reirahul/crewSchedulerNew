package rolustech.beans;

import java.io.Serializable;

public class Field implements Serializable {
	protected static final long serialVersionUID = 3633147792281288011L;
	public String name = "";
	public String type = "";
	public String dbType = "";
	public String [][]options; // 0 for key and 1 for display values of drop-downs
	public String label = "";
	public String value = "";
	public boolean required = false;
	public boolean updated = false;
	public String relatedModule = "";
	public String idName = "";
	public String typeName = "";
	
	public Field(String name, String type, String dbType, String[][] options, String label, String value, String relatedModule, String idName, String typeName, boolean required) {
		this.name = name;
		this.type = type;
		this.dbType = dbType;
		this.options = options;
		this.value = value;
		this.label = label;
		this.relatedModule = relatedModule;
		this.idName = idName;
		this.typeName = typeName;
		this.required = required;


	}

	public void loadValue(String key) {
		value = getValue(key);
	}
	
	public String getValue(String key) {
		if(options != null){
			for(int i=0; i<options[0].length; i++){
				if(options[0][i].equalsIgnoreCase(key)){
					return options[1][i];
				}
			}
		}
		
		return key;
	}
	
	public String getKey() {
		if(options != null){
			for(int i=0; i<options[1].length; i++){
				if(options[1][i].equalsIgnoreCase(value)){
					return options[0][i];
				}
			}
		}
		
		return value;
	}
}
