/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import java.util.Locale;

import net.sourceforge.zbar.Symbol;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.tagjam.pairmycare.model.Measurement;
import com.tagjam.pairmycare.model.Message;
import com.tagjam.pairmycare.model.Person;
import com.tagjam.pairmycare.model.PlanItem;

public class ExecutePlanActivity extends FragmentActivity implements OnClickListener,
		TextToSpeech.OnInitListener {
	public static final String TAG = "ExecutePlanActivity";

	public static final int SCANNER_ACTIVITY_REQUEST = 1;

	private TextToSpeech tts;

	private PlanItem plan;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_execute);

		plan = Utils.fromJson(getIntent().getStringExtra("plan"), PlanItem.class);

		if (((PairMyCareApp) getApplication()).isTtsEnabled()) {
			tts = new TextToSpeech(this, this);
		}

		findViewById(R.id.ok).setOnClickListener(this);
		findViewById(R.id.scan).setOnClickListener(this);
		findViewById(R.id.pedometer).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);

		if (plan.action != PlanItem.Action.NONE) {
			((TextView) findViewById(R.id.action)).setText(plan.action.toString().toLowerCase()
					.replace('_', ' '));
		} else {
			findViewById(R.id.action).setVisibility(View.INVISIBLE);
		}
		if (plan.action == PlanItem.Action.SCAN_CODE) {
			findViewById(R.id.scan).setVisibility(View.VISIBLE);
		} else if (plan.action == PlanItem.Action.PEDOMETER_TODAY) {
			findViewById(R.id.pedometer).setVisibility(View.VISIBLE);
		}
		if (plan.expectedValue == 0) {
			findViewById(R.id.value).setVisibility(View.INVISIBLE);
		}
		((TextView) findViewById(R.id.title)).setText(plan.title);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ok) {
			Message message = new Message();
			message.measurement = new Measurement();
			message.measurement.timestamp = System.currentTimeMillis();
			message.measurement.plan = plan;

			String value = ((TextView) findViewById(R.id.value)).getText().toString();
			try {
				message.measurement.value = Long.parseLong(value);
			} catch (Exception ex) {
				Log.w(TAG, ex);
			}

			if (plan.expectedValue != 0) {
				if ((message.measurement.value - plan.graceValue > plan.expectedValue)
						|| (message.measurement.value + plan.graceValue < plan.expectedValue)) {
					message.measurement.exception = true;
				}
			} else {
				message.measurement.exception = false;
			}
			message.measurement.title = String.format("Completed %s", plan.title);
			((PairMyCareApp) getApplication()).postMessage(Person.Type.CARETAKER, message, null);
			finish();
		} else if (v.getId() == R.id.cancel) {
			Message message = new Message();
			message.measurement = new Measurement();
			message.measurement.exception = true;
			message.measurement.title = String.format("Skipped %s", plan.title);
			((PairMyCareApp) getApplication()).postMessage(Person.Type.CARETAKER, message, null);
			finish();
		} else if (v.getId() == R.id.scan) {
			startActivityForResult(
					new Intent(this, ScannerActivity.class).putExtra("type", Symbol.NONE),
					SCANNER_ACTIVITY_REQUEST);
		} else if (v.getId() == R.id.pedometer) {
			((TextView) findViewById(R.id.value)).setText("9000");
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SCANNER_ACTIVITY_REQUEST:
			if (data != null && data.hasExtra("type") && data.hasExtra("data")) {
				String value = data.getStringExtra("data");
				((TextView) findViewById(R.id.value)).setText(value);
				try {
					long longValue = Long.parseLong(value);
					if (plan.expectedValue != longValue) {
						findViewById(R.id.value).setBackgroundColor(
								getResources().getColor(android.R.color.holo_orange_light));
						MediaPlayer.create(this, R.raw.zap).start();
					} else {
						findViewById(R.id.value).setBackgroundColor(
								getResources().getColor(android.R.color.holo_green_light));
					}
				} catch (NumberFormatException ex) {
					Log.w(TAG, ex);
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e(TAG, "This Language is not supported");
				return;
			}
		} else {
			Log.e("TTS", "Initilization Failed!");
			return;
		}
		tts.speak(String.format("Time to %s", plan.title), TextToSpeech.QUEUE_FLUSH, null);
	}
}
