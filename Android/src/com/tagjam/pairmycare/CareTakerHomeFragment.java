/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import java.util.List;

import net.sourceforge.zbar.Symbol;
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

import com.tagjam.pairmycare.model.Person;
import com.tagjam.pairmycare.model.PlanItem;

/**
 * A dummy fragment representing a section of the app, but that simply displays
 * dummy text.
 */
public class CareTakerHomeFragment extends Fragment implements OnClickListener, OnLongClickListener {
	public static final String TAG = "CareTakerHomeFragment";

	public static final int SCANNER_ACTIVITY_REQUEST = 1;
	public static final int PLANNER_ACTIVITY_REQUEST = 2;

	private List<PlanItem> plan;
	private PlanAdapter planAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_caretaker_home, container, false);
		rootView.findViewById(R.id.addCareGiver).setOnClickListener(this);
		rootView.findViewById(R.id.addEvent).setOnClickListener(this);
		((TextView) rootView.findViewById(R.id.numCareGivers)).setText(Integer
				.toString(((PairMyCareApp) getActivity().getApplication()).getPersons(
						Person.Type.CAREGIVER).size()));
		return rootView;
	}

	@Override
	public void onResume() {
		plan = ((PairMyCareApp) getActivity().getApplication()).getPlan();
		((TextView) getView().findViewById(R.id.numPlans)).setText(Integer.toString(plan.size()));
		planAdapter = new PlanAdapter();
		((ListView) getView().findViewById(R.id.plan)).setAdapter(planAdapter);
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.addCareGiver) {
			startActivityForResult(new Intent(getActivity(), ScannerActivity.class).putExtra(
					"type", Symbol.QRCODE), SCANNER_ACTIVITY_REQUEST);
		} else if (v.getId() == R.id.addEvent) {
			startActivityForResult(new Intent(getActivity(), PlannerActivity.class),
					PLANNER_ACTIVITY_REQUEST);
		} else if (v.getId() == R.id.delete) {
			((PairMyCareApp) getActivity().getApplication()).removePlan((PlanItem) v.getTag());
			planAdapter.notifyDataSetChanged();
		} else if (v.getTag() != null) {
			PlanItem plan = (PlanItem) v.getTag();
			startActivity(new Intent(getActivity(), ExecutePlanActivity.class).putExtra("plan",
					Utils.toJson(plan)));
		}
	}

	@Override
	public boolean onLongClick(View v) {
		v.findViewById(R.id.delete).setVisibility(View.VISIBLE);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK || data == null) {
			return;
		}
		switch (requestCode) {
		case SCANNER_ACTIVITY_REQUEST:
			if (data.hasExtra("type") && data.hasExtra("data")
					&& data.getIntExtra("type", 0) == Symbol.QRCODE) {
				((CareTakerActivity) getActivity()).addCareGiver(Utils.fromJson(
						data.getStringExtra("data"), Person.class));
			}
			break;
		case PLANNER_ACTIVITY_REQUEST:
			if (data.hasExtra("plan")) {
				((PairMyCareApp) getActivity().getApplication()).addPlan(Utils.fromJson(
						data.getStringExtra("plan"), PlanItem.class));
				planAdapter.notifyDataSetChanged();
			}
			break;
		}
	}

	public class PlanAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return plan.size();
		}

		@Override
		public PlanItem getItem(int position) {
			return plan.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PlanItem plan = getItem(position);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View vi = inflater.inflate(R.layout.plan_item, parent, false);
			((TextView) vi.findViewById(R.id.title)).setText(plan.title);
			((TextView) vi.findViewById(R.id.time)).setText(String.format("%d:%02d %s",
					plan.timeHour > 11 ? plan.timeHour - 12 : plan.timeHour, plan.timeMinute,
					plan.timeHour > 11 ? "PM" : "AM"));
			String tip = "";
			if (plan.repeatDays != null) {
				tip = plan.repeatDays.toString();
			} else if (plan.expectedValue != 0) {
				tip = String.format("Expected %d", plan.expectedValue);
			}
			((TextView) vi.findViewById(R.id.tip)).setText(tip);
			vi.findViewById(R.id.delete).setOnClickListener(CareTakerHomeFragment.this);
			vi.findViewById(R.id.delete).setTag(plan);
			vi.setOnLongClickListener(CareTakerHomeFragment.this);

			vi.setOnClickListener(CareTakerHomeFragment.this);
			vi.setTag(plan);

			return vi;
		}
	}
}
