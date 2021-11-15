package ggc.core.partnerstatus;

import ggc.core.WarehouseManager;
import ggc.core.util.Pair;

import java.util.Optional;
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
				if (date <= paymentDate - 2) {
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
			// No penalties for P1 or P2
			case P1:
			case P2:
				return 0.0;

			// 0 until 1 day after, then 2% daily
			case P3:
				if (date > paymentDate + 1) {
					return 0.02 * (date - paymentDate);
				}
				return 0.0;

			// 5% daily on P4
			case P4:
				return 0.05 * (date - paymentDate);

			default:
				return 0.0;
		}
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		return "SELECTION";
	}

	@Override
	public Optional<PartnerStatus> checkPromotion(double points) {
		// If we have more than `25000` points, go to `Elite`
		if (points >= 25000) {
			return Optional.of(new ElitePartnerStatus());
		}

		// Else we don't rank up
		return Optional.empty();
	}

	@Override
	public Pair<PartnerStatus, Double> checkDemotion(double points, int date, int paymentDate) {
		if (date > paymentDate + 2) {
			return new Pair<>(new NormalPartnerStatus(), 0.1 * points);
		} else {
			return new Pair<>(this, points);
		}
	}
}
