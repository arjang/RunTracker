package com.bignerdranch.android.runtracker;

import android.content.Context;

public class RunLoader extends DataLoader<Run> {
	private long mRunId;
	
	public RunLoader(Context context, long runId) {
		super(context);
		mRunId = runId;
	}
	
	//This function is called in a background thread and should generate a new
	//set of data to be published by the Loader
	@Override
	public Run loadInBackground() {
		return RunManager.get(getContext()).getRun(mRunId);		//Asks RunManager for a run with that ID and returns it
	}
	
	public boolean isTrackingRun(Run run) {
		return RunManager.get(getContext()).isTrackingRun(run);
	}
}
