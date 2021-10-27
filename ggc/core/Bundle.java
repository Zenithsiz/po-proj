package ggc.core;

import java.io.Serializable;

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
		_product = product;
		_quantity = quantity;
		_partner = partner;
		_unitPrice = unitPrice;
	}
}
