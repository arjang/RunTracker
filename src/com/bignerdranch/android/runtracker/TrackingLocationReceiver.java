package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.location.Location;
import android.util.Log;
/*
 * A standalone BroadcastReceiver to guarantee that your location intents will be handled no matter whether the rest
 * of RunTracker is up and running
 */
public class TrackingLocationReceiver extends LocationReceiver {
	@Override
	protected void onLocationReceived(Context context, Location location) {
		Log.i("TrackingLocationReceiver", "Receiving");
		RunManager.get(context).insertLocation(location);
	}
}
