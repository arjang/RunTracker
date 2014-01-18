package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class DataLoader<D> extends AsyncTaskLoader<D> {
	private D mData;
	
	public DataLoader(Context context) {
		super(context);
	}
	
	@Override
	protected void onStartLoading() {
		if (mData != null) {
			deliverResult(mData);
		} else {
			forceLoad();	//Calls superclass's forceLoad() method to go fetch the data
		}
	}
	
	/*
	 * Stash away the new data object and, if the loader is started, call the
	 * superclass implementation to make the delivery
	 */
	@Override
	public void deliverResult(D data) {
		mData = data;
		if (isStarted()) {
			super.deliverResult(data);
		}
	}
}
