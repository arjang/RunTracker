package com.bignerdranch.android.runtracker;

import java.util.Date;

import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.android.runtracker.RunDatebaseHelper.LocationCursor;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class RunMapFragment extends SupportMapFragment implements LoaderCallbacks<Cursor> {
	private static final String ARG_RUN_ID = "RUN_ID";
	private static final int LOAD_LOCATION = 0;
	
	private GoogleMap mGoogleMap;
	private LocationCursor mLocationCursor;
	
	public static RunMapFragment newInstance(long runId) {
		Bundle args = new Bundle();
		args.putLong(ARG_RUN_ID, runId);
		RunMapFragment rf = new RunMapFragment();
		rf.setArguments(args);
		return rf;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Check for a Run ID as an argument, and find the run
		Bundle args = getArguments();
		if (args != null) {
			long runId = args.getLong(ARG_RUN_ID, -1);
			if (runId != -1) {
				LoaderManager lm = getLoaderManager();
				lm.initLoader(LOAD_LOCATION, args, this);		//Returns the Loader that's created
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, parent, savedInstanceState);	
		
		//Stash a reference to the GoogleMap
		mGoogleMap = getMap();
		// Show the user's location
		mGoogleMap.setMyLocationEnabled(true);
		
		return view;
	}
	
	private void updateUI() {
		if (mGoogleMap == null || mLocationCursor == null) return;
		
		//Set up an overlay on the map for this run's locations
		//Create a polyline with all of the points
		PolylineOptions line = new PolylineOptions();
		//Also create a LatLngBoundsso you can zoom to fit
		LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
		//Iterate over the locations
		mLocationCursor.moveToFirst();
		int num = 1;
		while (!mLocationCursor.isAfterLast()) {
			Location loc = mLocationCursor.getLocation();
			LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
			
			Resources r = getResources();
			
			//If this is the first location, add a marker for it
			if (mLocationCursor.isFirst()) {
				String startDate = new Date(loc.getTime()).toString();
				MarkerOptions startMarkerOptions = new MarkerOptions()
						.position(latLng)
						.title(r.getString(R.string.run_start))
						.snippet(r.getString(R.string.run_started_at_format, startDate));
				mGoogleMap.addMarker(startMarkerOptions);
			} else if (mLocationCursor.isLast()) {
				// If this is the last location, and also not the first, add a marker
				String endDate = new Date(loc.getTime()).toString();
				MarkerOptions endMarkerOptions = new MarkerOptions()
						.position(latLng)
						.title(r.getString(R.string.run_finish))
						.snippet(r.getString(R.string.run_finished_at_format, endDate));
				mGoogleMap.addMarker(endMarkerOptions);
			}
			line.add(latLng);
			latLngBuilder.include(latLng);
			mLocationCursor.moveToNext();
			Log.i("RunMapFragment", num++ + "");
			
		}
		//Add the polyline to the map
		mGoogleMap.addPolyline(line);
		//Make the map zoom to show the track, with some padding
		//Use the size of the current display in pixels as a bounding box
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		//Construct a movement instruction for the map camera
		LatLngBounds latLngBounds = latLngBuilder.build();
		CameraUpdate movement = CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(),
								display.getWidth(), display.getHeight(), 15);
//		mGoogleMap.moveCamera(movement);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		long runId = args.getLong(ARG_RUN_ID, -1);
		return new LocationListCursorLoader(getActivity(), runId);
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mLocationCursor = (LocationCursor) cursor;
		updateUI();
	}
	
	//This method is called when the LoaderManager is shutting the loader down
	//after the user navigates away from the fragment
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Stop using the data
		mLocationCursor.close();
		mLocationCursor = null;
	}
}
