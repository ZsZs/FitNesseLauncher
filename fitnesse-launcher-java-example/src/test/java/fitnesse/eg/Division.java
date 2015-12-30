package fitnesse.eg;

import fit.ColumnFixture;

/**
 * Originally copied from FitNesse source code.
 * 
 * @see eg.Division
 */
public class Division extends ColumnFixture {

	public double numerator;

	public double denominator;

	public double quotient() {
		double quotient = numerator / denominator;
		// For testing Project Issue #36
		/*if (quotient > 20.0) {
			throw new IllegalStateException("TEST EXCEPTION [Division]",
					new NullPointerException("NESTED"));
		}*/
		return quotient;
	}

	public void setNumerator(double numerator) {
		this.numerator = numerator;
	}

	public void setDenominator(double denominator) {
		this.denominator = denominator;
	}
}
