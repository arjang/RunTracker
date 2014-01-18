package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

public abstract class SQLiteCursorLoader extends AsyncTaskLoader<Cursor> {
	private Cursor mCursor;
	
	public SQLiteCursorLoader(Context context) {
		super(context);
	}
	
	protected abstract Cursor loadCursor();
	
	/*
	 * Calls the abstract loadCursor() to get the cursor and calls the getCount()
	 * method on the cursor to ensure that the data is avail in memory once it's passed to 
	 * the main thread
	 */
	@Override
	public Cursor loadInBackground() {
		Cursor cursor = loadCursor();
		if (cursor != null) {
			// Ensure that the content window is filled
			cursor.getCount();
		}
		return cursor;
	}
	
	@Override
	public void deliverResult(Cursor data) {
		Cursor oldCursor = mCursor;
		mCursor = data;
		
		if (isStarted()) {					//If true, means the data can be delivered
			super.deliverResult(data);
		}
		
		if (oldCursor != null && oldCursor != data && !oldCursor.isClosed()) {
			oldCursor.close();				//old cursor is closed to free up memory
		}
	}
	
	/*
	 * Load the data
	 */
	@Override
	protected void onStartLoading() {
		if (mCursor != null) {
			deliverResult(mCursor);			//Sends the result of the load to the registered listener
		}	
		
		if (takeContentChanged() || mCursor == null) {
			forceLoad();					//Force an asynchronous load.
		}
	}
	
	/*
	 * Stop their loader
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible
		cancelLoad();						//Attempt to cancel the current load task.
	}
	
	/*
	 * Handles a request to cancel a load
	 */
	@Override
	public void onCanceled(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}
	
	/*
	 * Resetting the loader
	 */
	@Override
	public void onReset() {
		super.onReset();
		
		// Ensure the loader is stopped
		onStopLoading();
		
		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}
		
		mCursor = null;
	}
}
