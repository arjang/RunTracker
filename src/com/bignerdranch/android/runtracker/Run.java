package com.bignerdranch.android.runtracker;

import java.util.Date;

/*
 * Record detail of a single run
 */
public class Run {
	private Date mStartDate;
	
	public Run() {
		mStartDate = new Date();
	}
	
	public Date getStartDate() {
		return mStartDate;
	}
	
	public void setStartDate(Date startDate) {
		mStartDate = startDate;
	}
	
	public int getDurationSeconds(long endMillis) {
		return (int) ((endMillis - mStartDate.getTime()) / 1000);
	}
	
	public static String formatDuration(int durationSeconds) {
		int seconds = durationSeconds % 60;
		int minutes = ((durationSeconds - seconds) / 60) % 60;
		int hours = (durationSeconds - (minutes * 60) - seconds) / 3600;
		//Output in integer values of 2 digit, with the first being zero if number < 10
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);		
	}
	
	
}
