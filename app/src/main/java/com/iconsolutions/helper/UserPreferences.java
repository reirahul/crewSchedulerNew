package com.iconsolutions.helper;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

import rolustech.beans.ModuleConfig;
import rolustech.beans.RelationshipConfig;
import rolustech.helper.AlertHelper;
import rolustech.helper.ObjectHelper;

public class UserPreferences {
    public static final String APP_NAME = "CREW APP";

    public static final int LIVEDATA = 0;
    public static final int OFFLINEDATA = 1;
    public static final int CACHEDATA = 2;

    public static String lastSync = null;
    public static String lastSyncRelationShip = null;

    //Invalid certificate on https
    public static boolean invalidCertificate = false;

    public static boolean syncRunning = false;

    //Server information
    public static String userID = null;
    public static String url = null;
    public static String displayUrl = null;
    public static String userName = null;
    public static String password = null;

    // Server information for new user registration
    public static final String REGISTER_URL = "http://rsugarcrm.rolustech.com/soap.php";
    public static final String REGISTER_USERNAME = "21B8FEEE65145370EE066F350AE829FC";
    public static final String REGISTER_PASSWORD = "F146418F1EEDA99115E3FE9CCA7CA8B0";

    //	public static final String SERVER_URL = "http://12.32.158.182:8181/gd_dev2/soap.php";
    public static final String SERVER_URL = "http://mgdl.gardendesignlandscaping.com/gd_dev2/soap.php";

    public static int mode = LIVEDATA;
    public static int importLimit = 200;
    public static int imageVarify = 0;
    public static int displayLimit = 200;
    public static String assignedUser = "All";
    public static String selectedDate = "";
    public static String PREFS_SYSTEM_TYPE = "commercial";
    public static String SYSTEM_TYPE_COMMERCIAL = "commercial";
    public static String SYSTEM_TYPE_RESIDENTIAL = "residential";

    //SignUP information
    public static String appId = null;
    public static String name = null;
    public static String department = null;
    public static String company = null;
    public static String email = null;

    public static boolean firstLaunch = true;

    //moduleConfig
    public static HashMap<String, ModuleConfig> moduleConfiguration = null;
    public static HashMap<String, RelationshipConfig> relationshipsConfiguration = null;

    public static boolean loaded = false;
    public static boolean launchSync = false;
    public static boolean useExtendedImport = false;
    public static boolean usingV2Soap = false;
    public static boolean encryptDb = false;

    //Sync Information Dialog
    public static boolean syncModuleDialog = true;
    public static boolean syncAllDialog = true;

    //In App Purchase
    public static final String SKU_MONTHLY_ITEMS = "monthlysub";
    public static final String SKU_ANNUAL_ITEMS = "yearlysub";
    public static final String SKU_PURCHASE_TIME_YEARLY = null;
    public static final String SKU_PURCHASE_TIME_MONTHLY = null;
    public static boolean isMonthlySub = false;
    public static boolean isAnnualSub = false;
    public static long monthlyPurchaseTime = 0;
    public static long yearlyPurchaseTime = 0;
    public static String trialDate = "";
    public static int trialPeriod = 7;

    public static HashMap<String, String> WorkOrderHoursRequest = null;
    public static HashMap<String, String> LineItemsQtyRequest = null;

    @SuppressWarnings("unchecked")
    public static boolean reLoadPrefernces(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences("rsugarcrmConfig", Activity.MODE_PRIVATE);

            url = sp.getString("url", null);
            displayUrl = sp.getString("displayUrl", null);
            userName = sp.getString("userName", null);
            password = sp.getString("password", null);

            mode = sp.getInt("mode", LIVEDATA);
            importLimit = sp.getInt("importLimit", 200);
            displayLimit = sp.getInt("displayLimit", 200);
            assignedUser = sp.getString("assignedUser", "All");
            selectedDate = sp.getString("selectedDate", "");

            appId = sp.getString("appId", null);
            name = sp.getString("name", null);
            PREFS_SYSTEM_TYPE = sp.getString("JobSystemType", SYSTEM_TYPE_COMMERCIAL);
            if(PREFS_SYSTEM_TYPE.equalsIgnoreCase("Both"))
                PREFS_SYSTEM_TYPE = SYSTEM_TYPE_COMMERCIAL;
            department = sp.getString("department", null);
            company = sp.getString("company", null);
            email = sp.getString("email", null);

            firstLaunch = sp.getBoolean("firstLaunch", true);

            moduleConfiguration = (HashMap<String, ModuleConfig>) ObjectHelper.unserialize(sp.getString("moduleConfiguration", null));
            relationshipsConfiguration = (HashMap<String, RelationshipConfig>) ObjectHelper.unserialize(sp.getString("relationshipsConfiguration", null));

            lastSync = sp.getString("lastSync", null);
            lastSyncRelationShip = sp.getString("lastSyncRelationShip", null);

            launchSync = sp.getBoolean("launchSync", false);
            syncModuleDialog = sp.getBoolean("syncModuleDialog", true);
            syncAllDialog = sp.getBoolean("syncAllDialog", true);

            syncRunning = sp.getBoolean("syncRunning", false);

            useExtendedImport = sp.getBoolean("useExtendedImport", false);
            usingV2Soap = sp.getBoolean("useV2Soap", false);
            encryptDb = sp.getBoolean("encryptDB", false);

            //User Details
            userID = sp.getString("userID", null);
//            userID = "3";

            WorkOrderHoursRequest = (HashMap<String, String>) ObjectHelper.unserialize(sp.getString("workorder_hrs", null));
            LineItemsQtyRequest = (HashMap<String, String>) ObjectHelper.unserialize(sp.getString("lineitems_qty", null));
            loaded = true;

        } catch (Exception e) {
            AlertHelper.logError(e);
            clear();
        }

        return loaded;
    }

    public static void loadInAppPreferences(Context context) {
        SharedPreferences sp = context.getSharedPreferences("RSugarCRMPro", Activity.MODE_PRIVATE);

        trialDate = sp.getString("trialStartDate", "");
        isMonthlySub = sp.getBoolean(UserPreferences.SKU_MONTHLY_ITEMS, false);
        isAnnualSub = sp.getBoolean(UserPreferences.SKU_ANNUAL_ITEMS, false);
        monthlyPurchaseTime = sp.getLong(UserPreferences.SKU_PURCHASE_TIME_MONTHLY, 0);
        yearlyPurchaseTime = sp.getLong(UserPreferences.SKU_PURCHASE_TIME_YEARLY, 0);
    }

    public static boolean save(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences("rsugarcrmConfig", Activity.MODE_PRIVATE);

            Editor editor = sp.edit();

            editor.putString("url", url);
            editor.putString("displayUrl", displayUrl);
            editor.putString("userName", userName);
            editor.putString("password", password);

            editor.putInt("mode", mode);
            editor.putInt("importLimit", importLimit);
            editor.putInt("displayLimit", displayLimit);
            editor.putString("assignedUser", assignedUser);
            editor.putString("selectedDate", selectedDate);

            editor.putString("appId", appId);
            editor.putString("name", name);
            editor.putString("department", department);
            editor.putString("JobSystemType", PREFS_SYSTEM_TYPE);
            editor.putString("company", company);
            editor.putString("email", email);
            editor.putBoolean("firstLaunch", false);
            editor.putString("userID", userID);

            editor.putString("moduleConfiguration", ObjectHelper.serialize(moduleConfiguration));
            editor.putString("relationshipsConfiguration", ObjectHelper.serialize(relationshipsConfiguration));

            editor.putString("lastSync", lastSync);
            editor.putString("lastSyncRelationShip", lastSyncRelationShip);

            editor.putBoolean("launchSync", launchSync);

            editor.putBoolean("syncModuleDialog", syncModuleDialog);
            editor.putBoolean("syncAllDialog", syncAllDialog);

            editor.putBoolean("syncRunning", syncRunning);

            editor.putBoolean("useExtendedImport", useExtendedImport);
            editor.putBoolean("useV2Soap", usingV2Soap);
            editor.putBoolean("encryptDB", encryptDb);

            editor.putString("workorder_hrs", ObjectHelper.serialize(WorkOrderHoursRequest));
            editor.putString("lineitems_qty", ObjectHelper.serialize(LineItemsQtyRequest));

            editor.commit();
            return true;
        } catch (Exception e) {
            AlertHelper.logError(e);
        }
        return false;
    }
    public static boolean saveSystemType(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences("rsugarcrmConfig", Activity.MODE_PRIVATE);

            Editor editor = sp.edit();
            editor.putString("JobSystemType", PREFS_SYSTEM_TYPE);
            editor.commit();

            return true;
        } catch (Exception e) {
            AlertHelper.logError(e);
        }
        return false;
    }
    public static void clear() {
        try {
            //Loading with default
            userID = null;
            lastSync = null;
            lastSyncRelationShip = null;

            url = null;
            displayUrl = null;
            userName = null;
            password = null;

            mode = LIVEDATA;
            importLimit = 200;
            displayLimit = 200;
            assignedUser = "All";
            selectedDate = "";

            appId = null;
            name = null;
            department = null;
            PREFS_SYSTEM_TYPE = SYSTEM_TYPE_COMMERCIAL;
            company = null;
            email = null;

            moduleConfiguration = null;
            relationshipsConfiguration = null;

            launchSync = false;

            syncModuleDialog = true;
            syncAllDialog = true;

            syncRunning = false;

            WorkOrderHoursRequest = null;
            LineItemsQtyRequest = null;

            loaded = false;
        } catch (Exception e) {
            AlertHelper.logError(e);
        }
    }

}
