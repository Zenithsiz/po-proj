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
}