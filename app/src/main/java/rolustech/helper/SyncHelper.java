package rolustech.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

import java.io.InputStream;

import com.iconsolutions.helper.UserPreferences;

public class SyncHelper {
	private ProgressDialog dialog;
	private Handler handler = new Handler();
	private Activity activity;
	private Context context;
	private boolean syncAllFlag = false, removeDbFlag = false;
	private InputStream inputStream;

	public void performSync(final Activity act, boolean syncAll, boolean removeOldDb, Context con) {
		this.context = con;
		this.activity = act;
		this.syncAllFlag = syncAll;
		this.removeDbFlag = removeOldDb;

		if (this.activity != null && !NetworkHelper.isAvailable(this.activity)) {
			AlertHelper.showAlert(this.activity, "No Connectivity!", "Please check your internet connection and try again.");
			return;
		}

		if (removeDbFlag) {
			ImportHelper.removeDB();
			UserPreferences.lastSync = null;
			UserPreferences.lastSyncRelationShip = null;
		}

		String syncNewVal = DateHelper.getCurrentDbDateTime();
		String failedModules;


		failedModules = FileBasedSync.performSync(con, true, dialog);

		UserPreferences.lastSync = syncNewVal;
		UserPreferences.save(con);
	}

}
