package ggc.core;

import java.io.Serializable;
import java.util.Objects;

/// Product batch
public class Batch implements Serializable {
	/// Serial number for serialization.
	private static final long serialVersionUID = 2021_10_27_05_54L;

	/// The product in this batch
	private Product _product;

	/// Quantity of product in this batch
	private int _quantity;

	/// Partner of this batch
	private Partner _partner;

	/// Price of each unit
	private double _unitPrice;

	// Note: Package private to ensure we don't construct it outside of `core`.
	Batch(Product product, int quantity, Partner partner, double unitPrice) {
		_product = Objects.requireNonNull(product);
		_quantity = quantity;
		_partner = Objects.requireNonNull(partner);
		_unitPrice = unitPrice;
	}

	/// Returns the product of this batch
	Product getProduct() {
		return _product;
	}

	/// Returns the partner of this batch
	Partner getPartner() {
		return _partner;
	}

	/// Returns the per-unit price of this batch
	double getUnitPrice() {
		return _unitPrice;
	}

	/// Returns the quantity of product in this batch
	int getQuantity() {
		return _quantity;
	}

	/// Compares two batches by unit price
	static int compareByUnitPrice(Batch lhs, Batch rhs) {
		return Double.compare(lhs._unitPrice, rhs._unitPrice);
	}
}
