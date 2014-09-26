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
import com.tagjam.pairmycare.model.Message;
import com.tagjam.pairmycare.model.Person;

public class CareGiverActivity extends FragmentActivity {
	private static final String TAG = "CareGiverActivity";

	private PairMyCareApp app;

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_caregiver);

		app = (PairMyCareApp) getApplication();
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getIntent().hasExtra("message")) {
			Message message = Utils.fromJson(getIntent().getStringExtra("message"), Message.class);
			app.storeMessage(message);
			if (!app.hasPerson(message.from)) {
				app.addPerson(message.from);
				mSectionsPagerAdapter.notifyDataSetChanged();
			}
			if (message.measurement.plan == null) {
				mViewPager.setCurrentItem(getCareTakers().indexOf(message.from) + 1);
			}
			getIntent().removeExtra("message");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.caregiver, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_caretaker_mode:
			startActivity(new Intent(this, CareTakerActivity.class));
			break;
		case R.id.action_settings:
			new SettingsFragment().show(getSupportFragmentManager(), null);
			break;
		}
		return true;
	}

	public List<Person> getCareTakers() {
		return app.getPersons(Person.Type.CARETAKER);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				return new CareGiverHomeFragment();
			}
			List<Person> caretakers = getCareTakers();
			Fragment fragment = new CareGiverSectionFragment();
			Bundle args = new Bundle();
			if (position - 1 < caretakers.size()) {
				args.putString("person", new Gson().toJson(caretakers.get(position - 1)));
			}
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return getCareTakers().size() + 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if (position == 0) {
				return getString(R.string.home).toUpperCase(Locale.getDefault());
			}
			List<Person> caretakers = getCareTakers();
			if (position - 1 < caretakers.size()) {
				return caretakers.get(position - 1).name.toUpperCase(Locale.getDefault());
			}
			return null;
		}
	}
}
