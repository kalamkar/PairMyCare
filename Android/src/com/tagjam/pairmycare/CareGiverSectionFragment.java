/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tagjam.pairmycare.model.Measurement;
import com.tagjam.pairmycare.model.Message;
import com.tagjam.pairmycare.model.Person;
import com.tagjam.pairmycare.model.PlanItem;

public class CareGiverSectionFragment extends Fragment implements OnClickListener,
		OnLongClickListener {

	public static final int PLANNER_ACTIVITY_REQUEST = 1;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"EEE, d MMM yyyy hh:mm a");

	private Person caretaker;
	private List<Message> messages;
	private MessageListAdapter messagesAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		caretaker = new Gson().fromJson(getArguments().getString("person"), Person.class);
		View rootView = inflater.inflate(R.layout.fragment_caregiver_section, container, false);
		rootView.findViewById(R.id.send).setOnClickListener(this);
		rootView.findViewById(R.id.addPlan).setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onResume() {
		reloadMessages();
		super.onResume();
	}

	private void reloadMessages() {
		messages = ((PairMyCareApp) getActivity().getApplication()).getMessages(caretaker);
		messagesAdapter = new MessageListAdapter();
		((ListView) getView().findViewById(R.id.messageList)).setAdapter(messagesAdapter);
	}

	@Override
	public boolean onLongClick(View v) {
		v.findViewById(R.id.deleteMsg).setVisibility(View.VISIBLE);
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.send) {
			Message message = new Message();
			message.measurement = new Measurement();
			message.measurement.timestamp = System.currentTimeMillis();
			message.measurement.title = ((TextView) getView().findViewById(R.id.messageTitle))
					.getText().toString();
			((PairMyCareApp) getActivity().getApplication()).postMessage(Person.Type.CAREGIVER,
					message, caretaker);
			((TextView) getView().findViewById(R.id.messageTitle)).setText("");
		} else if (v.getId() == R.id.addPlan) {
			startActivityForResult(new Intent(getActivity(), PlannerActivity.class),
					PLANNER_ACTIVITY_REQUEST);
		} else if (v.getId() == R.id.deleteMsg) {
			((PairMyCareApp) getActivity().getApplication()).removeMessage((Message) v.getTag());
			reloadMessages();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK || data == null) {
			return;
		}
		switch (requestCode) {
		case PLANNER_ACTIVITY_REQUEST:
			if (data.hasExtra("plan")) {
				Message message = new Message();
				message.plan = Utils.fromJson(data.getStringExtra("plan"), PlanItem.class);
				((PairMyCareApp) getActivity().getApplication()).postMessage(Person.Type.CAREGIVER,
						message, caretaker);
			}
			break;
		}
	}

	public class MessageListAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return messages.size();
		}

		@Override
		public Message getItem(int position) {
			return messages.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Message message = getItem(position);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View vi = inflater.inflate(R.layout.message_list_item, parent, false);
			((TextView) vi.findViewById(R.id.title)).setText(message.measurement.title);

			if (message.measurement.exception) {
				vi.findViewById(R.id.exception).setVisibility(View.VISIBLE);
				vi.findViewById(R.id.tip).setBackgroundColor(
						getResources().getColor(android.R.color.holo_orange_light));
			}

			if (message.measurement.plan != null && message.measurement.plan.expectedValue != 0
					&& message.measurement.plan.expectedValue != message.measurement.value) {
				((TextView) vi.findViewById(R.id.tip)).setText(String.format(
						"Expected %d Actual %d", message.measurement.plan.expectedValue,
						message.measurement.value));
			} else if (message.measurement.timestamp != 0) {
				((TextView) vi.findViewById(R.id.tip)).setText(DATE_FORMAT.format(new Date(
						message.measurement.timestamp)));
			}

			vi.findViewById(R.id.deleteMsg).setOnClickListener(CareGiverSectionFragment.this);
			vi.findViewById(R.id.deleteMsg).setTag(message);
			vi.setOnLongClickListener(CareGiverSectionFragment.this);

			return vi;
		}
	}
}
