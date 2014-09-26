/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * A dummy fragment representing a section of the app, but that simply displays
 * dummy text.
 */
public class QrCodeFragment extends DialogFragment {
	private final static String CHART_API_URL = "http://chart.apis.google.com/chart?cht=qr&chs=300x300&chld=H|0&chl=%s";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().setTitle(R.string.show_to_care_taker_to_add);
		View rootView = inflater.inflate(R.layout.fragment_qrcode, container, false);
		String data = getArguments().getString("data");
		((WebView) rootView.findViewById(R.id.qrcode)).loadUrl(String.format(CHART_API_URL, data));
		return rootView;
	}
}
