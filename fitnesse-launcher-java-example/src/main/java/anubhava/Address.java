package anubhava;

import java.util.ArrayList;
import java.util.List;

/**
 * Modified or written by Anubhava Srivastava for use with FitNesse.
 * Copyright (c) 2008 Anubhava Srivastava 
 * Released under the terms of the GNU General Public License version 2
 * 
 * @author Anubhava Srivastava
 * @see "http://anubhava.wordpress.com/"
 */
public class Address {
	public static class Name {
		private String surname = null;
		private String firstname = null;

		public Name() {
		}

		public Name(String surname, String firstname) {
			super();
			this.surname = surname;
			this.firstname = firstname;
		}

		public Name(Name n) {
			this.surname = n.surname;
			this.firstname = n.firstname;
		}

		public static Object parse(String s) {
			Name n = new Name();
			String[] arr = s.split(" ");
			if (arr.length == 2) {
				n.setFirstname(arr[0]);
				n.setSurname(arr[1]);
			}
			return n;
		}

		@Override
		public String toString() {
			return this.firstname + " " + this.surname;
		}

		public String getSurname() {
			return surname;
		}

		public void setSurname(String surname) {
			this.surname = surname;
		}

		public String getFirstname() {
			return firstname;
		}

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}

		@Override
		public boolean equals(Object o) {
			// GenericFixture.debug("************* inside equals: " +
			// o.getClass());
			boolean result = false;
			if (this == o) {
				result = true;
			} else if (o instanceof String) {
				Name n = (Name) parse(o.toString());
				result = this.equals(n);
			} else if (o instanceof Name) {
				Name n = (Name) o;
				if (this.surname.equalsIgnoreCase(n.surname)
						&& this.firstname.equalsIgnoreCase(n.firstname))
					result = true;
			}
			return result;
		}
	}

	private int streetNo = 0;
	private String street = null;
	private String city = null;
	private int postcode = 0;
	private String state = null;
	private Name name = null;
	private List<Name> residents;

	public Address() {
		super();
		residents = new ArrayList<Name>();
	}

	public Address(Integer streetNo, String street, String city, int postcode,
			String state) {
		super();
		setAddress(streetNo, street, city, postcode, state);
		residents = new ArrayList<Name>();
	}

	public void setAddress(Integer streetNo, String street, String city,
			int postcode, String state) {
		this.streetNo = streetNo.intValue();
		this.street = street;
		this.city = city;
		this.postcode = postcode;
		this.state = state;
	}

	public void setStreetNo(Integer streetNo) {
		this.streetNo = streetNo;
	}

	public Integer getStreetNo() {
		return this.streetNo;
	}

	public String getStreet() {
		return this.street;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCity() {
		return this.city;
	}

	public void setPostcode(int postcode) {
		this.postcode = postcode;
		// For testing Project Issue #36
		//throw new IllegalStateException("TEST EXCEPTION [Address]",
				//new NullPointerException("NESTED"));
	}

	public int getPostcode() {
		return this.postcode;
	}

	public String getFullAddress() {
		return this.streetNo + " " + this.street + ", " + this.city + ", "
				+ this.state + " - " + this.postcode;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Name getName() {
		return this.name;
	}

	public void setName(Name name) {
		this.name = name;
		residents.add(name);
	}

	public void addResident(Name name) {
		residents.add(name);
	}

	public Name[] getResidents() {
		return residents.toArray(new Name[0]);
	}

	public static int countNames(Name[] names) {
		return names.length;
	}
}
