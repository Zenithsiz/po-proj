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

	/// Amount of product of this transaction
	private int _amount;

	/// Total price of transaction
	private double _totalPrice;

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

	/// Returns this transaction's product amount
	int getAmount() {
		return _amount;
	}

	/// Returns this transaction's total price
	double getTotalPrice() {
		return _totalPrice;
	}
}
