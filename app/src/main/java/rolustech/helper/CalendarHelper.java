package rolustech.helper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;

import java.util.Calendar;

import rolustech.beans.SugarBean;

public class CalendarHelper {
	
	public static void createCalendarEvents(Activity activity, SugarBean bean) {
		// get calendar
		Calendar cal = Calendar.getInstance();     
		Uri EVENTS_URI = Uri.parse(getCalendarUriBase(activity) + "events");
		ContentResolver cr = activity.getContentResolver();

		// event insert
		ContentValues values = new ContentValues();
		values.put("calendar_id", 1);
		values.put("title", "Event from RSugar "+ bean.moduleName + ": " + bean.getListTitle());
		values.put("allDay", 0);
		
		//for half hour before reminder
//		values.put("dtstart", DateHelper.convertDateToMilliSeconds(bean.getFieldValue("date_start")) - 30*60*1000);// cal.getTimeInMillis() + 11*60*1000); // event starts at 11 minutes from now
//		values.put("dtend", DateHelper.convertDateToMilliSeconds(bean.getFieldValue("date_start")) + 60*60*1000); // ends 60 minutes from now

		values.put("dtstart", DateHelper.convertDateToMilliSeconds(bean.getFieldValue("date_start")));
		values.put("dtend", DateHelper.convertDateToMilliSeconds(bean.getFieldValue("date_start"))  + 60*60*1000); // ends 60 minutes from now
		
		values.put("description", bean.getFieldValue("description"));
		//values.put("visibility", 0);
		values.put("hasAlarm", 1);
		values.put("eventTimezone", Time.getCurrentTimezone());
		Uri event = cr.insert(EVENTS_URI, values);

		// reminder insert
		Uri REMINDERS_URI = Uri.parse(getCalendarUriBase(activity) + "reminders");
		values = new ContentValues();
		values.put( "event_id", Long.parseLong(event.getLastPathSegment()));
		values.put( "method", 1 );
		values.put( "minutes", 10 );
		cr.insert( REMINDERS_URI, values );
		
		AlertHelper.showAlert(activity, "Reminder Added", null);
	}
	
	public static String getCalendarUriBase(Activity act) {

	    String calendarUriBase = null;
	    Uri calendars = Uri.parse("content://calendar/calendars");
	    Cursor managedCursor = null;
	    try {
	        managedCursor = act.managedQuery(calendars, null, null, null, null);
	    } catch (Exception e) {
	    }
	    if (managedCursor != null) {
	        calendarUriBase = "content://calendar/";
	    } else {
	        calendars = Uri.parse("content://com.android.calendar/calendars");
	        try {
	            managedCursor = act.managedQuery(calendars, null, null, null, null);
	        } catch (Exception e) {
	        }
	        if (managedCursor != null) {
	            calendarUriBase = "content://com.android.calendar/";
	        }
	    }
	    return calendarUriBase;
	}

}
