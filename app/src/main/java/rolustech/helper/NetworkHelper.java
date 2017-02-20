package rolustech.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkHelper {

	public static boolean locationDetected = true;

	public static boolean isAvailable(Context context) {
		try{
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			if (info == null){
				return false;
			}
			
			return info.isConnectedOrConnecting();
		} catch (Exception e) {
			AlertHelper.logError(e);
		}
		
		return false;
	}

	//Checking if URL is accessible
	public static boolean isURLAccessable(String URL){
		try{
			URL url = new URL(URL);
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestProperty("Connection", "close");
			urlc.setConnectTimeout(2500);
			urlc.connect();
			if (urlc.getResponseCode() == 200) {
				return true;
			}
		}catch (Exception e) {
			AlertHelper.logError(e);
		}
		
		return false;
	}
}
