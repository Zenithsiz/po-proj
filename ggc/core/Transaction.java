package ggc.core;

import java.io.Serializable;

public class Transaction implements Serializable {
	/// Serial number for serialization.
	private static final long serialVersionUID = 2021_10_27_03_16L;

	/// Id
	private int _id;

	/// Payment date
	private int _paymentDate;

	/// Partner associated with this transaction
	private Partner _partner;

	/// Product of this transaction
	private Product _product;

	/// Amount of product of this transaction
	private int _amount;

	/// Total price of transaction
	private float _totalPrice;
}
