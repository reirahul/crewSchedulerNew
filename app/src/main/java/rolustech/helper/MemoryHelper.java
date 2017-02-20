package rolustech.helper;

import android.view.View;
import android.view.ViewGroup;

public class MemoryHelper {

/**
 * Cleans memory associated with the drawable
 * @param rootView Root-view of the activity.
 */
	public static void cleanMemory(View rootView) {
		try {
			unbindDrawables(rootView);
		} catch (Exception e) {
			AlertHelper.logError(e);
		}

		System.gc();
	}
	
/**
 * Explores the view tree recursively and:
 * 1) Removes callbacks on all the background drawables.
 * 2) Removes childs on every viewgroup.
 * @param view
 */
    public static void unbindDrawables(View view) throws Exception {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}
