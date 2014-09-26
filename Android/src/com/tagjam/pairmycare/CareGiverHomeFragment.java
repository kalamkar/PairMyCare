/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tagjam.pairmycare.model.Message;
import com.tagjam.pairmycare.model.Person;

/**
 * A dummy fragment representing a section of the app, but that simply displays
 * dummy text.
 */
public class CareGiverHomeFragment extends Fragment implements OnClickListener {
	private static final String TAG = "CareGiverHomeFragment";

	private List<Person> caretakers;
	private CareTakersAdapter careTakersAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_caregiver_home, container, false);
		rootView.findViewById(R.id.addCareTaker).setOnClickListener(this);
		((TextView) rootView.findViewById(R.id.numCareTakers)).setText(Integer
				.toString(((PairMyCareApp) getActivity().getApplication()).getPersons(
						Person.Type.CARETAKER).size()));
		return rootView;
	}

	@Override
	public void onResume() {
		caretakers = ((PairMyCareApp) getActivity().getApplication())
				.getPersons(Person.Type.CARETAKER);
		careTakersAdapter = new CareTakersAdapter();
		((ListView) getView().findViewById(R.id.caretakersList)).setAdapter(careTakersAdapter);
		((TextView) getView().findViewById(R.id.grade))
				.setText(getGrade(((PairMyCareApp) getActivity().getApplication())
						.getMessages(null)));
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.addCareTaker) {
			Person me = ((PairMyCareApp) getActivity().getApplication()).getOwner();
			me.type = Person.Type.CAREGIVER;
			Bundle args = new Bundle();
			args.putString("data", new Gson().toJson(me));
			QrCodeFragment qrCode = new QrCodeFragment();
			qrCode.setArguments(args);
			qrCode.show(getFragmentManager(), null);
		}
	}

	private String getGrade(List<Message> messages) {
		int numMessages = 0;
		int numExceptions = 0;
		for (Message message : messages) {
			numMessages++;
			if (message.measurement.exception) {
				numExceptions++;
			}
		}
		if (numMessages == 0) {
			return "?";
		}
		int percent = numExceptions * 100 / numMessages;
		if (percent < 15) {
			return "A+";
		} else if (percent < 30) {
			return "A";
		} else if (percent < 45) {
			return "B+";
		} else if (percent < 60) {
			return "B";
		} else if (percent < 75) {
			return "C+";
		}
		return "C";
	}

	private Pair<String, String> getHighLow(List<Message> messages) {
		String low = "";
		String high = "";
		int lowestPercent = 100;
		int highestPercent = 0;
		Map<String, Pair<Integer, Integer>> map = new HashMap<String, Pair<Integer, Integer>>();

		for (Message message : messages) {
			if (message.measurement.plan == null) {
				continue;
			}
			Pair<Integer, Integer> planStats = map.get(message.measurement.plan.title);
			if (planStats == null) {
				planStats = Pair.create(0, 1);
			}
			if (message.measurement.exception) {
				planStats = Pair.create(planStats.first + 1, planStats.second);
			}
			int percent = planStats.first * 100 / planStats.second;
			if (percent < lowestPercent) {
				lowestPercent = percent;
				low = message.measurement.plan.title;
			}
			if (percent > highestPercent) {
				highestPercent = percent;
				high = message.measurement.plan.title;
			}
			map.put(message.measurement.plan.title, planStats);
		}
		for (String key : map.keySet()) {
			Pair<Integer, Integer> pair = map.get(key);
			Log.i(TAG, String.format("%s: %d,%d", key, pair.first, pair.second));
		}
		return Pair.create(low, high);
	}

	public class CareTakersAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return caretakers.size();
		}

		@Override
		public Person getItem(int position) {
			return caretakers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Person caretaker = getItem(position);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View vi = inflater.inflate(R.layout.caretaker_item, parent, false);

			((TextView) vi.findViewById(R.id.name)).setText(caretaker.name);
			((TextView) vi.findViewById(R.id.grade))
					.setText(getGrade(((PairMyCareApp) getActivity().getApplication())
							.getMessages(caretaker)));

			Pair<String, String> highLow = getHighLow(((PairMyCareApp) getActivity()
					.getApplication()).getMessages(null));
			((TextView) vi.findViewById(R.id.high)).setText(String.format("Good at: %s",
					highLow.first));
			((TextView) vi.findViewById(R.id.low)).setText(String.format("Struggling to: %s",
					highLow.second));

			return vi;
		}
	}
}
