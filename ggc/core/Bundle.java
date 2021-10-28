package ggc.core;

import java.io.Serializable;
import java.util.Objects;

/// Product bundle
public class Bundle implements Serializable {
	/// Serial number for serialization.
	private static final long serialVersionUID = 2021_10_27_05_54L;

	/// The product in this bundle
	private Product _product;

	/// Quantity of product in this bundle
	private int _quantity;

	/// Partner of this bundle
	private Partner _partner;

	/// Price of each unit
	private float _unitPrice;

	// Note: Package private to ensure we don't construct it outside of `core`.
	Bundle(Product product, int quantity, Partner partner, float unitPrice) {
		_product = Objects.requireNonNull(product);
		_quantity = quantity;
		_partner = Objects.requireNonNull(partner);
		_unitPrice = unitPrice;
	}

	/// Returns the product of this bundle
	public Product getProduct() {
		return _product;
	}

	/// Returns the per-unit price of this bundle
	public float getUnitPrice() {
		return _unitPrice;
	}

	/// Returns the quantity of product in this bundle
	public int getQuantity() {
		return _quantity;
	}

	/// Compares two bundles by unit price
	public static int compareByUnitPrice(Bundle lhs, Bundle rhs) {
		return Float.compare(lhs._unitPrice, rhs._unitPrice);
	}
}
