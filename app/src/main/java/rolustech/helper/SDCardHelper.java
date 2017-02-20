package rolustech.helper;

import android.os.Environment;

import java.io.File;

public class SDCardHelper {

/*
 * Reads a file from SD-Card with given path and file name
 */
	public static File readFileFromCache(String path, String filename){
		try{
			File dir = new File(Environment.getExternalStorageDirectory(), path);
			if(dir.exists()){
				File file = new File(dir, filename);
				if(file.exists()){
					return file;
				}
			}
		}catch (Exception e) {
			AlertHelper.logError(e);
		}
		return null;
	}

/*
 * Deletes a file from SD-Card with given path and file name
 */
	public static boolean deleteFromCache(String path, String filename) {
		try{
			File dir = new File(Environment.getExternalStorageDirectory(), path);
			if(dir.exists()){
				File file = new File(dir, filename);
				if(file.exists()){
					file.delete();
					return true;
				}
			}
		}catch (Exception e) {
			AlertHelper.logError(e);
		}
		
		return false;
	}
	
}
