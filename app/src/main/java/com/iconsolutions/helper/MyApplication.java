package com.iconsolutions.helper;

import android.app.Application;

import com.iconsolutions.crewschedular.R;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by kashif on 6/2/16.
 */
@ReportsCrashes(mailTo = "mubashir.hussain@rolustech.com", customReportContent = {
        ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME,
        ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL,
        ReportField.CUSTOM_DATA, ReportField.STACK_TRACE }, mode = ReportingInteractionMode.SILENT)


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        super.onCreate();
        ACRA.init(this);

        // add device data in crash report

    }

}
