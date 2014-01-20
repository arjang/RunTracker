package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class DataLoader<D> extends AsyncTaskLoader<D> {
	private D mData;
	
	public DataLoader(Context context) {
		super(context);
	}
	
	//Handles a request to start the Loader
	@Override
	protected void onStartLoading() {
		
		if (mData != null) {
			// If we currently have a result available, deliver it
            // immediately.
			deliverResult(mData);
		} else {
			forceLoad();	//Calls superclass's forceLoad() method to go fetch the data
		}
	}
	
	/*
	 * Called when there is new data to deliver to the client
	 * Stash away the new data object and, if the loader is started, call the
	 * superclass implementation to make the delivery/
	 */
	@Override
	public void deliverResult(D data) {
		mData = data;
		if (isStarted()) {					//Return whether this load has been started.
			super.deliverResult(data);		//Sends the result of the load to the registered listener
		}
	}
}
