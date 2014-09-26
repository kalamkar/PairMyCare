/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare.model;

public class Measurement {
	public long timestamp;
	public String title;

	public long value;
	public boolean exception;

	public PlanItem plan;

	public String[] tags;
}
