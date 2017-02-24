package rolustech.communication;

import android.content.Context;

import com.iconsolutions.helper.UserPreferences;
import rolustech.communication.cacheable.CacheableClient;
import rolustech.communication.db.DBClient;
import rolustech.communication.soap.SOAPClient;
import rolustech.communication.soap.SOAPClientV2;
import rolustech.helper.AlertHelper;
import rolustech.helper.NetworkHelper;

public class CommunicationFactory {
	/*
	 * Returns the communicator according to the mode selected
	 * DBClient for offline mode 
	 * Soap Client for online mode
	 */
	public static Communicator getCommunicator(Context context, boolean forceSOAP, boolean forceDB){
		Communicator com = null;
		int ret = 1;
		if(!UserPreferences.loaded){
			UserPreferences.reLoadPrefernces(context);
		}
		
		if( (forceSOAP || NetworkHelper.isAvailable(context)) && !forceDB){
			if(UserPreferences.usingV2Soap) {
				//return soap object
				com = new SOAPClientV2(UserPreferences.url);
				if(SOAPClientV2.getSess_id() == null){
					ret = com.login(UserPreferences.userName, UserPreferences.password);
				}
			} else {
				//return soap object
				com = new SOAPClient(UserPreferences.url);
				if(SOAPClient.getSess_id() == null){
					ret = com.login(UserPreferences.userName, UserPreferences.password);
				}
			}
		}else if(UserPreferences.mode == UserPreferences.CACHEDATA && !forceDB){
			com = new CacheableClient(context);
			ret = com.login(UserPreferences.userName, UserPreferences.password);
		}
		else{
			//return db object
			com = new DBClient(context);
			ret = com.login(null, null);
		}
		
		if(ret != 1){
			com = null;
			AlertHelper.logError(new Exception("Acquirring Communicator Failed"));
		}
		
		return com;
	}
}
