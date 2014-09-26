/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.gson.Gson;
import com.tagjam.pairmycare.model.Message;
import com.tagjam.pairmycare.model.Person;

public class GCMIntentService extends GCMBaseIntentService {

	@Override
	protected void onMessage(Context context, Intent intent) {
		String msgStr = intent.getExtras().getString("message");
		Message message = new Gson().fromJson(msgStr, Message.class);
		Log.i(TAG, String.format("Message : %s", new Gson().toJson(message)));
		if (message.from != null && message.from.type == Person.Type.CAREGIVER) {
			// Send message to CareTakerActivity
			startActivity(new Intent(this, CareTakerActivity.class).putExtra("message", msgStr)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
		} else if (message.from != null && message.from.type == Person.Type.CARETAKER) {
			// Send message to CareGiverActivity
			startActivity(new Intent(this, CareGiverActivity.class).putExtra("message", msgStr)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
		}
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		Log.i(TAG, String.format("Registered: %s", regId));
		((PairMyCareApp) getApplication()).saveRegistration(regId);
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		Log.i(TAG, String.format("Unregistered: %s", regId));
	}

	@Override
	protected void onError(Context context, String errorId) {
		Log.e(TAG, String.format("Error: %s", errorId));
	}
}
