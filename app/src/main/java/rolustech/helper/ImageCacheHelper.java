package rolustech.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageCacheHelper {

	public static void writeToCache(Bitmap bitmap, String path, String filename){
		try{
			File dir = new File(Environment.getExternalStorageDirectory(), path);
			if(!dir.exists()){
				dir.mkdirs();
			}

			File image = new File(dir, filename);
			if(!image.exists()){
				image.createNewFile();

				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

				FileOutputStream fo = new FileOutputStream(image);
				fo.write(bytes.toByteArray());
				fo.close();
			}
		}catch (Exception e) {
			AlertHelper.logError(e);
		}
	}

	public static Bitmap readFromCache(String path, String filename){
		try{
			File dir = new File(Environment.getExternalStorageDirectory(), path);
			if(dir.exists()){
				File image = new File(dir, filename);
				if(image.exists()){
					FileInputStream fi = new FileInputStream(image);
					Bitmap bitmap = BitmapFactory.decodeStream(fi, null, null);
					fi.close();
					return bitmap;
				}
			}
		}catch (Exception e) {
			AlertHelper.logError(e);
		}
		return null;
	}

	public static Bitmap readFromCache(File file){
		try{
			if(file.exists()){
				FileInputStream fi = new FileInputStream(file);
				Bitmap bitmap = BitmapFactory.decodeStream(fi, null, null);
				fi.close();
				return bitmap;
			}
		}catch (Exception e) {
			AlertHelper.logError(e);
		}
		return null;
	}

	public static boolean deleteFromCache(String path, String filename) {
		try{
			File dir = new File(Environment.getExternalStorageDirectory(), path);
			if(dir.exists()){
				File image = new File(dir, filename);
				if(image.exists()){
					image.delete();
					return true;
				}
			}
		}catch (Exception e) {
			AlertHelper.logError(e);
		}

		return false;
	}

	public static byte[] readFileFromCache(String path, String filename){
		try{
		File dir = new File(Environment.getExternalStorageDirectory(), path);
		if(dir.exists()){
		File image = new File(dir, filename);
		if(image.exists()){
		FileInputStream fi = new FileInputStream(image);
		byte[] ba = readBytesFromFile(image);
		fi.close();
		return ba;
		}
		}
		}catch (Exception e) {
		AlertHelper.logError(e);
		}
		return null;
		}
	public static byte[] readBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
		throw new IOException("Could not completely read file " + file.getName() + " as it is too long (" + length + " bytes, max supported " + Integer.MAX_VALUE + ")");
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int)length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
		offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
		throw new IOException("Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
		}
}
