package com.bignerdranch.android.runtracker;
/*
 * Provide methods to insert, query, and otherwise managing the data in SQLite
 */
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class RunDatebaseHelper extends SQLiteOpenHelper {	//SQLiteOpenHelper is a helper class to manage database creation and version management.
	private static final String DB_NAME = "runs.sqlite";
	private static final int VERSION = 1;
	
	private static final String TABLE_RUN = "run";
	private static final String COLUMN_RUN_ID = "_id";
	private static final String COLUMN_RUN_START_DATE = "start_date";
	
	private static final String TABLE_LOCATION = "location";
	private static final String COLUMN_LOCATION_LATITUDE = "latitude";
	private static final String COLUMN_LOCATION_LONGITUDE = "longitude";
	private static final String COLUMN_LOCATION_ALTITUDE = "altitude";
	private static final String COLUMN_LOCATION_TIMESTAMP = "timestamp";
	private static final String COLUMN_LOCATION_PROVIDER = "provider";
	private static final String COLUMN_LOCATION_RUN_ID = "run_id";
	
	public RunDatebaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {	    //SQLiteDatabase exposes methods to manage a SQLite database
		// Create the "Run" table
		db.execSQL("create table run (" + 			//Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data
		           "_id integer primary key autoincrement, start_date integer)");
		// Create the "location" table
		db.execSQL("create table location (" + 
				   " timestamp integer, latitude real, longitude real, altitude real," +
				   " provider varchar(100), run_id integer references run(_id))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Implement schema changes and data message here when upgrading
	}
	
	public long insertRun(Run run) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_RUN_START_DATE, run.getStartDate().getTime());
		//.insert() is a convenience method for inserting a row into the database.
		// getWritableDatabase() create and/or open a database that will be used for reading and writing.
		return getWritableDatabase().insert(TABLE_RUN, null, cv);	//Return the ID of the new row	
	}
	
	public long insertLocation(long runId, Location location) {
		ContentValues cv = new ContentValues();							//ContentValues is essentially a set of key-value pairs
		cv.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
		cv.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
		cv.put(COLUMN_LOCATION_ALTITUDE, location.getAltitude());
		cv.put(COLUMN_LOCATION_TIMESTAMP, location.getTime());
		cv.put(COLUMN_LOCATION_PROVIDER, location.getProvider());
		cv.put(COLUMN_LOCATION_RUN_ID, runId);
		return getWritableDatabase().insert(TABLE_LOCATION, null, cv);
	}
	
	public RunCursor queryRuns() {
		//Equivalent to "select * from run order by start_date asc"
		Cursor wrapped = getReadableDatabase().query(TABLE_RUN, 
				null, null, null, null, null, COLUMN_RUN_START_DATE + " asc");
		return new RunCursor(wrapped);
	}
	
	public RunCursor queryRuns(long id) {
		Cursor wrapped = getReadableDatabase().query(TABLE_RUN,
				null, // All columns 
				COLUMN_RUN_ID + " = ?", //Look for a run ID
				new String[] {String.valueOf(id)}, //with this value
				null, //group by
				null, //order by
				null, //having
				"1"); //limit 1 row
		return new RunCursor(wrapped);
	}
	
	public LocationCursor queryLastLocationForRun(long runId) {
		Cursor wrapped = getReadableDatabase().query(TABLE_LOCATION, 
				null, // All columns 
				COLUMN_LOCATION_RUN_ID + " = ?", //limit to the given run
				new String[] {String.valueOf(runId)},  
				null, //group by 
				null, //having 
				COLUMN_LOCATION_TIMESTAMP + " desc", //order by latest first
				"1"); //Limit 1
		return new LocationCursor(wrapped);
	}
	
	/*
	 * A convenience class to wrap a cursor that returns rows from the "run" table
	 * The {@link getRun()} method will give you a Run instance representing
	 * the current row
	 */
	public static class RunCursor extends CursorWrapper {
		
		public RunCursor(Cursor c) {
			super(c);
		}
		
		/*
		 * Returns a Run object configured for the current row,
		 * or null if the current row is invalid
		 */
		public Run getRun() {
			if (isBeforeFirst() || isAfterLast())		//Check to ensure that the cursor is within bounds
				return null;
			
			Run run = new Run();
			long runId = getLong(getColumnIndex(COLUMN_RUN_ID));
			run.setId(runId);
			long startDate = getLong(getColumnIndex(COLUMN_RUN_START_DATE));
			run.setStartDate(new Date(startDate));
			return run;
		}
	}
	
	public static class LocationCursor extends CursorWrapper {
		public LocationCursor(Cursor cursor) {
			super(cursor);
		}
		
		public Location getLocation() {
			if (isBeforeFirst() || isAfterLast())
				return null;
			// First get the provider out so you can use the constructor
			String provider = getString(getColumnIndex(COLUMN_LOCATION_PROVIDER));
			Location location = new Location(provider);
			// Populate the remaining properties
			location.setLatitude(getDouble(getColumnIndex(COLUMN_LOCATION_LATITUDE)));
			location.setLongitude(getDouble(getColumnIndex(COLUMN_LOCATION_LONGITUDE)));
			location.setAltitude(getDouble(getColumnIndex(COLUMN_LOCATION_ALTITUDE)));
			location.setTime(getLong(getColumnIndex(COLUMN_LOCATION_TIMESTAMP)));
			return location;
		}
	}
}
