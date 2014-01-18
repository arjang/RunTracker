package com.bignerdranch.android.runtracker;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.bignerdranch.android.runtracker.RunDatebaseHelper.RunCursor;

/*
 * Now the Cursor is being loaded on the UI thread, it is not a good practice.
 */
public class RunListFragment extends ListFragment {
	private static final String TAG = "RunListFragment";
	private static final int REQUEST_NEW_RUN = 0;
	private static final int START_TRACKING = 1;
	private RunCursor mCursor;
	private RunManager mRunManager;
	private Drawable mDefaultColor;
	private boolean mIsDefaultColorSet;
	private View mViewOfListItem;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		// Query the list of runs
		mRunManager = RunManager.get(getActivity());
		mCursor = mRunManager.queryRuns();
		// Create an Adapter to point at this cursor
		RunCursorAdapter adapter = new RunCursorAdapter(getActivity(), mCursor);
		setListAdapter(adapter);
		mIsDefaultColorSet = false;
	}
	
	@Override
	public void onDestroy() {
		mCursor.close();
		super.onDestroy();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.run_list_options, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_new_run:
			Intent intent = new Intent(getActivity(), RunActivity.class);
			startActivityForResult(intent, REQUEST_NEW_RUN);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (REQUEST_NEW_RUN == requestCode) {
			mCursor.requery();
			((RunCursorAdapter) getListAdapter()).notifyDataSetChanged();	//Forces the list to reload once the user returns to it
		}
		else if (START_TRACKING == requestCode) {							//Changes background color if the run is being tracked
			Run run = mCursor.getRun();
			Resources res = getResources();
			PendingIntent pi = PendingIntent
					.getActivity(getActivity(), 0, new Intent(getActivity(), RunActivity.class), 0);
			NotificationManager notificationManager = 
					(NotificationManager) getActivity().getSystemService("notification");
			
			if (mRunManager.isTrackingRun(run)) {
				mViewOfListItem.setBackgroundColor(Color.GREEN);
				((RunCursorAdapter) getListAdapter()).notifyDataSetChanged();
				Notification notification = new NotificationCompat.Builder(getActivity())
					.setTicker(res.getString(R.string.tracking_run))
					.setSmallIcon(android.R.drawable.ic_menu_report_image)				//Configure small icon
					.setContentTitle(res.getString(R.string.tracking_run))			//Configure the appearance
					.setContentText(res.getString(R.string.tracking_run))
					.setContentIntent(pi)	//pi will be fired when user presses this notification in the drawer
					.build();
				
				notificationManager.notify(0, notification);
			} else {
				mViewOfListItem.setBackground(mDefaultColor);
				notificationManager.cancelAll();
			}	
		}
	}
	
	@Override
	public void onListItemClick(ListView listview, View view, int pos, long id) {
		// The id argument will be the Run ID; CursorAdapter gives us this for 
		Intent intent = new Intent(getActivity(), RunActivity.class);
		intent.putExtra(RunActivity.EXTRA_RUN_ID, id);
		startActivityForResult(intent, START_TRACKING);
		
		mViewOfListItem = view;
		if (!mIsDefaultColorSet) {
			mIsDefaultColorSet = true;
			mDefaultColor = view.getBackground();
		}
	}
	
	private static class RunCursorAdapter extends CursorAdapter {
		private RunCursor mRunCursor;
		
		public RunCursorAdapter(Context context, RunCursor cursor) {
			super(context, cursor, 0);	//Constructor of CursorAdapter 
			mRunCursor = cursor;
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// Use a layout inflater to get a row view
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
		}
		
		/*
		 * This method will be called by CursorAdapter when it wants to configure
		 * a view to hold data for a row in the cursor
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {	//The view is always from the prev newView()
			// Get the run for the current row
			Run run = mRunCursor.getRun();			//Cursor already positioned by CursorAdapter
			
			// Set up the start date text view
			TextView startDateTextView = (TextView) view;
			String cellText = 
					context.getString(R.string.cell_text, run.getStartDate());
			startDateTextView.setText(cellText);
		}
	}
}
