package rolustech.helper;

import org.kobjects.base64.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectHelper {
	
	public static String serialize(Object o) throws Exception {
		if(o == null){
			return null;
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream obj_out = new ObjectOutputStream(bos);
        obj_out.writeObject(o);
        obj_out.flush();
        obj_out.close();
		return Base64.encode(bos.toByteArray());
	}
	
	public static Object unserialize(String str) throws Exception {
		if(str == null || str.equalsIgnoreCase("null")){
			return null;
		}
		byte bytes[] = Base64.decode(str);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream obj_in = new ObjectInputStream(bis);
		Object o = obj_in.readObject();
		obj_in.close();
		return o;
	}
}
