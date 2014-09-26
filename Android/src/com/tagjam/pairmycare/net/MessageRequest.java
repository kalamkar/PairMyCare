/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare.net;

import java.io.ByteArrayOutputStream;

import android.util.Log;

public class MessageRequest extends BaseHttpRequest {
	private static final String TAG = "MessageRequest";

	public MessageRequest(String url) {
		super(url);
	}

	@Override
	protected void onPostExecute(ByteArrayOutputStream result) {
		Log.i(TAG, "Received response " + result.toString());
		super.onPostExecute(result);
	}
}
