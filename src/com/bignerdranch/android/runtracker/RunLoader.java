package com.bignerdranch.android.runtracker;

import android.content.Context;

public class RunLoader extends DataLoader<Run> {
	private long mRunId;
	
	public RunLoader(Context context, long runId) {
		super(context);
		mRunId = runId;
	}
	
	@Override
	public Run loadInBackground() {
		return RunManager.get(getContext()).getRun(mRunId);		//Asks RunManager for a run with that ID and returns it
	}
}
