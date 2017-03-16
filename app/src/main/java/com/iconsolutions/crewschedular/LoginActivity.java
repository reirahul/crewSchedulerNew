package com.iconsolutions.crewschedular;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.app.AlertDialog;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import rolustech.beans.SugarBean;
import com.iconsolutions.helper.UserPreferences;
import rolustech.communication.CommunicationFactory;
import rolustech.communication.soap.SOAPClient;
import rolustech.communication.soap.SOAPClientV2;
import rolustech.helper.AlertHelper;
import rolustech.helper.ConfigurationHelper;
import rolustech.helper.ImportDatabase;
import rolustech.helper.NetworkHelper;
import rolustech.helper.NormalSync;
import rolustech.helper.RelationshipHelper;
import rolustech.helper.SoapHelper;
import rolustech.helper.SyncHelper;

import static com.iconsolutions.crewschedular.MainActivity.hasPermissions;


/**
 * Created by kashif on 3/22/16.
 */
public class LoginActivity extends Activity implements OnClickListener {

    EditText email_tv, password_tv;
    ImageView login_btn;
    Button importBtn;
    private ProgressDialog dialog;
    private int state = -1;
    private Boolean temp = false;
    private SOAPClient soapClient;
    String title, msg;

    private ArrayList<String> availableModules = null;
    private ArrayList<String> forbiddenModules = new ArrayList<String>();
    private ArrayList<String> allowedModules = new ArrayList<String>();
    private ArrayList<String> modulesListForDistributor = new ArrayList<String>();

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.INTERNET};
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        initUI();
    }

    private void initUI() {
//        getActionBar().hide();
        email_tv = (EditText) findViewById(R.id.email);
        password_tv = (EditText) findViewById(R.id.password);
        login_btn = (ImageView) findViewById(R.id.login_btn);
        importBtn = (Button) findViewById(R.id.import_btn);
        importBtn.setVisibility(View.INVISIBLE);
        login_btn.setOnClickListener(this);
        importBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.login_btn:
                loginUser();
                break;
            case R.id.import_btn:
                importDatabase();
                break;
        }
    }

    private void startMainActivity() {
        UserPreferences.reLoadPrefernces(this);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

    }



    private void loginUser() {

        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String url = SoapHelper.generateUrl(UserPreferences.SERVER_URL.trim());
                    String id = email_tv.getText().toString().trim();
                    String pass = password_tv.getText().toString().trim();
                    // checking soap versions
                    if (UserPreferences.usingV2Soap && !url.contains(SOAPClientV2.SOAP_URL)) {
                        url = url.replace(SOAPClient.SOAP_URL, SOAPClientV2.SOAP_URL);
                    } else if (!UserPreferences.usingV2Soap) {
                        url = url.replace(SOAPClientV2.SOAP_URL, SOAPClient.SOAP_URL);
                    }

                    if (url.equalsIgnoreCase("") || id.equalsIgnoreCase("") || pass.equalsIgnoreCase("")) {
                        state = 1;
                    }
                    else {

                        //UserPreferences.useExtendedImport = useExtendedImport.isChecked();
                        UserPreferences.encryptDb = false;

                        if (temp.compareTo(UserPreferences.encryptDb) != 0) {
                            //removing DB file
                            NormalSync.removeDB();
                        }

                        boolean urlChanged = !url.equals(UserPreferences.url);
                        UserPreferences.url = url;
                        UserPreferences.userName = id;
                        UserPreferences.password = pass;

                        if (NetworkHelper.isAvailable(getApplicationContext())) {
                            SOAPClient com = new SOAPClient(UserPreferences.url);
                            int ret = com.login(UserPreferences.userName, UserPreferences.password);
                            if (ret == 1) {
                                try {
                                    if (urlChanged) {
                                        UserPreferences.moduleConfiguration = null;
                                        //removing DB file
                                        NormalSync.removeDB();
                                    }
                                    UserPreferences.userID = com.getCurrentUserID();
                                } catch (Exception e) {
                                    AlertHelper.logError(e);
                                }
                                state = 4;
                            }
                            else {
                                if (UserPreferences.invalidCertificate) {
                                    state = 3;
                                    UserPreferences.invalidCertificate = false;
                                }
                                else {
                                    state = 2;
                                }
                            }

                        }
                        else {
                            state = 0;
                        }

                        if (state == 4) {
                            if (!UserPreferences.save(LoginActivity.this)) {
                                //Reset SharePreference Now
                                UserPreferences.reLoadPrefernces(getApplicationContext());
                                state = 5;
                            }
                            else {
                                SugarBean.loadCom(getApplicationContext(), false, false);
                                soapClient = (SOAPClient) CommunicationFactory.getCommunicator(LoginActivity.this, true, false);
                                if (soapClient != null) {
                                    availableModules = soapClient.getAvailableModules();
                                }
                                else {
                                    int ret;
                                    if (UserPreferences.usingV2Soap) {
                                        //return soap object
                                        soapClient = new SOAPClientV2(UserPreferences.url);
                                        if (SOAPClientV2.getSess_id() == null) {
                                            ret = soapClient.login(UserPreferences.userName, UserPreferences.password);
                                        }
                                    }
                                    else {
                                        //return soap object
                                        soapClient = new SOAPClient(UserPreferences.url);
                                        if (SOAPClient.getSess_id() == null) {
                                            ret = soapClient.login(UserPreferences.userName, UserPreferences.password);
                                        }
                                    }
                                    if (soapClient != null) {
                                        availableModules = soapClient.getAvailableModules();
                                    }
                                }
                            }

                            if (availableModules == null || availableModules.size() == 0) {
                                if (UserPreferences.moduleConfiguration == null) {
                                    AlertHelper.showAlert(LoginActivity.this, "Error!", "SOAP Connection failed");
                                    return;
                                } else {
                                    availableModules = new ArrayList<String>();

                                    Object[] mods = UserPreferences.moduleConfiguration.keySet().toArray();
                                    for (int i = 0; i < mods.length; i++) {
                                        availableModules.add((String) mods[i]);
                                    }
                                }
                            }

                            if (availableModules != null) {
                                Collections.sort(availableModules);
                            }

                            //Setting modules
                            populateForbiddenModules();
                            populateAllowedModules();
                            ConfigurationHelper.setConfigurationModules(modulesListForDistributor);
                            RelationshipHelper.loadRelationshipConfiguration(LoginActivity.this);

                            SugarBean bean = new SugarBean(getApplicationContext(), "Users");
                            SugarBean[] newBeans = bean.retrieveAll("users.id = '" + UserPreferences.userID + "'", "", 0, 1, 0, null);
                            if (newBeans != null && newBeans.length > 0) {
                                SugarBean object = newBeans[0];
                                UserPreferences.name = object.getFieldValue("first_name") + " " + object.getFieldValue("last_name");
                                UserPreferences.PREFS_SYSTEM_TYPE = object.getFieldValue("system_job_type");
                                UserPreferences.department = object.getFieldValue("department");
/*
                                ArrayList<String> bf = new ArrayList<>();
                                ArrayList<String> bf1 = new ArrayList<String>();
                                StringBuffer finalData = new StringBuffer();
                                for (String value : object.getNameArray(true))
                                    bf.add(value);
                                for (String value : object.getValueArray(true))
                                    bf1.add(value);
                                for (int i = 0; i < bf.size(); i++)
                                    finalData.append(bf.get(i) + " = " + bf1.get(i) + "\n");
                                Log.e("LOGIN", "Responce of Server= " + finalData.toString());

*/
                            }

                            SugarBean.loadCom(LoginActivity.this, false, false);
                            if (!UserPreferences.save(getApplicationContext())) {
                                AlertHelper.showAlert(LoginActivity.this, "Error!", "Unable to save configurations");
                            } else {

                            }
                        }
                    }

                } catch (Exception e) {
//                    Log.d("Exception: ", e.getMessage());
                }

//                UserPreferences.mode = UserPreferences.OFFLINEDATA;
                UserPreferences.save(LoginActivity.this);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleResponse();
                    }
                });
            }
        }).start();

    }




    public void handleResponse() {
        dialog.dismiss();

        //re-loading preferences to avoid raw data
        UserPreferences.reLoadPrefernces(LoginActivity.this);

        switch (state) {
            case 0:
                AlertHelper.showAlert(LoginActivity.this, "Error!", "No connectivity found. Please check your network connection");
                break;
            case 1:
                AlertHelper.showAlert(LoginActivity.this, "Error!", "Please fill all fields.");
                break;
            case 2:
                AlertHelper.showAlert(LoginActivity.this, "Error!", "Invalid Credentials");
                break;
            case 3:
                AlertHelper.showAlert(LoginActivity.this, "Error!", "Unable to connect to server.");
                break;
            case 4:
                dialog.dismiss();
                AlertDialog diag = new AlertDialog.Builder(LoginActivity.this).create();
                diag.setCancelable(false);
                diag.setTitle("Do you want.......");
                diag.setButton("Import database", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        importDatabase();
                        dialog.dismiss();
                    }
                });
                diag.setButton2("Import Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startMainActivity();
                    }
                });
                diag.show();
                break;
            case 5:
                AlertHelper.showAlert(LoginActivity.this, "Error!", "Unable to save preferences.");
                break;
        }

        state = -1;
    }

    public void importDatabase() {
        // new SyncHelper().performSync(ServerConfig.this, true, true);
        dialog = ProgressDialog.show(LoginActivity.this, null, "Please Wait...Importing Database");

        new Thread(new Runnable() {

            @Override
            public void run() {
                if (!NetworkHelper.isAvailable(getApplicationContext())) {
                    msg = "Wifi Is Not Enabled";
                    title = "No Network!";
                } else {
                    try {
                        Looper.prepare();
   //                     String url = SoapHelper.generateUrl(UserPreferences.url);
                        SOAPClient soap = new SOAPClient(UserPreferences.url);
                        String link = soap.getDownloadLink();
//                        link = "http://12.32.158.182:8181/gd_dev2/sqliteDB/sqliteZip.zip";
                        if (link == null || link.length() == 0) {
//                            link = "http://12.32.158.182:8181/gd_dev2/sqliteDB/sqliteZip.zip";
                            throw new Exception("Empty URL");
                        }
//                        String link = url.replaceAll("/soap.php", "") + "/" + DBlink;

                        String succ= ImportDatabase.downloadDB(link, getApplicationContext());
                        if(succ.equalsIgnoreCase("Success")) {
                            msg = "Database imported successfully.";
                            title = "";
                            new SyncHelper().performSync(LoginActivity.this, true, false, LoginActivity.this);
                        }

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        dialog.dismiss();
  //                      msg = "Cannot Import Database...Please try again in sometime";
                        msg = e.toString();
                        title = "Error!";
                    }
                }
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        dialog.dismiss();
                        AlertHelper.showAlert(LoginActivity.this, title, msg);
                        if (!title.equalsIgnoreCase("Error!")) {
                            finish();
                            startMainActivity();
                        }
                    }
                });
            }
        }).start();

    }


    public void populateAllowedModules() {
        if (availableModules != null) {
            modulesListForDistributor.addAll(Arrays.asList( "ro_crew_work_order", "AOS_Products_Quotes", "Documents", "DocumentRevisions", "Users", "rt_Jobs","ro_crew_work_line_items"));
            for (int i = 0; i < availableModules.size(); i++) {
//				if(!forbiddenModules.contains(availableModules.get(i).toLowerCase())) {
                if (checkAllowedModules(availableModules.get(i))) {
                    allowedModules.add(availableModules.get(i));
                }
            }
        }
    }

    public Boolean checkAllowedModules(String module_name) {
        if (modulesListForDistributor.contains(module_name))
            return true;

        return false;
    }

    public void populateForbiddenModules() {
        forbiddenModules.add("acl");
        forbiddenModules.add("administration");
        forbiddenModules.add("audit");
        forbiddenModules.add("activities");
        forbiddenModules.add("aclactions");
        forbiddenModules.add("acl_fields");
        forbiddenModules.add("aclfields");
        forbiddenModules.add("calendar");
        forbiddenModules.add("configurator");
        forbiddenModules.add("connectors");
        forbiddenModules.add("currencies");
        forbiddenModules.add("customfields");
        forbiddenModules.add("dashboard");
        forbiddenModules.add("dropdown");
        forbiddenModules.add("dynamic");
        forbiddenModules.add("dynamicfields");
        forbiddenModules.add("dynamiclayout");
        forbiddenModules.add("editcustomfields");
        forbiddenModules.add("eapm");
        forbiddenModules.add("emails");
        forbiddenModules.add("emailtext");
        forbiddenModules.add("emailaddresses");
        forbiddenModules.add("emailtemplates");
        forbiddenModules.add("emailman");
        forbiddenModules.add("emailmarketing");
        forbiddenModules.add("fieldsmetadata");
        forbiddenModules.add("help");
        forbiddenModules.add("home");
        forbiddenModules.add("iframes");
        forbiddenModules.add("import");
        forbiddenModules.add("inboundemail");
        forbiddenModules.add("labeleditor");
        forbiddenModules.add("mailmerge");
        forbiddenModules.add("mergerecords");
        forbiddenModules.add("mysettings");
        forbiddenModules.add("oauthkeys");
        forbiddenModules.add("oauthtokens");
        forbiddenModules.add("releases");
        forbiddenModules.add("optimisticlock");
        forbiddenModules.add("savedsearch");
        forbiddenModules.add("schedulers_jobs");
        forbiddenModules.add("schedulers");
        forbiddenModules.add("studio");
        forbiddenModules.add("sugarfeed");
        forbiddenModules.add("sync");
        forbiddenModules.add("trackers");
        forbiddenModules.add("teammemberships");
        forbiddenModules.add("upgradewizard");
        forbiddenModules.add("userpreferences");
        forbiddenModules.add("versions");
        forbiddenModules.add("groups");
        forbiddenModules.add("employees");
        forbiddenModules.add("campaignlog");
        forbiddenModules.add("aclroles");
        forbiddenModules.add("campaigntrackers");
//        forbiddenModules.add("documentrevisions");
//		forbiddenModules.add("projecttask");
        forbiddenModules.add("prospectlists");
        forbiddenModules.add("roles");
        forbiddenModules.add("relationships");
        forbiddenModules.add("users");
        forbiddenModules.add("reports");
        forbiddenModules.add("teamsets");
        forbiddenModules.add("teamsetmodule");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
