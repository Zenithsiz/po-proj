package ggc.core;

/** Time period for calculating discounts and penalties */
public enum TimePeriod {
	/** Period 1 */
	P1,

	/** Period 2 */
	P2,

	/** Period 3 */
	P3,

	/** Period 4 */
	P4;

	/**
	 * Creates a time period from a date
	 * 
	 * @param date
	 *            The date to calculate the discount at
	 * @param paymentDate
	 *            the payment date of the transaction
	 * @param factor
	 *            The product payment factor
	 * @return The time period
	 */
	public static TimePeriod fromDate(int date, int paymentDate, int factor) {
		if (paymentDate - date >= factor) {
			return TimePeriod.P1;
		} else if (paymentDate - date >= 0) {
			return TimePeriod.P2;
		} else if (date - paymentDate <= factor) {
			return TimePeriod.P3;
		} else {
			return TimePeriod.P4;
		}
	}

	/**
	 * Returns the date at which P2 starts
	 *
	 * @param paymentDate
	 *            the payment date of the transaction
	 * @param factor
	 *            The product payment factor
	 * @return The start date of P2
	 */
	public static int startDateP2(int paymentDate, int factor) {
		return paymentDate - factor;
	}

	/**
	 * Returns the date at which P3 starts
	 *
	 * @param paymentDate
	 *            the payment date of the transaction
	 * @param factor
	 *            The product payment factor
	 * @return The start date of P3
	 */
	public static int startDateP3(int paymentDate, int factor) {
		return paymentDate;
	}

	/**
	 * Returns the date at which P4 starts
	 *
	 * @param paymentDate
	 *            the payment date of the transaction
	 * @param factor
	 *            The product payment factor
	 * @return The start date of P4
	 */
	public static int startDateP4(int paymentDate, int factor) {
		return paymentDate + factor;
	}
}