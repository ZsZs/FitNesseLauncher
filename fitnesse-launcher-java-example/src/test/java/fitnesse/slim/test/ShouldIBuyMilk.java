package fitnesse.slim.test;

import java.util.List;


/**
 * Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
 * Released under the terms of the CPL Common Public License version 1.0.
 * Originally copied from FitNesse source code.
 */
public class ShouldIBuyMilk {

	private int dollars;
	
	private int pints;
	
	private boolean creditCard;

	public void setCashInWallet(int dollars) {
		this.dollars = dollars;
	}

	public void setPintsOfMilkRemaining(int pints) {
		this.pints = pints;
	}

	public void setCreditCard(String valid) {
		creditCard = "yes".equals(valid);
	}

	public String goToStore() {
		/*if (pints == 1 && dollars > 2 && creditCard) {
    		// For testing Project Issue #36
			throw new IllegalStateException("TEST EXCEPTION [ShouldIBuyMilk]",
					new NullPointerException("NESTED"));
		} else*/ if (pints == 0 && (dollars > 2 || creditCard)) {
			return "yes";
		} else {
			return "no";
		}
	}

	public void execute() {

	}

	public void table(List<List<String>> table) {
	}
}
