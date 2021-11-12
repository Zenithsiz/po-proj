package ggc.core.partnerstatus;

import ggc.core.WarehouseManager;
import java.util.Optional;
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

	@Override
	public Optional<PartnerStatus> checkPromotion(double points) {
		// If we have more than `2000` points, go to `Selection`
		if (points >= 2000) {
			return Optional.of(new SelectionPartnerStatus());
		}

		// Else we don't rank up
		return Optional.empty();
	}

	@Override
	public PartnerStatus demote() {
		// Note: If we get demoted, we're still a normal
		return this;
	}

}
