package rolustech.helper;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.iconsolutions.helper.UserPreferences;
import rolustech.communication.db.DBConnection;

/**
 * Created by kashif on 5/4/16.
 */
public class FileSyncHelper {

        public static String syncDatabase(String URL, Context con) throws Exception {
            if (URL == null || URL.trim().equalsIgnoreCase("")|| URL.trim().equalsIgnoreCase("-1")) {
                throw new Exception("Empty URL");
            }

            java.net.URL url = new URL(URL);
            URLConnection conn = url.openConnection();

            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception(httpConn.getResponseCode() + "");
            }


            File dir = new File(Environment.getExternalStorageDirectory(), UserPreferences.APP_NAME);
            if(!dir.exists()){
                dir.mkdirs();
            }

            //Downloading zip file
            FileOutputStream fo = new FileOutputStream(DBConnection.SYNC_PATH + "sync.zip");
            InputStream is = new BufferedInputStream(httpConn.getInputStream());

            byte data[] = new byte[1024];
            int count;
            while ((count = is.read(data)) != -1) {
                fo.write(data, 0, count);
            }
            fo.flush();
            fo.close();
            is.close();

            ZipInputStream zin = new ZipInputStream(new FileInputStream(DBConnection.SYNC_PATH + "sync.zip"));
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                if (!ze.isDirectory()) {
                    fo = new FileOutputStream(DBConnection.SYNC_PATH);
                    while ((count = zin.read(data)) != -1) {
                        fo.write(data, 0, count);
                    }
                    fo.flush();
                    fo.close();
                    zin.close();
                    break;
                }
            }

            //Removing zip file
            File zip = new File(DBConnection.SYNC_PATH + "sync.zip");
            if(zip.exists()){
                zip.delete();
            }

//		if (UserPreferences.usingV2Soap) {
//			return "Success";
//		} else {
            return "Success";
//		}
        }

}
