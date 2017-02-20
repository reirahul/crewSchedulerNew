package rolustech.helper;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadHelper {

	private String FOLDER_NAME = "/rSugarCRM/SyncFiles/";
	
	public void downloadZippedFile(String module, String path) throws Exception {
		module = module + ".zip";
		
		File dir;
		try{
			if (path == null || path.trim().equalsIgnoreCase("")|| path.trim().equalsIgnoreCase("-1")) {
				//throw new Exception("Empty URL");
			}
	
			URL url = new URL(path);
			URLConnection conn = url.openConnection();
	
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setRequestMethod("GET");
			httpConn.connect();
	       
			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				//throw new Exception(httpConn.getResponseCode() + "");
			}
			
			dir = new File(Environment.getExternalStorageDirectory(), FOLDER_NAME);
			if(!dir.exists()){
				dir.mkdirs();
			}

			//Downloading zip file
			FileOutputStream fo = new FileOutputStream(Environment.getExternalStorageDirectory() + FOLDER_NAME + File.separator + module);
			InputStream is = new BufferedInputStream(httpConn.getInputStream());

			byte data[] = new byte[1024];
			int count;
			while ((count = is.read(data)) != -1) {
				try {
					fo.write(data, 0, count);
				} catch(Exception e) {
					AlertHelper.logError(e);
				}
			}

			fo.flush();
			fo.close();
			is.close();
		} catch (Exception e) {
			AlertHelper.logError(e);
			File zipFile = new File(Environment.getExternalStorageDirectory() + FOLDER_NAME + File.separator + module);
			if(zipFile.exists()){
				zipFile.delete();
			}
		}

//		startZipExtraction(module);
//		SDCardHelper.deleteFromCache(FOLDER_NAME, module);

        String zipFile = Environment.getExternalStorageDirectory() + FOLDER_NAME + File.separator + module;
        String unzipLocation = Environment.getExternalStorageDirectory() + FOLDER_NAME;

        Decompress d = new Decompress(zipFile, unzipLocation);
        d.unzip();
        Log.d("TAG: ", "DONE Unzipping of"+d.toString());
	}

	public void startZipExtraction(String module) {
		try {
			File zipFile = new File(Environment.getExternalStorageDirectory() + FOLDER_NAME + File.separator + module);
			unzipFile(zipFile);
		} catch (Exception e) {
			AlertHelper.logError(e);
		}
	}

	public void unzipFile(File zipfile) {

		File zipFile = zipfile;
		String directory = null;

		directory = Environment.getExternalStorageDirectory() + FOLDER_NAME;
	
		new UnZip(zipFile, directory).run();
	}

	public class UnZip {

		File archive;
		String outputDir;

		public UnZip(File ziparchive, String directory) {
			archive = ziparchive;
			outputDir = directory;
		}


		@SuppressWarnings("rawtypes")
		public void run() {
			try {
				ZipFile zipfile = new ZipFile(archive);
				for (Enumeration e = zipfile.entries(); 
						e.hasMoreElements();) {
					ZipEntry entry = (ZipEntry) e.nextElement();
					Log.v("zip entry ", entry.getName());
					unzipEntry(zipfile, entry, outputDir);
				}
			} catch (Exception e) {
				AlertHelper.logError(e);
			}
		}

		@SuppressWarnings("rawtypes")
		public void unzipArchive(File archive, String outputDir) {
			try {
				ZipFile zipfile = new ZipFile(archive);
				for (Enumeration e = zipfile.entries(); 
						e.hasMoreElements();) {
					ZipEntry entry = (ZipEntry) e.nextElement();
					unzipEntry(zipfile, entry, outputDir);
				}
			} catch (Exception e) {
				AlertHelper.logError(e);
			}
		}

		private void unzipEntry(ZipFile zipfile, ZipEntry entry,
				String outputDir) throws IOException {

			if (entry.isDirectory()) {
				createDir(new File(outputDir, entry.getName()));
				return;
			}
			File outputFile = new File(outputDir, entry.getName());
			if (!outputFile.getParentFile().exists()) {
				createDir(outputFile.getParentFile());
			}

			BufferedInputStream inputStream = new 
					BufferedInputStream(zipfile
							.getInputStream(entry));
			BufferedOutputStream outputStream = new BufferedOutputStream(
					new FileOutputStream(outputFile));

			try {
				copyStream(inputStream, outputStream);
			} finally {
				outputStream.close();
				inputStream.close();
			}
		}

		private void createDir(File dir) {
			if (!dir.mkdirs())
				throw new RuntimeException("Can not create dir " + dir);
		}
	}
	
	public static void copyStream(BufferedInputStream is, BufferedOutputStream os) {
        final int buffer_size=8192;
        try {
            byte[] bytes=new byte[buffer_size];
            for(;;) {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
}



/**
 *
 * @author jon
 */
final class Decompress {
	private String _zipFile;
	private String _location;

	public Decompress(String zipFile, String location) {
		_zipFile = zipFile;
		_location = location;

		_dirChecker("");
	}

	public void unzip() {
		try  {
			FileInputStream fin = new FileInputStream(_zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			while ((ze = zin.getNextEntry()) != null) {
				Log.v("Decompress", "Unzipping " + ze.getName());

				if(ze.isDirectory()) {
					_dirChecker(ze.getName());
				} else {
					FileOutputStream fout = new FileOutputStream(_location + ze.getName());
					for (int c = zin.read(); c != -1; c = zin.read()) {
						fout.write(c);
					}

					zin.closeEntry();
					fout.close();
				}

			}
			zin.close();
		} catch(Exception e) {
			Log.e("Decompress", "unzip", e);
		}

	}

	private void _dirChecker(String dir) {
		File f = new File(_location + dir);

		if(!f.isDirectory()) {
			f.mkdirs();
		}
	}
}
