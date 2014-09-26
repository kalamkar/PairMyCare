/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

public class BaseHttpRequest extends AsyncTask<Pair<String, String>, Void, ByteArrayOutputStream> {
	private static final String TAG = "BaseHttpRequest";

	protected final String url;

	public BaseHttpRequest(String url) {
		this.url = url;
	}

	@Override
	protected ByteArrayOutputStream doInBackground(Pair<String, String>... params) {
		try {
			HttpPost request = new HttpPost(url);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			for (Pair<String, String> param : params) {
				nameValuePairs.add(new BasicNameValuePair(param.first, param.second));
			}
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = new DefaultHttpClient().execute(request);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			response.getEntity().writeTo(output);
			output.close();

			return output;
		} catch (ClientProtocolException e) {
			Log.w(TAG, e);
		} catch (IOException e) {
			Log.w(TAG, e);
		}
		return null;
	}
}
