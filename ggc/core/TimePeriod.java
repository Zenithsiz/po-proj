package ggc.core;

/// Time period for calculating discounts and penalties
public enum TimePeriod {
	P1, P2, P3, P4;

	/// Creates a time period from the current date, the payment date, and the factor
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

	/// Returns the date at which P2 starts
	public static int startDateP2(int paymentDate, int factor) {
		return paymentDate - factor;
	}

	/// Returns the date at which P3 starts
	public static int startDateP3(int paymentDate, int factor) {
		return paymentDate;
	}

	/// Returns the date at which P4 starts
	public static int startDateP4(int paymentDate, int factor) {
		return paymentDate + factor;
	}
}