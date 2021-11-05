package ggc.core;

import java.io.Serializable;

public abstract class Transaction implements Serializable {
	/// Serial number for serialization.
	private static final long serialVersionUID = 2021_10_27_03_16L;

	/// Id
	private int _id;

	/// Payment date
	private int _paymentDate;

	/// Product of this transaction
	private Product _product;

	/// Partner associated with this transaction
	private Partner _partner;

	/// Quantity of product of this transaction
	private int _quantity;

	/// Total price of transaction
	private double _totalPrice;

	// Note: Package private to ensure we don't construct it outside of `core`.
	Transaction(int id, int paymentDate, Product product, Partner partner, int quantity, double totalPrice) {
		_id = id;
		_paymentDate = paymentDate;
		_product = product;
		_partner = partner;
		_quantity = quantity;
		_totalPrice = totalPrice;
	}

	/// Returns this transaction's id
	int getId() {
		return _id;
	}

	/// Returns this transaction's payment date
	int getPaymentDate() {
		return _paymentDate;
	}

	/// Returns this transaction's product
	Product getProduct() {
		return _product;
	}

	/// Returns this transaction's partner
	Partner getPartner() {
		return _partner;
	}

	/// Returns this transaction's product amount
	int getAmount() {
		return _quantity;
	}

	/// Returns this transaction's total price
	double getTotalPrice() {
		return _totalPrice;
	}

	/// Formats this transaction
	abstract String format();
}
