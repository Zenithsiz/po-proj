package ggc.core.partnerStatus;

import ggc.core.WarehouseManager;
import ggc.core.PartnerStatus;
import ggc.core.TimePeriod;

public class NormalPartnerStatus implements PartnerStatus {
	@Override
	public double getDiscount(int date, int paymentDate, int factor) {
		// Check which time period we're on
		switch (TimePeriod.fromDate(date, paymentDate, factor)) {
			// 10% if we're on P1
			case P1:
				return 0.1;

			// 0% for all others
			case P2:
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
			// No penalties for P1 and P2
			case P1:
			case P2:
				return 0.0;
			// 5% on P3 and 10% on P4
			case P3:
				return 0.05 * (date - TimePeriod.startDateP3(paymentDate, factor));
			case P4:
				return 0.1 * (date - TimePeriod.startDateP4(paymentDate, factor));

			default:
				return 0.0;
		}
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		return "NORMAL";
	}

}
