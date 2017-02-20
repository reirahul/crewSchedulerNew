package rolustech.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class DateHelper {
	public static String dbFormat = "yyyy-MM-dd HH:mm:ss";
	public static String displayFormat = "MM/dd/yyyy hh:mm a";
	
	public static String toDB(String displayDate){
		if(displayDate == null || displayDate.trim().equalsIgnoreCase("")) return "";
		
		try {
			SimpleDateFormat srcFormat = new SimpleDateFormat(displayFormat);
			srcFormat.setTimeZone(getCurrentTimeZone());
			
			Date d = srcFormat.parse(displayDate);
			
			SimpleDateFormat targetFormat = new SimpleDateFormat(dbFormat);
			targetFormat.setTimeZone(new SimpleTimeZone(0,"GMT"));
			
			return targetFormat.format(d.getTime());
		} catch (ParseException e) {
			AlertHelper.logError(e);
			return "";
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String toDBAlarm(String displayDate){
		if(displayDate == null || displayDate.trim().equalsIgnoreCase("")) return "";
		
		try {
			SimpleDateFormat srcFormat = new SimpleDateFormat(displayFormat);
			srcFormat.setTimeZone(getCurrentTimeZone());
			
			Date d = srcFormat.parse(displayDate);
			d.setMinutes(d.getMinutes() - 15);
			
			SimpleDateFormat targetFormat = new SimpleDateFormat(dbFormat);
			targetFormat.setTimeZone(new SimpleTimeZone(0,"GMT"));
			
			return targetFormat.format(d.getTime());
		} catch (ParseException e) {
			AlertHelper.logError(e);
			return "";
		}
	}
	
	public static String toDBDate(String displayDate){
		if(displayDate == null || displayDate.trim().equalsIgnoreCase("")) return "";
		
		try {
			SimpleDateFormat srcFormat =  new SimpleDateFormat(displayFormat.split(" ")[0]);
			
			Date d = srcFormat.parse(displayDate.trim());
			
			SimpleDateFormat targetFormat = new SimpleDateFormat(dbFormat.split(" ")[0]);
			
			return targetFormat.format(d.getTime());
		} catch (ParseException e) {
			AlertHelper.logError(e);
			return "";
		}
	}
	
	public static String toDisplay(String dbDate){
		if(dbDate == null || dbDate.trim().equalsIgnoreCase("")) return "";
		
		try {
			SimpleDateFormat srcFormat =  new SimpleDateFormat(dbFormat);
			srcFormat.setTimeZone(new SimpleTimeZone(0,"GMT"));
			
			Date d = srcFormat.parse(dbDate.trim());
			
			SimpleDateFormat targetFormat = new SimpleDateFormat(displayFormat);
			srcFormat.setTimeZone(getCurrentTimeZone());
			
			return targetFormat.format(d.getTime());
		} catch (ParseException e) {
			AlertHelper.logError(e);
			return "";
		}
	}

	public static String toDisplayDate(String dbDate) {
		if(dbDate == null || dbDate.trim().equalsIgnoreCase("")) return "";
		
		try {
			SimpleDateFormat srcFormat =  new SimpleDateFormat(dbFormat.split(" ")[0]);
			
			Date d = srcFormat.parse(dbDate.trim());
			
			SimpleDateFormat targetFormat = new SimpleDateFormat(displayFormat.split(" ")[0]);
			
			return targetFormat.format(d.getTime());
		} catch (ParseException e) {
			AlertHelper.logError(e);
			return "";
		}
	}
	
	protected static TimeZone getCurrentTimeZone() {
		return TimeZone.getDefault();
	}

/*
 * return 1 if date1 is greater, 0 for eaqual and 2 if date2 is greater
 */
	public static int compareDates(String date1, String date2) {
		if(date1.trim().equalsIgnoreCase(date2.trim())) return 0;
		
		SimpleDateFormat format =  new SimpleDateFormat(dbFormat);
		try {
			Date d1 = format.parse(date1.trim());
			Date d2 = format.parse(date2.trim());
			
			if(d2.getTime() > d1.getTime()) return 2;
		} catch (ParseException e) {
			AlertHelper.logError(e);
			return 0;
		}
		return 1;
	}	
	
	public static String getCurrentDisplayDateTime(){
		SimpleDateFormat sdf = new SimpleDateFormat(displayFormat);
		sdf.setTimeZone(TimeZone.getDefault());
		
		return sdf.format(new Date().getTime());
	}

	public static String getCurrentDbDateTime() {
		return toDB(getCurrentDisplayDateTime());
	}	
	
	public static String getCurrentDisplayDate(){
		SimpleDateFormat sdf = new SimpleDateFormat(displayFormat.split(" ")[0]);
		sdf.setTimeZone(TimeZone.getDefault());
		
		return sdf.format(new Date().getTime());
	}

	public static String getCurrentDbDate() {
		return toDBDate(getCurrentDisplayDate());
	}
	
	public static void registerDateDialog (final Activity activity, final TextView selectedField,final long timeInMilli) {
		final DatePickerDialog.OnDateSetListener datepickersetLister = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
				String d = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year;
				selectedField.setText(d);
			}
		};
		
		selectedField.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int mYear, mMonth, mDay;
				Calendar c = Calendar.getInstance();
				if(timeInMilli!=0){
					c.setTimeInMillis(timeInMilli);
				}
				mYear = c.get(Calendar.YEAR);
				mMonth = c.get(Calendar.MONTH);
				mDay = c.get(Calendar.DAY_OF_MONTH);
				
				DatePickerDialog datePicker = new DatePickerDialog(activity, datepickersetLister, mYear,mMonth, mDay);
				datePicker.setTitle("Select Date");
				datePicker.show();
			}
		});	
	}
	
	public static String[] getTodayTimeBounds(){
		
		String[] dateBounds = new String[2];
		 
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(new SimpleTimeZone(0,"GMT"));
		
		Date d = new Date();
	
		d.setHours(0);
		d.setMinutes(0);
		d.setSeconds(0);
		
		dateBounds[0] = format.format(d.getTime());
		
		d.setHours(23);
		d.setMinutes(59);
		d.setSeconds(59);
		
		dateBounds[1] = format.format(d.getTime());
		return dateBounds;
	}
	public static String getMySetDate(String date){
		
		String returnDate = "";
		if(!date.equals("")){
			String[] sParts = date.split("[/:\\s+]");
			int[] iParts = new int[sParts.length];
			
			for(int i = 0; i < sParts.length; i++)
			    iParts[i] = Integer.parseInt(sParts[i]);
			   
			    	returnDate +=iParts[2]+"-";
			    	returnDate +=iParts[0]+"-";
			    	returnDate +=iParts[1]+" 19:00:00";
		}
		return returnDate;
	}
	
	public static long convertDateToMilliSeconds(String date) {
		long l = 0;
		date = toDisplay(date);
		
		try {
			SimpleDateFormat targetFormat = new SimpleDateFormat(displayFormat);
			Date dt = targetFormat.parse(date);
			l = dt.getTime();
		} catch (Exception e) {
			
		}
				
		return l;
	}
	
	public static int[] splitToComponentTimes(String Seconds)
	{
	    long longVal = Integer.parseInt(Seconds);
	    int hours = (int) longVal / 3600;
	    int remainder = (int) longVal - hours * 3600;
	    int mins = remainder / 60;
	    remainder = remainder - mins * 60;
	  //  int secs = remainder;

	    int[] ints = {hours , mins };
	    return ints;
	}
	public static String convertToCurrentTZ(String dbDate){
		if(dbDate == null || dbDate.trim().equalsIgnoreCase("")) return "";
		
		try {
			SimpleDateFormat srcFormat =  new SimpleDateFormat(dbFormat);
			srcFormat.setTimeZone(new SimpleTimeZone(0,"GMT"));
			
			Date d = srcFormat.parse(dbDate.trim());
			
			//SimpleDateFormat targetFormat = new SimpleDateFormat(displayFormat);
			srcFormat.setTimeZone(getCurrentTimeZone());
			
			return srcFormat.format(d.getTime());
		} catch (ParseException e) {
			AlertHelper.logError(e);
			return "";
		}
	}
	
	public static String convertToGMTTZ(String dbDate){
		if(dbDate == null || dbDate.trim().equalsIgnoreCase("")) return "";
		
		try {
			SimpleDateFormat srcFormat =  new SimpleDateFormat(dbFormat);
			srcFormat.setTimeZone(getCurrentTimeZone());
			
			Date d = srcFormat.parse(dbDate.trim());
			
			srcFormat.setTimeZone(new SimpleTimeZone(0,"GMT"));
			
			return srcFormat.format(d.getTime());
		} catch (ParseException e) {
			AlertHelper.logError(e);
			return "";
		}
	}
	
	public static String getDate(String date){
		if(date.length() > 0){
			return date.substring(0,10);
		}
		return "";
	}
	
	//for formatting calendar date
		public static String getCalDate(long time){
			SimpleDateFormat formatter = new SimpleDateFormat(dbFormat);

		    // Create a calendar object that will convert the date and time value in milliseconds to date. 
		     Calendar calendar = Calendar.getInstance();
		     calendar.setTimeInMillis(time);
		     return formatter.format(calendar.getTime());
			
		}
}
