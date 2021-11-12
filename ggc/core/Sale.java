package ggc.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * A sale of product to a partner
 * 
 * Represents a transaction in which the warehouse sells a quantity of product to a partner, possibly manufacturing
 * more, if not directly available.
 */
public class Sale extends Transaction {
	/** Deadline for the payment of this sale */
	private int _paymentDeadline;

	/** Date this sale was paid at, if paid */
	// Note: transient because we can't [de]serialize an optional
	private transient OptionalInt _paymentDate;

	/** Base cost of the transaction */
	private double _baseCost;

	/** Cost of the transaction, if paid */
	// Note: transient because we can't [de]serialize an optional
	private transient OptionalDouble _paidCost;

	/**
	 * Creates a new sale
	 * 
	 * @param id
	 *            The id of this transaction
	 * @param product
	 *            The product that was sold
	 * @param partner
	 *            The partner the product was sold to
	 * @param quantity
	 *            The amount of product sold
	 * @param baseCost
	 *            The base cost of this sale
	 * @param paymentDeadline
	 *            The payment deadline for this sale
	 */
	Sale(int id, Product product, Partner partner, int quantity, double baseCost, int paymentDeadline) {
		super(id, product, partner, quantity);
		_paymentDeadline = paymentDeadline;
		_paymentDate = OptionalInt.empty();
		_paidCost = OptionalDouble.empty();
		_baseCost = baseCost;
	}

	/**
	 * Override for serialization to write our transient fields
	 * 
	 * @param out
	 *            The stream to write to
	 * @throws IOException
	 *             If unable to write
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeBoolean(_paymentDate.isPresent());
		if (_paymentDate.isPresent()) {
			out.writeInt(_paymentDate.getAsInt());
		}

		out.writeBoolean(_paidCost.isPresent());
		if (_paidCost.isPresent()) {
			out.writeDouble(_paidCost.getAsDouble());
		}
	}

	/**
	 * Override for deserialization to read our transient fields
	 * 
	 * @param in
	 *            The stream to read from
	 * @throws IOException
	 *             If unable to read
	 * @throws ClassNotFoundException
	 *             If a class wasn't found while loading
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		_paymentDate = in.readBoolean() ? OptionalInt.of(in.readInt()) : OptionalInt.empty();
		_paidCost = in.readBoolean() ? OptionalDouble.of(in.readDouble()) : OptionalDouble.empty();
	}

	/**
	 * Retrieves the payment deadline of this sale
	 * 
	 * @return The payment deadline
	 */
	int getPaymentDeadline() {
		return _paymentDeadline;
	}

	/**
	 * Retrieves the sale's base cost
	 * 
	 * @return The base cost
	 */
	double getBaseCost() {
		return _baseCost;
	}

	/**
	 * Retrieves the sale's paid cost
	 * 
	 * @return The paid cost
	 */
	OptionalDouble getPaidCost() {
		return _paidCost;
	}

	/**
	 * Retrieves the total cost, if paid on `date`.
	 * 
	 * @param date
	 *            The date to check payment on
	 * @return The total cost
	 */
	double getTotalCostAt(int date) {
		int paymentFactor = getProduct().getPaymentFactor();
		var discount = getPartner().getStatus().getDiscount(date, _paymentDeadline, paymentFactor);
		var penalty = getPartner().getStatus().getPenalty(date, _paymentDeadline, paymentFactor);

		return _baseCost * (1.0 - discount) * (1.0 + penalty);
	}

	/**
	 * Retrieves if this sale is paid
	 * 
	 * @return If this sale is paid
	 */
	boolean isPaid() {
		return _paidCost.isPresent() && _paymentDate.isPresent();
	}

	/**
	 * Pays this sale and returns the amount paid.
	 * 
	 * <p>
	 * If already paid, doesn't do anything and returns the paid cost.
	 * </p>
	 * 
	 * @param date
	 *            The date to pay this sale at
	 * @return The amount paid
	 */
	double pay(int date) {
		// If we're already paid, ignore
		if (isPaid()) {
			return _paidCost.getAsDouble();
		}

		// Get the total cost for `date` and set ourselves as paid
		var paymentAmount = getTotalCostAt(date);
		_paidCost = OptionalDouble.of(paymentAmount);
		_paymentDate = OptionalInt.of(date);

		return paymentAmount;
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		var partner = getPartner();
		var product = getProduct();

		// Either the payed amount, or the amount to pay
		var paidCost = _paidCost.orElseGet(() -> getTotalCostAt(warehouseManager.getDate()));

		var baseString = new StringBuilder(String.format("VENDA|%d|%s|%s|%d|%d|%d|%d", getId(), partner.getId(),
				product.getId(), getQuantity(), Math.round(_baseCost), Math.round(paidCost), getPaymentDeadline()));

		if (isPaid()) {
			baseString.append(String.format("|%d", _paymentDate.getAsInt()));
		}

		return baseString.toString();
	}
}
