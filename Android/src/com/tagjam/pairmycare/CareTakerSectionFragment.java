/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import java.util.List;

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

public class CareTakerSectionFragment extends Fragment implements OnClickListener,
		OnLongClickListener {

	private Person caregiver;
	private List<Message> messages;
	private MessageListAdapter messagesAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		caregiver = new Gson().fromJson(getArguments().getString("person"), Person.class);
		View rootView = inflater.inflate(R.layout.fragment_caretaker_section, container, false);
		rootView.findViewById(R.id.send).setOnClickListener(this);
		rootView.findViewById(R.id.delete).setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onResume() {
		reloadMessages();
		super.onResume();
	}

	private void reloadMessages() {
		messages = ((PairMyCareApp) getActivity().getApplication()).getMessages(caregiver);
		messagesAdapter = new MessageListAdapter();
		((ListView) getView().findViewById(R.id.messageList)).setAdapter(messagesAdapter);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.send) {
			Message message = new Message();
			message.measurement = new Measurement();
			message.measurement.timestamp = System.currentTimeMillis();
			message.measurement.title = ((TextView) getView().findViewById(R.id.messageTitle))
					.getText().toString();
			((PairMyCareApp) getActivity().getApplication()).postMessage(Person.Type.CARETAKER,
					message, caregiver);
			((TextView) getView().findViewById(R.id.messageTitle)).setText("");
		} else if (v.getId() == R.id.delete) {
			((CareTakerActivity) getActivity()).removeCareGiver(caregiver);
		} else if (v.getId() == R.id.deleteMsg) {
			((PairMyCareApp) getActivity().getApplication()).removeMessage((Message) v.getTag());
			reloadMessages();
		}
	}

	@Override
	public boolean onLongClick(View v) {
		v.findViewById(R.id.deleteMsg).setVisibility(View.VISIBLE);
		return true;
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
			((TextView) vi.findViewById(R.id.tip)).setText("");

			vi.findViewById(R.id.deleteMsg).setOnClickListener(CareTakerSectionFragment.this);
			vi.findViewById(R.id.deleteMsg).setTag(message);
			vi.setOnLongClickListener(CareTakerSectionFragment.this);

			return vi;
		}
	}
}
