/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.tagjam.pairmycare.model.Measurement;
import com.tagjam.pairmycare.model.Message;
import com.tagjam.pairmycare.model.Person;

public class CareTakerActivity extends FragmentActivity {
	private static final String TAG = "CareTakerActivity";

	private PairMyCareApp app;

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_caretaker);

		app = (PairMyCareApp) getApplication();

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		if (app.getOwnerName() == null) {
			new SettingsFragment().show(getSupportFragmentManager(), null);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getIntent().hasExtra("message")) {
			Message message = Utils.fromJson(getIntent().getStringExtra("message"), Message.class);
			if (message.plan != null) {
				app.addPlan(message.plan);
			} else if (message.measurement != null) {
				app.storeMessage(message);
				if (!app.hasPerson(message.from)) {
					app.addPerson(message.from);
					mSectionsPagerAdapter.notifyDataSetChanged();
				}
				mViewPager.setCurrentItem(getCareGivers().indexOf(message.from) + 1);
			}
			getIntent().removeExtra("message");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.caretaker, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_caregiver_mode:
			startActivity(new Intent(this, CareGiverActivity.class));
			break;
		case R.id.action_settings:
			new SettingsFragment().show(getSupportFragmentManager(), null);
			break;
		}
		return true;
	}

	public void addCareGiver(Person person) {
		if (!app.hasPerson(person) && person != null && person.name != null && person.id != null
				&& person.type == Person.Type.CAREGIVER) {
			app.addPerson(person);
			mSectionsPagerAdapter.notifyDataSetChanged();

			// Send an acknowledgement message to paired caregiver.
			Message message = new Message();
			message.measurement = new Measurement();
			message.measurement.timestamp = System.currentTimeMillis();
			message.measurement.title = String.format("Hello %s, I have added you as a caregiver.",
					person.name);
			app.postMessage(Person.Type.CARETAKER, message, person);
		}
	}

	public void removeCareGiver(Person person) {
		app.removePerson(person);
		mSectionsPagerAdapter.notifyDataSetChanged();
	}

	public List<Person> getCareGivers() {
		return app.getPersons(Person.Type.CAREGIVER);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				return new CareTakerHomeFragment();
			}
			List<Person> caregivers = getCareGivers();
			Fragment fragment = new CareTakerSectionFragment();
			Bundle args = new Bundle();
			if (position - 1 < caregivers.size()) {
				args.putString("person", new Gson().toJson(caregivers.get(position - 1)));
			}
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return getCareGivers().size() + 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if (position == 0) {
				return getString(R.string.home).toUpperCase(Locale.getDefault());
			}
			List<Person> caregivers = getCareGivers();
			if (position - 1 < caregivers.size()) {
				String name = caregivers.get(position - 1).name;
				if (name == null) {
					return "UNKNOWN";
				}
				return name.toUpperCase(Locale.getDefault());
			}
			return null;
		}
	}
}
