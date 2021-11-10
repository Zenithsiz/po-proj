package ggc.core;

import java.io.Serializable;

/// Partner status
public interface PartnerStatus extends Serializable, WarehouseFormattable {
	/// Returns the discount of this status (0.0 to 1.0) for a given date
	double getDiscount(int date, int paymentDate, int factor);

	/// Returns the penalty of this status (>= 0.0) for a given date
	double getPenalty(int date, int paymentDate, int factor);
}
