/*******************************************************************************
 * Copyright 2013 Abhijit Kalamkar. All rights reserved.
 ******************************************************************************/
package com.tagjam.pairmycare.model;

public class Person {
	public static enum Type {
		CARETAKER, CAREGIVER
	}

	public Type type;
	public String id;
	public String name;

	@Override
	public boolean equals(Object o) {
		try {
			Person person = (Person) o;
			if (id != null && id.equals(person.id) && name != null && name.equals(person.name)
					&& type == person.type) {
				return true;
			}
		} catch (Exception ex) {
		}
		return false;
	}
}
