package com.bignerdranch.android.runtracker;
/*
 * Singleton to manage the communication with LocationManager
 */
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.bignerdranch.android.runtracker.RunDatebaseHelper.LocationCursor;
import com.bignerdranch.android.runtracker.RunDatebaseHelper.RunCursor;

public class RunManager {
	private static final String TAG = "RunManager";
	public static final String ACTION_LOCATION = "com.bignerdranch.android.runtracker.ACTION_LOCATION";
	private static final String TEST_PROVIDER = "TEST_PROVIDER";
	private static final String PREFS_FILE = "runs";
	private static final String PREFS_CURRENT_RUN_ID = "RunManager.currentRunId";
	
	private static RunManager sRunManager;
	private Context mAppContext;				//Context of the current state of the app
	private LocationManager mLocationManager;	//Provide access to the system location services
	private RunDatebaseHelper mHelper;
	private SharedPreferences mPrefs;
	private long mCurrentRunId;
	
	//The private constructor forces users to use RunManager.get(Context)
	private RunManager(Context appContext) {
		mAppContext = appContext;
		mLocationManager = (LocationManager) mAppContext.getSystemService(Context.LOCATION_SERVICE);	//This is how you retrieve LocationManager
		mHelper = new RunDatebaseHelper(mAppContext);
		mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
		mCurrentRunId = mPrefs.getLong(PREFS_CURRENT_RUN_ID, -1);
	}
	
	public static RunManager get(Context context) {
		if (sRunManager == null) {
			// Use the application context to avoid leaking activities
			sRunManager = new RunManager(context.getApplicationContext());
		}
		return sRunManager;
	}
	
	/*
	 * Create pendingIntent for locationManager to be broadcasted when location update happens
	 */
	private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
		Intent broadcast = new Intent(ACTION_LOCATION);
		
		//Flag indicating that if the described PendingIntent already exists, then simply return null instead of creating it
		int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;		
		return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);	//Retrieve a PendingIntent that will perform a broadcast
	}
	
	/*
	 * start tracking
	 */
	public void startLocationUpdates() {
		String provider = LocationManager.GPS_PROVIDER;
		
		//If you have the test provider and it's enabled, use it
		if (mLocationManager.getProvider(TEST_PROVIDER) != null && 
				mLocationManager.isProviderEnabled(TEST_PROVIDER))
			provider = TEST_PROVIDER;
		Log.i(TAG, "Using provider " + provider);
		
		//Get the last known location and broadcast it if you have one
		Location lastKnown = mLocationManager.getLastKnownLocation(provider);
		if (lastKnown != null) {
			//Reset the time to now
			lastKnown.setTime(System.currentTimeMillis());					//Set timestamp of lastKnown
			broadcastLocation(lastKnown);
		}
		
		// Start updates from the location manager
		PendingIntent pi = getLocationPendingIntent(true);
		mLocationManager.requestLocationUpdates(provider, 0, 0, pi);		//Min time to wait (milliseconds) and min dist to cover (m) before sending the next update
	}
	
	/*
	 * Broadcast the given intent to all interested BroadcastReceivers
	 */
	private void broadcastLocation(Location location) {
		Intent broadcast = new Intent(ACTION_LOCATION);
		broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
		mAppContext.sendBroadcast(broadcast);	//Broadcast the given intent to all interested BroadcastReceivers
	}
	
	/*
	 * Stop tracking
	 */
	public void stopLocationUpdates() {
		PendingIntent pi = getLocationPendingIntent(false);
		if (pi != null) {
			mLocationManager.removeUpdates(pi);
			pi.cancel();
		}
	}
	
	/*
	 * Check if it's currently tracking any run
	 */
	public boolean isTrackingRun() {
		return getLocationPendingIntent(false) != null;
	}
	
	public boolean isTrackingRun(Run run) {
		return run != null && run.getId() == mCurrentRunId;
	}
	
	/*
	 * Saves a new run in SQLite db, start tracking the run and return the instance of the run
	 */
	public Run startNewRun() {
		// Insert a new run into the db
		Run run = insertRun();
		// Start tracking the run
		startTrackingRun(run);
		return run;
	}
	
	public void startTrackingRun(Run run) {
		// Keep the ID
		mCurrentRunId = run.getId();
		// Store it in sharedPreferences
		mPrefs.edit().putLong(PREFS_CURRENT_RUN_ID, mCurrentRunId).commit();
		// Start location update
		startLocationUpdates();
	}
	
	public void stopRun() {
		stopLocationUpdates();
		mCurrentRunId = -1;
		mPrefs.edit().remove(PREFS_CURRENT_RUN_ID).commit();		//Modifications to Prefs must be down thru the editor
	}
	
	private Run insertRun() {
		Run run = new Run();
		run.setId(mHelper.insertRun(run));			//Run id is row id in SQLite table
		return run;
	}
	
	/*
	 * Does the work of executing SQL query and providing the plain cursor
	 * to a new RunCursor
	 */
	public RunCursor queryRuns() {
		return mHelper.queryRuns();
	}
	
	/*
	 * Insert a location for the currently tracking run
	 */
	public void insertLocation(Location loc) {
		if (mCurrentRunId != -1) {
			mHelper.insertLocation(mCurrentRunId, loc);
		} else {
			Log.e(TAG, "location received with no tracking run" + loc.toString());
		}
	}
	
	public Run getRun(long id) {
		Run run = null;
		RunCursor cursor = mHelper.queryRuns(id);
		cursor.moveToFirst();
		// If you got a row, get a run
		if (!cursor.isAfterLast())
			run = cursor.getRun();	
		cursor.close();					//Caller of this method has no access to RunCursor, so must close() before returning
		return run;
	}
	
	public Location getLastKnownLocationForRun(long runId) {
		Location location = null;
		LocationCursor cursor = mHelper.queryLastLocationForRun(runId);
		cursor.moveToFirst();
		// If you got a row, get a location
		if (!cursor.isAfterLast())
			location = cursor.getLocation();
		cursor.close();
		return location;
	}
}
