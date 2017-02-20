package rolustech.helper;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardHelper {

	public static void hideKeyboard(Activity context, View view)
	{
		/*context.getWindow().setSoftInputMode(
			    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
			);*/
		
		/*InputMethodManager inputManager = (InputMethodManager) view
	            .getContext()
	            .getSystemService(Context.INPUT_METHOD_SERVICE);

	    IBinder binder = view.getWindowToken();
	    inputManager.hideSoftInputFromWindow(binder,
	            InputMethodManager.HIDE_NOT_ALWAYS);*/
		InputMethodManager inputManager = (InputMethodManager) context
			    .getSystemService(Context.INPUT_METHOD_SERVICE);

			    // check if no view has focus:
			     View v = ((Activity) context).getCurrentFocus();
			     if (v == null)
			        return;

			    inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
}
