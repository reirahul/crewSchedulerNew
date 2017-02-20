package rolustech.helper;

import android.app.Activity;
import android.util.DisplayMetrics;

public class ScreenHelper {
	
	public static float getScreenDensity(Activity act) {
			DisplayMetrics metrics = new DisplayMetrics();
			act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			return metrics.density;
	}
	
	public static int getScreenDensityDpi(Activity act) {
		DisplayMetrics metrics = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.densityDpi;
	}
	
	public static int getScreenWidth(Activity act) {
		DisplayMetrics metrics = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.widthPixels;
	}

	public static int getScreenHeight(Activity act) {
		DisplayMetrics metrics = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.heightPixels;
	}
}
