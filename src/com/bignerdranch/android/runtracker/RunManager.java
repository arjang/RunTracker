package com.bignerdranch.android.runtracker;
/*
 * Singleton to manage the communication with LocationManager
 */
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class RunManager {
	private static final String TAG = "RunManager";
	public static final String ACTION_LOCATION = "com.bignerdranch.android.runtracker.ACTION_LOCATION";
	private static final String TEST_PROVIDER = "TEST_PROVIDER";
	
	private static RunManager sRunManager;
	private Context mAppContext;				//Context of the current state of the app
	private LocationManager mLocationManager;	//Provide access to the system location services
	
	//The private constructor forces users to use RunManager.get(Context)
	private RunManager(Context appContext) {
		mAppContext = appContext;
		mLocationManager = (LocationManager) mAppContext.getSystemService(Context.LOCATION_SERVICE);	//This is how you retrieve LocationManager
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
		return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
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
}
