package rolustech.helper;

import java.util.ArrayList;
import java.util.Collections;

import rolustech.beans.Field;

public class FieldsSorter {
	public static ArrayList<Field> sortFields(ArrayList<Field> fields) {
		for(int i = 0; i < fields.size(); i++) {
			for(int j = 0; j < fields.size() - 1; j++) {
				if(shouldSwap(fields.get(j).name, fields.get(j + 1).name)) {
					Collections.swap(fields, j, j + 1);
				}
			}
		}
		
		return fields;
	}

	public static boolean shouldSwap(String jay, String jayPlusOne) {
		int length = 0; 

		if(jay.length() < jayPlusOne.length()) {
			length = jay.length();
		} else {
			length = jayPlusOne.length();
		}

		boolean flag = false;
		for(int i = 0; i < length; i++) {
			if(jay.charAt(i) == jayPlusOne.charAt(i)) {
				continue;
			} else {
				if(jay.charAt(i) > jayPlusOne.charAt(i)) {
					flag = true;
				} else {
					flag = false;
				}
				break;
			}
		}
		return flag;
	}
	//public static boolean sortReqFields(String fieldName,ArrayList<Field> fields) {
		
//	}

	public static boolean sortReqFields(String name,
			ArrayList<Field> sortedfields) {
		boolean flag = true;
		for(int i = 0; i < sortedfields.size(); i++) {
			
			if(name.equalsIgnoreCase(sortedfields.get(i).name)){
				flag = false;
			}
		}
		
		return flag;
	}
}
