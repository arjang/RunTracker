package com.bignerdranch.android.runtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RunFragment extends Fragment {
	private static final String TAG = "RunFragment";
	private RunManager mRunManager;
	private Button mStartButton, mStopButton;
	private TextView mStartedTextView, mLatitudeTextView, mLongitudeTextView,
	mAltitudeTextView, mDurationTextView;
	
	private Location mLastLocation;
	private Run mRun;
	
	private BroadcastReceiver mLocationReceiver = new LocationReceiver() {
		@Override
		protected void onLocationReceived(Context context, Location loc) {
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		mRunManager = RunManager.get(getActivity());
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
				mRunManager.startLocationUpdates();
				mRun = new Run();						//Start a new run
				updateUI();
			}
		});
		
		mStopButton = (Button) view.findViewById(R.id.run_stopButton);
		mStopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mRunManager.stopLocationUpdates();
				updateUI();
			}
		});
		
		return view;
	}
	
	/*
	 * Enable the Start button when the track is not running
	 */
	private void updateUI() {
		boolean started = mRunManager.isTrackingRun();
		
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
		}
		mDurationTextView.setText(Run.formatDuration(durationSecond));
		
		mStartButton.setEnabled(!started);
		mStopButton.setEnabled(started);
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
}
