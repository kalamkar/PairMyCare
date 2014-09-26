/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Utils {
	private static final String TAG = "Utils";

	public static final Gson gson = new Gson();

	public static <T> T fromJson(String json, Class<T> classOfT) {
		try {
			return gson.fromJson(json, classOfT);
		} catch (JsonSyntaxException ex) {
			Log.w(TAG, String.format("Unable to parse %s", json));
			return null;
		}
	}

	public static <T> String toJson(T t) {
		return gson.toJson(t);
	}
}
