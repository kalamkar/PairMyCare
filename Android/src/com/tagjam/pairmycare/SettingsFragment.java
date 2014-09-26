/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class SettingsFragment extends DialogFragment implements OnClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().setTitle(R.string.settings);
		View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
		rootView.findViewById(R.id.save).setOnClickListener(this);
		String ownerName = ((PairMyCareApp) getActivity().getApplication()).getOwnerName();
		if (ownerName != null) {
			((TextView) rootView.findViewById(R.id.personName)).setText(ownerName);
		}
		((CheckBox) rootView.findViewById(R.id.tts)).setChecked(((PairMyCareApp) getActivity()
				.getApplication()).isTtsEnabled());
		return rootView;
	}

	@Override
	public void onClick(View v) {
		((PairMyCareApp) getActivity().getApplication()).setOwnerName(((TextView) getView()
				.findViewById(R.id.personName)).getText().toString());
		((PairMyCareApp) getActivity().getApplication()).setTtsEnabled(((CheckBox) getView()
				.findViewById(R.id.tts)).isChecked());
		dismiss();
	}

	@Override
	public void onDestroy() {
		String ownerName = ((PairMyCareApp) getActivity().getApplication()).getOwnerName();
		if (ownerName == null) {
			ownerName = getActivity().getResources().getString(android.R.string.unknownName);
		}
		((PairMyCareApp) getActivity().getApplication()).setOwnerName(ownerName);
		super.onDestroy();
	}
}
