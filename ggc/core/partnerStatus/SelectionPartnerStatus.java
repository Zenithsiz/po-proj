package ggc.core.partnerStatus;

import ggc.core.WarehouseManager;
import ggc.core.PartnerStatus;
import ggc.core.TimePeriod;

public class SelectionPartnerStatus implements PartnerStatus {
	@Override
	public double getDiscount(int date, int paymentDate, int factor) {
		// Check which time period we're on
		switch (TimePeriod.fromDate(date, paymentDate, factor)) {
			// 10% if we're on P1
			case P1:
				return 0.1;

			// 5% if we're within 2 days before P3, else fallthrough to 0.0
			case P2:
				if (date <= TimePeriod.startDateP3(paymentDate, factor) - 2) {
					return 0.05;
				}

				// 0%
			case P3:
			case P4:
			default:
				return 0.0;
		}
	}

	@Override
	public double getPenalty(int date, int paymentDate, int factor) {
		// Check which time period we're on
		switch (TimePeriod.fromDate(date, paymentDate, factor)) {
			// No penalties for P1, P2 or P3
			case P1:
			case P2:
			case P3:
				return 0.0;

			// 5% on P4
			case P4:
				return 0.05 * (date - TimePeriod.startDateP4(paymentDate, factor));

			default:
				return 0.0;
		}
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		return "SELECTION";
	}

}
