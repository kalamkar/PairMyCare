/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// TODO(abhi): Move this in a service for triggering reliably.
public class CalendarEventReceiver extends BroadcastReceiver {
	private static final String TAG = "CalendarEventReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, intent.getDataString());
	}

}
