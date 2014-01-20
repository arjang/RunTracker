package com.bignerdranch.android.runtracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class RunMapFragment extends SupportMapFragment {
	private static final String ARG_RUN_ID = "RUN_ID";
	
	private GoogleMap mGoogleMap;
	
	public static RunMapFragment newInstance(long runId) {
		Bundle args = new Bundle();
		args.putLong(ARG_RUN_ID, runId);
		RunMapFragment rf = new RunMapFragment();
		rf.setArguments(args);
		return rf;
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
}
