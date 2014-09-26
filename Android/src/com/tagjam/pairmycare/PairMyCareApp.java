/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Application;
import android.provider.Settings.Secure;
import android.util.Log;
import android.util.Pair;

import com.google.android.gcm.GCMRegistrar;
import com.tagjam.pairmycare.model.Message;
import com.tagjam.pairmycare.model.Person;
import com.tagjam.pairmycare.model.Person.Type;
import com.tagjam.pairmycare.model.PlanItem;
import com.tagjam.pairmycare.net.BaseHttpRequest;
import com.tagjam.pairmycare.net.MessageRequest;

public class PairMyCareApp extends Application {
	private static final String TAG = "PairMyCareApp";
	private static final String GCM_SENDER_ID = "854741235433";

	private final Person owner = new Person();
	private final List<Person> persons = new ArrayList<Person>();
	private final List<Message> messages = new ArrayList<Message>();
	private final List<PlanItem> plan = new ArrayList<PlanItem>();

	@Override
	public void onCreate() {
		super.onCreate();
		owner.id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

		// Initialize push notifications
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, GCM_SENDER_ID);
		} else {
			Log.v(TAG, "Already registered " + regId);
			saveRegistration(regId);
		}

		loadList(persons, "persons", Person.class);
		loadList(messages, "messages", Message.class);
		loadList(plan, "plan", PlanItem.class);
	}

	public Person getOwner() {
		return owner;
	}

	public void setOwnerName(String ownerName) {
		owner.name = owner.name;
		getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putString("owner", ownerName)
				.commit();
	}

	public String getOwnerName() {
		if (owner.name == null) {
			owner.name = getSharedPreferences(getPackageName(), MODE_PRIVATE).getString("owner",
					null);
		}
		return owner.name;
	}

	public void setTtsEnabled(boolean enabled) {
		getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean("tts", enabled)
				.commit();
	}

	public boolean isTtsEnabled() {
		return getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean("tts", false);
	}

	public List<Person> getPersons(Type type) {
		List<Person> personsOfType = new ArrayList<Person>();
		for (Person person : persons) {
			if (person.type == type) {
				personsOfType.add(person);
			}
		}
		return personsOfType;
	}

	public void addPerson(Person person) {
		persons.add(person);
		storeList(persons, "persons", Person.class);
	}

	public void removePerson(Person person) {
		persons.remove(person);
		storeList(persons, "persons", Person.class);
	}

	public boolean hasPerson(Person person) {
		return persons.contains(person);
	}

	@SuppressWarnings("unchecked")
	public void saveRegistration(String regId) {
		if (owner.id == null) {
			owner.id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		}
		Log.i(TAG, String.format("Saving registration for %s on server.", owner.id));
		new BaseHttpRequest("http://pairmycare.appspot.com/register").execute(
				Pair.create("id", owner.id), Pair.create("token", regId));
	}

	@SuppressWarnings("unchecked")
	public void postMessage(Person.Type myModeType, Message message, Person person) {
		message.from = new Person();
		message.from.id = owner.id;
		message.from.name = owner.name;
		message.from.type = myModeType;
		String msgStr = Utils.toJson(message);
		if (person == null || person.id == null) {
			for (Person to : persons) {
				Log.i(TAG, String.format("Posting message %s to %s.", msgStr, to.id));
				new MessageRequest("http://pairmycare.appspot.com/message").execute(
						Pair.create("id", to.id), Pair.create("message", msgStr));
			}
		} else {
			Log.i(TAG, String.format("Posting message %s to %s.", msgStr, person.id));
			new MessageRequest("http://pairmycare.appspot.com/message").execute(
					Pair.create("id", person.id), Pair.create("message", msgStr));
		}
	}

	public void storeMessage(Message message) {
		messages.add(message);
		storeList(messages, "messages", Message.class);
	}

	public List<Message> getMessages(Person from) {
		if (from == null) {
			return messages;
		}
		List<Message> messagesFrom = new ArrayList<Message>();
		for (Message message : messages) {
			if (message.from != null && message.from.equals(from)) {
				messagesFrom.add(message);
			}
		}
		return messagesFrom;
	}

	public void removeMessage(Message message) {
		messages.remove(message);
		storeList(messages, "messages", Message.class);
	}

	public void addPlan(PlanItem item) {
		plan.add(item);
		storeList(plan, "plan", PlanItem.class);
	}

	public void removePlan(PlanItem item) {
		Log.i(TAG, String.format("Deleting plan item success = ", plan.remove(item)));
		storeList(plan, "plan", PlanItem.class);
	}

	public final List<PlanItem> getPlan() {
		return plan;
	}

	private <T> void loadList(List<T> list, String prefName, Class<T> classOfT) {
		Set<String> set = getSharedPreferences(getPackageName(), MODE_PRIVATE).getStringSet(
				prefName, new HashSet<String>());
		for (String item : set) {
			list.add(Utils.fromJson(item, classOfT));
		}
	}

	private <T> void storeList(List<T> list, String prefName, Class<T> classOfT) {
		Set<String> set = new HashSet<String>();
		for (T item : list) {
			set.add(Utils.toJson(item));
		}

		Log.i(TAG, String.format("Storing %d items to %s", set.size(), prefName));
		getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putStringSet(prefName, set)
				.commit();
	}
}
