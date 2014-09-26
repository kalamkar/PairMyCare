/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare.model;

public class PlanItem {
	public static enum Action {
		NONE, SCAN_CODE, HEART_RATE, PEDOMETER_TODAY
	}

	public String title;

	public int timeHour;
	public int timeMinute;
	public long graceMillis;

	public String[] repeatDays;

	public long expectedValue;
	public long graceValue;

	public Action action;
}
