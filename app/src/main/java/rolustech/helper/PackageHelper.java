package rolustech.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;

public class PackageHelper {

/**
 * Returns Package Information of the package
 * @param act [Activity]
 * @return PackageInfo [Contains version name and version code]
 * 
 */
	public static PackageInfo getPackageInfo(Activity act) {
		try {
			return act.getPackageManager().getPackageInfo(act.getPackageName(), 0);
		} catch (Exception e) {
			AlertHelper.logError(e);
			return null;
		}
	}

/**
 * Returns the version name String of the application
 * @param act [Activity]
 * @return versionNumber [String containing version of the application package]
 */
	public static String getVersionString(Context act) {
		String version = null;
		String appendBeta = "";//" Beta";
		
		try {
			PackageInfo info = act.getPackageManager().getPackageInfo(act.getPackageName(), 0);
			version = "Version: " + info.versionName.toString() + appendBeta;
		} catch (Exception e) {
			AlertHelper.logError(e);
			version = "";
		}
		
		//For beta only
		//version = "Version: 3.5 Beta";
		
		return version;
	}
	public static String getVersion(Context act) {
		String version = null;
		
		try {
			PackageInfo info = act.getPackageManager().getPackageInfo(act.getPackageName(), 0);
			version = info.versionName.toString();
		} catch (Exception e) {
			AlertHelper.logError(e);
			version = "";
		}
		
		return version;
	}
	

}
