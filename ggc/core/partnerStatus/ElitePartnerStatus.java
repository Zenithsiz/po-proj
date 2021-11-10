package ggc.core.partnerStatus;

import ggc.core.WarehouseManager;
import ggc.core.PartnerStatus;
import ggc.core.TimePeriod;

public class ElitePartnerStatus implements PartnerStatus {
	@Override
	public double getDiscount(int date, int paymentDate, int factor) {
		// Check which time period we're on
		switch (TimePeriod.fromDate(date, paymentDate, factor)) {
			// 10% if we're on P1 or P2
			case P1:
			case P2:
				return 0.1;

			// 5% if we're in P3
			case P3:
				return 0.05;

			// 0% on P4
			case P4:
			default:
				return 0.0;
		}
	}

	@Override
	public double getPenalty(int date, int paymentDate, int factor) {
		// No penalties
		return 0.0;
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		return "ELITE";
	}

}
