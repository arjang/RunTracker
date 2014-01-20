package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.database.Cursor;

/*
 * A Loader to query the database for the run's location and to keep
 * the query off the UI thread
 */
public class LocationListCursorLoader extends SQLiteCursorLoader {
	private long mRunId;
	
	public LocationListCursorLoader(Context context, long runId) {
		super(context);
		mRunId = runId;
	}
	
	@Override
	protected Cursor loadCursor() {
		return RunManager.get(getContext()).queryLocationsForRun(mRunId);
	}

}
