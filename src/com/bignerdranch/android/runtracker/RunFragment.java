package com.bignerdranch.android.runtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RunFragment extends Fragment {
	private static final String TAG = "RunFragment";
	private static final String ARG_RUN_ID = "RUN_ID";
	private static final int LOAD_RUN = 0;
	private static final int LOAD_LOCATION = 1;
	private RunManager mRunManager;
	private Button mStartButton, mStopButton, mMapButton;
	private TextView mStartedTextView, mLatitudeTextView, mLongitudeTextView,
	mAltitudeTextView, mDurationTextView;
	
	private Location mLastLocation;
	private Run mRun;
	
	private BroadcastReceiver mLocationReceiver = new LocationReceiver() {	//LocationReceiver
		@Override
		protected void onLocationReceived(Context context, Location loc) {
			if (!mRunManager.isTrackingRun()) 
				return;
			mLastLocation = loc;
			if (isVisible()) 							//True if the current fragment is visible to the user
				updateUI();
		}
		
		@Override
		protected void onProviderEnabledChanged(boolean enabled) {
			int toastText = enabled ? R.string.gps_enabled : R.string.gps_disabled;
			Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
		}
	};
	
	public static RunFragment newInstance(long runId) {
		Bundle args = new Bundle();
		args.putLong(ARG_RUN_ID, runId);
		RunFragment rf = new RunFragment();
		rf.setArguments(args);
		return rf;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		mRunManager = RunManager.get(getActivity());
		
		//Check for a run ID as an argument, and find the run
		Bundle args = getArguments();
		if (args != null) {
			long runId = args.getLong(ARG_RUN_ID);
			if (runId != -1) {
				LoaderManager lm = getLoaderManager();
				lm.initLoader(LOAD_RUN, args, new RunLoaderCallbacks());
				lm.initLoader(LOAD_LOCATION, args, new LocationLoaderCallbacks());
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_run, parent, false);
		
		mStartedTextView = (TextView) view.findViewById(R.id.run_startedTextView);
		mLatitudeTextView = (TextView) view.findViewById(R.id.run_latitudeTextView);
		mLongitudeTextView = (TextView) view.findViewById(R.id.run_longitudeTextView);
		mAltitudeTextView = (TextView) view.findViewById(R.id.run_altitudeTextView);
		mDurationTextView = (TextView) view.findViewById(R.id.run_durationTextView);
		
		mStartButton = (Button) view.findViewById(R.id.run_startButton);
		mStartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mRun == null) {
					mRun = mRunManager.startNewRun();
				} else {
					mRunManager.startTrackingRun(mRun);
				}
				updateUI();
			}
		});
		
		mStopButton = (Button) view.findViewById(R.id.run_stopButton);
		mStopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mRunManager.stopRun();
				updateUI();
			}
		});
		
		mMapButton = (Button) view.findViewById(R.id.run_mapButton);
		mMapButton.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), RunMapActivity.class);
				intent.putExtra(RunMapActivity.EXTRA_RUN_ID, mRun.getId());
				startActivity(intent);
			}
		});
		
		return view;
	}
	
	/*
	 * Enable the Start button when the track is not running
	 */
	private void updateUI() {
		boolean started = mRunManager.isTrackingRun();
		boolean trackingThisRun = mRunManager.isTrackingRun(mRun);
		
		if (mRun != null) {
			mStartedTextView.setText(mRun.getStartDate().toString());
		}
		
		int durationSecond = 0;
		//Display running stats on the UI
		if (mRun != null && mLastLocation != null) {
			durationSecond = mRun.getDurationSeconds(mLastLocation.getTime());
			mLatitudeTextView.setText(Double.toString(mLastLocation.getLatitude()));
			mLongitudeTextView.setText(Double.toString(mLastLocation.getLongitude()));
			mAltitudeTextView.setText(Double.toString(mLastLocation.getAltitude()));
			mMapButton.setEnabled(true);
		} else {
			mMapButton.setEnabled(false);
		}
		mDurationTextView.setText(Run.formatDuration(durationSecond));
		
		mStartButton.setEnabled(!started);
		mStopButton.setEnabled(started && trackingThisRun);
	}
	
	/*
	 * Called when the fragment is visible to the user
	 */
	@Override
	public void onStart() {
		super.onStart();
		getActivity().registerReceiver(mLocationReceiver, 		//Register a BroadcastReceiver to be run in the main activity thread
					new IntentFilter(RunManager.ACTION_LOCATION));
	}
	
	/*
	 * Called on the fragment is no longer started
	 */
	@Override
	public void onStop() {
		getActivity().unregisterReceiver(mLocationReceiver);			//Unregister the receiver
		super.onStop();
	}
	
	/*
	 * A subclass of LoaderCallbacks interface for a client to interact with the LoaderManager. 
	 * LoaderCallbacks implementation is associated with the loader, and will be called when the loader state changes.
	 */
	private class RunLoaderCallbacks implements LoaderCallbacks<Run> {
		
		@Override
		public Loader<Run> onCreateLoader(int id, Bundle args) {
			return new RunLoader(getActivity(), args.getLong(ARG_RUN_ID));	//new RunLoader pointing at the fragment's current activity
		}
		
		/*
		 *  If at the point of this call the caller is in its started state, and the requested loader already exists 
		 *  and has generated its data, then the system calls onLoadFinished() immediately (during initLoader())
		 */
		@Override
		public void onLoadFinished(Loader<Run> loader, Run run) {	//Called when a previously created loader has finished its load.
			mRun = run;
			updateUI();
		}
		
		// This is called when the last Cursor provided to onLoadFinished()
	    // above is about to be closed.  We need to make sure we are no
	    // longer using it.
		@Override
		public void onLoaderReset(Loader<Run> loader) {
			// Do nothing
		}
	}
	
	/*
	 * 
	 */
	private class LocationLoaderCallbacks implements LoaderCallbacks<Location> {
		
		@Override
		public Loader<Location> onCreateLoader(int id, Bundle args) {
			return new LastLocationLoader(getActivity(), args.getLong(ARG_RUN_ID));
		}
		
		@Override
		public void onLoadFinished(Loader<Location> loader, Location location) {
			mLastLocation = location;
			updateUI();
		}
		
		@Override
		public void onLoaderReset(Loader<Location> location) {
			// Do nothing
		}
	}
}
