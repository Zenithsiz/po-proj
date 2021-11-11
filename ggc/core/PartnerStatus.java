package ggc.core;

import java.io.Serializable;
import java.util.Optional;

/** Partner status */
public interface PartnerStatus extends Serializable, WarehouseFormattable {
	/**
	 * Calculates the discount of this status (0.0 to 1.0) for a given date
	 * 
	 * @param date
	 *            The date to calculate the discount at
	 * @param paymentDate
	 *            the payment date of the transaction
	 * @param factor
	 *            The product payment factor
	 * @return The discount
	 */
	double getDiscount(int date, int paymentDate, int factor);

	/**
	 * Calculates the penalty of this status (>= 0.0) for a given date
	 * 
	 * @param date
	 *            The date to calculate the penalty at
	 * @param paymentDate
	 *            the payment date of the transaction
	 * @param factor
	 *            The product payment factor
	 * @return The penalty
	 */
	double getPenalty(int date, int paymentDate, int factor);

	/**
	 * Possibly promotes this status, if the amount of points is enough
	 * 
	 * @param points
	 *            The amount of points of the partner
	 * @return The promoted partner status, if promoted
	 */
	Optional<PartnerStatus> checkPromotion(double points);

	/**
	 * Demotes this partner
	 * 
	 * @return The demoted partner status
	 */
	PartnerStatus demote();
}
