package rolustech.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iconsolutions.helper.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		
		int status = NetworkUtil.getConnectivityStatusString(context);
		if (!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            if(status != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
            	
            	Log.e("network status", "network connected");
            	Intent serviceIntent = new Intent(context, SyncService.class);
            	context.startService(serviceIntent);
            	//start background service
//                new ForceExitPause(context).execute();
            }

       }
	}

}
