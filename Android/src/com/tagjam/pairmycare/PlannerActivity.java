/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import net.sourceforge.zbar.Symbol;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.tagjam.pairmycare.model.PlanItem;

public class PlannerActivity extends FragmentActivity implements OnClickListener {
	public static final String TAG = "PlannerActivity";

	public static final int SCANNER_ACTIVITY_REQUEST = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_planner);

		findViewById(R.id.save).setOnClickListener(this);
		findViewById(R.id.scan).setOnClickListener(this);

		((TimePicker) findViewById(R.id.time)).setCurrentHour(9);
		((TimePicker) findViewById(R.id.time)).setCurrentMinute(0);

		Spinner spinner = (Spinner) findViewById(R.id.actionSpinner);
		ArrayAdapter<PlanItem.Action> adapter = new ArrayAdapter<PlanItem.Action>(this,
				android.R.layout.simple_spinner_item, PlanItem.Action.values());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.save) {
			PlanItem plan = new PlanItem();
			plan.title = ((TextView) findViewById(R.id.title)).getText().toString();
			plan.timeHour = ((TimePicker) findViewById(R.id.time)).getCurrentHour();
			plan.timeMinute = ((TimePicker) findViewById(R.id.time)).getCurrentMinute();
			plan.action = (PlanItem.Action) ((Spinner) findViewById(R.id.actionSpinner))
					.getSelectedItem();
			try {
				plan.expectedValue = Long.parseLong(((TextView) findViewById(R.id.expectedValue))
						.getText().toString());
				plan.graceValue = Long.parseLong(((TextView) findViewById(R.id.graceValue))
						.getText().toString());
			} catch (NumberFormatException ex) {
			}
			setResult(RESULT_OK, new Intent().putExtra("plan", Utils.toJson(plan)));
			finish();
		} else if (v.getId() == R.id.scan) {
			startActivityForResult(
					new Intent(this, ScannerActivity.class).putExtra("type", Symbol.NONE),
					SCANNER_ACTIVITY_REQUEST);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SCANNER_ACTIVITY_REQUEST:
			if (data != null && data.hasExtra("type") && data.hasExtra("data")) {
				((TextView) findViewById(R.id.expectedValue)).setText(data.getStringExtra("data"));
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
