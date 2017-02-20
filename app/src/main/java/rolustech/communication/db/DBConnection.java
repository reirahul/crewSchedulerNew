package rolustech.communication.db;


import android.content.Context;
import android.os.Environment;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.util.Locale;

import rolustech.beans.Field;
import rolustech.beans.ModuleConfig;
import rolustech.beans.UserPreferences;
import rolustech.helper.AlertHelper;

public class DBConnection{
	private static SQLiteDatabase db;
	public static final String DB_PATH = Environment.getExternalStorageDirectory() + File.separator + UserPreferences.APP_NAME + File.separator;
	public static final String SYNC_PATH = Environment.getExternalStorageDirectory() + File.separator + UserPreferences.APP_NAME + File.separator + "Sync";
	public static final String DB_NAME = "data.db";
	public static final String SYNC_COLUMN = "sync_on_save";

/*
 * Returns database connection for the local DB
 */

	public static synchronized SQLiteDatabase getConnection(Context context){

		if(db != null && !new File(DB_PATH + DB_NAME).exists()){
			db.close();
			db = null;
		}

		if(db == null){
			try{
				SQLiteDatabase.loadLibs(context);
		        String encryptStr = "";
				if(UserPreferences.encryptDb) {
					encryptStr = "rolustech123";
				}

				if(new File(DB_PATH + DB_NAME).exists()){
					db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, encryptStr, null);
				}else{
					if(!UserPreferences.loaded){
						UserPreferences.reLoadPrefernces(context);
					}

					if(UserPreferences.moduleConfiguration == null){
						throw new Exception("Modules are not cofigured.");
					}

					db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, encryptStr, null);
					executeSchema(context);
				}
				db.setVersion(1);
				db.setLocale(Locale.getDefault());
				db.setLockingEnabled(true);
			}catch(Exception e){
				db = null;
				AlertHelper.logError(e);
			}
		}

		return db;
	}

	private static void executeSchema(Context context) throws Exception {
		Object[] modules = UserPreferences.moduleConfiguration.keySet().toArray();

		for(int i=0; i<modules.length; i++){
			String modName = (String) modules[i];
			ModuleConfig config = UserPreferences.moduleConfiguration.get(modName);

			if(config != null && config.fields != null){
				//creating main table
				String query = "CREATE TABLE IF NOT EXISTS " + modName.toLowerCase() + " (id TEXT, " + SYNC_COLUMN + " TEXT";
				String cstmQuery = "";

				Object[] fields = config.fields.keySet().toArray();

				for(int j=0; j<fields.length; j++){
					Field field = config.fields.get(fields[j]);

					//Skip id field
					if(field.name.equalsIgnoreCase("id")){
						continue;
					}

					//Skip _c fields if using extended import
					if(UserPreferences.useExtendedImport && field.name.endsWith("_c")){
						cstmQuery += ", " + field.name + " TEXT";
						continue;
					}

					query += ", " + field.name + " TEXT";
				}

				query += ", PRIMARY KEY(\"id\"));";

				//Logging create table query
				AlertHelper.logError(new Exception(modName + " - Create Query: " + query));

				db.execSQL(query);

				//execute cstm table query if using extended import
				if(UserPreferences.useExtendedImport && !cstmQuery.equalsIgnoreCase("")){
					cstmQuery = "CREATE TABLE IF NOT EXISTS " +	modName.toLowerCase() +	"_cstm (id_c TEXT" + cstmQuery + ");";

					//Logging create table query
					AlertHelper.logError(new Exception(modName + " - Create Cstm Query: " + cstmQuery));

					db.execSQL(cstmQuery);
				}
			}
		}
	}

/*
 * reset database connection
 */
	public static void resetDbConnection() {
		if(db != null) {
			db.close();
			db = null;
		}
	}
}