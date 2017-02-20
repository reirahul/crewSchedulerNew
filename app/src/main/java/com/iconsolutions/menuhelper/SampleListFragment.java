package com.iconsolutions.menuhelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.iconsolutions.crewschedular.CalendarFragment;
import com.iconsolutions.crewschedular.CrewJobsListFragment;
import com.iconsolutions.crewschedular.LoginActivity;
import com.iconsolutions.crewschedular.MainActivity;
import com.iconsolutions.crewschedular.R;
import com.iconsolutions.crewschedular.SampleListAdapter;

import java.io.File;

import rolustech.beans.UserPreferences;
import rolustech.communication.soap.SOAPClient;
import rolustech.helper.AlertHelper;
import rolustech.helper.ImportDatabase;
import rolustech.helper.ImportHelper;
import rolustech.helper.NetworkHelper;
import rolustech.helper.SoapHelper;
import rolustech.helper.SyncHelper;

//import com.refractive.fragments.EventSelectionFragment;
//import com.refractive.fragments.SettingsFragment;

public class SampleListFragment extends Fragment {

    //	FragmentActivity fm;
    View view;
    ProgressDialog dialog;
    String msg, title;
    Handler handler = new Handler();

    Boolean syncInProgress = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.sidemenulist, null);
//		this.fm = getActivity();
        return view;
    }

    public class SampleItem {
        public String tag;
        public int iconRes;

        public SampleItem(String tag, int iconRes) {
            this.tag = tag;
            this.iconRes = iconRes;
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UserPreferences.reLoadPrefernces(getActivity());
        SampleListAdapter adapter = new SampleListAdapter(getActivity());

        adapter.add(new SampleItem("Schedular", 0));
        adapter.add(new SampleItem("Calendar", 0));
        adapter.add(new SampleItem("Sync", 0));
        adapter.add(new SampleItem("Logout", 0));
        adapter.add(new SampleItem("Switch to " + (UserPreferences.PREFS_SYSTEM_TYPE.equalsIgnoreCase(UserPreferences.SYSTEM_TYPE_COMMERCIAL) ? "Residential" : "Commercial"), 0));
        adapter.add(new SampleItem(UserPreferences.name + "/" + UserPreferences.department + "/" + UserPreferences.PREFS_SYSTEM_TYPE, 0));

        ListView lv = (ListView) view.findViewById(R.id.menuList);
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                MainActivity mainActivity = (MainActivity) SampleListFragment.this.getActivity();

                Fragment fragment = null;
                switch (position) {
                    case 0:
                        fragment = new CrewJobsListFragment();
                        break;
                    case 1:
                        fragment = new CalendarFragment();
                        break;
                    case 2:
//					fragment = new SettingsFragment();
//					if(!UserPreferences.syncRunning)
                        performSync();
//					else
//						AlertHelper.showAlert(SampleListFragment.this.getActivity(), getString(R.string.app_name), "Sync is already in progress, please wait a while.");
                        break;
                    case 3:
                        if (NetworkHelper.isAvailable(SampleListFragment.this.getActivity())) {
                            ImportHelper.removeDB();
                            UserPreferences.clear();
                            Intent intent = new Intent(SampleListFragment.this.getActivity(), LoginActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        } else {
                            AlertHelper.showAlert(SampleListFragment.this.getActivity(), getString(R.string.app_name), "Network Not Available");
                        }
                        break;
                    case 4:
                        if (UserPreferences.PREFS_SYSTEM_TYPE.equalsIgnoreCase(UserPreferences.SYSTEM_TYPE_COMMERCIAL)) {
                            UserPreferences.PREFS_SYSTEM_TYPE = UserPreferences.SYSTEM_TYPE_RESIDENTIAL;
                        } else {
                            UserPreferences.PREFS_SYSTEM_TYPE = UserPreferences.SYSTEM_TYPE_COMMERCIAL;
                        }
                        UserPreferences.saveSystemType(getActivity());
                        mainActivity.setSampleListFragment();
                        fragment = new CrewJobsListFragment();

                        break;
                    default:
                        break;
                }
//				if(fragment instanceof CrewJobsListFragment || fragment instanceof CalendarFragment)
//				{
//					mainActivity.switchContent(fragment);
//				} else
                if (fragment != null)
                    mainActivity.addContent(fragment);

            }
        });
        lv.setAdapter(adapter);
    }


    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public void performSync() {
        dialog = ProgressDialog.show(getContext(), null, "Please Wait...Syncing Database");
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (!NetworkHelper.isAvailable(getContext())) {
                    msg = "Wifi Is Not Enabled";
                    title = "No Network!";
                } else {
                    try
                    {
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

                        String succ = ImportDatabase.downloadDB(link, getActivity());
                        if (succ.equalsIgnoreCase("Success")) {
                            new SyncHelper().performSync(getActivity(), true, false, getActivity());
                            sendEmailtoManager();

                            msg = "Successfully Synced";
                            title = UserPreferences.APP_NAME;
                        }

                    }

                   catch (Exception e) {
                        e.printStackTrace();
                        dialog.dismiss();
                        msg = "Cannot Sync Database...Please try again in sometime";
                        title = "Error!";
                    }
                }
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        dialog.dismiss();
                        AlertHelper.showAlert((Activity) getContext(), title, msg);
                    }
                });
            }
        }).start();
    }

    public void sendEmailtoManager() {

        try {

            Object[] wKeys = UserPreferences.WorkOrderHoursRequest.keySet().toArray();
            for (int i = 0; i < wKeys.length; i++) {
                String workOrderId = (String) wKeys[i];
                String hours = UserPreferences.WorkOrderHoursRequest.get(workOrderId);

                SOAPClient soap = new SOAPClient(UserPreferences.url);
                String response = soap.setValueEntry("ro_crew_work_order", hours, workOrderId);
//                if(response != "-1")
                UserPreferences.WorkOrderHoursRequest.remove(workOrderId);
            }

            Object[] lKeys = UserPreferences.LineItemsQtyRequest.keySet().toArray();
            for (int i = 0; i < lKeys.length; i++) {
                String lineItemId = (String) lKeys[i];
                String quantity = UserPreferences.LineItemsQtyRequest.get(lineItemId);

                SOAPClient soap = new SOAPClient(UserPreferences.url);
                String response = soap.setValueEntry("AOS_Products_Quotes", quantity, lineItemId);
//                if(response != "-1")
                UserPreferences.LineItemsQtyRequest.remove(lineItemId);

            }
        } catch (Exception e) {

        }
    }

/*	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		MainActivity mainActivity = (MainActivity)this.getActivity();
		
		Fragment fragment = null;
		switch(position){
		case 0:
			fragment = new DashboardFragment(this.getActivity());
			break;
		case 1:
			break;
		case 2:
			fragment = new SettingsFragment(this.getActivity());
			break;
		case 3:
			break;
		default:
			break;
		}
		mainActivity.switchContent(fragment);
	}*/
}
