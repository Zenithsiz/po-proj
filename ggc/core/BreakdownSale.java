package ggc.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ggc.core.util.Pair;

/// A sale from a breakdown
public class BreakdownSale extends Sale {
	/// Payment date
	private int _paymentDate;

	/// Differential total price
	private double _differentialTotalPrice;

	/// Product quantities created by this breakdown
	private List<Pair<Product, Pair<Integer, Double>>> _productsCreated;

	BreakdownSale(int id, int paymentDate, Product product, Partner partner, int quantity, double totalPrice,
			List<Pair<Product, Pair<Integer, Double>>> productsCreated) {
		super(id, product, partner, quantity, Math.abs(totalPrice));
		_paymentDate = paymentDate;
		_differentialTotalPrice = totalPrice;
		_productsCreated = new ArrayList<>(productsCreated);
	}

	/// Returns the payment date of this sale
	int getPaymentDate() {
		return _paymentDate;
	}

	/// Returns the differential total price of this sale
	double getDifferentialTotalPrice() {
		return _differentialTotalPrice;
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		var partner = getPartner();
		var product = getProduct();
		var baseString = new StringBuilder(String.format("DESAGREGAÇÃO|%s|%s|%s|%d|%.0f|%.0f|%d|", getId(),
				partner.getId(), product.getId(), getAmount(), _differentialTotalPrice, getTotalPrice(), _paymentDate));

		String components = _productsCreated.stream().map(pair -> String.format("%s:%d:%.0f", pair.getLhs().getId(),
				pair.getRhs().getLhs(), pair.getRhs().getRhs())).collect(Collectors.joining("#"));

		baseString.append(components);

		return baseString.toString();
	}

	@Override
	boolean isPaid() {
		return true;
	}
}
