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
public class CreditSale extends Sale {
	/** Deadline for the payment of this sale */
	private int _paymentDeadline;

	/** Date this sale was paid at, if paid */
	// Note: transient because we can't [de]serialize an optional
	private transient OptionalInt _paymentDate;

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
	 * @param totalPrice
	 *            The base total cost of this sale
	 * @param paymentDeadline
	 *            The payment deadline for this sale
	 */
	// TODO: Swap `partner` and `quantity`, like in `Bundle`.
	CreditSale(int id, Product product, Partner partner, int quantity, double totalPrice, int paymentDeadline) {
		super(id, product, partner, quantity, totalPrice);
		_paymentDeadline = paymentDeadline;
		_paymentDate = OptionalInt.empty();
		_paidCost = OptionalDouble.empty();
	}

	/**
	 * Override for serialization to write our transient fields
	 * 
	 * @param out
	 *            The stream to write to
	 * @throws IOException
	 *             If unable to write
	 */
	// Note: We need to override the saving and loading because we use a `OptionalDouble`.
	// Note: We use `NaN` as a sentinel value for the paid amount, as that shouldn't ever be a valid
	//       amount to paid. We also use `-1` for the payment date, as that shouldn't be a valid date
	// TODO: Think about writing a boolean to see if present.
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeInt(_paymentDate.orElse(-1));
		out.writeDouble(_paidCost.orElse(Double.NaN));
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
		var paymentDate = in.readInt();
		var paidAmount = in.readDouble();
		_paymentDate = paymentDate == -1 ? OptionalInt.empty() : OptionalInt.of(paymentDate);
		_paidCost = Double.isNaN(paidAmount) ? OptionalDouble.empty() : OptionalDouble.of(paidAmount);
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
	 * Retrieves the amount paid, if paid, or else calculates the amount that would need to be paid if paid on
	 * <code> date </code>.
	 * 
	 * @param date
	 *            The date to check payment on
	 * @return The amount paid, if paid, or the amount that needs to be paid on <code>date</code>
	 */
	double getPaymentAmount(int date) {
		int paymentFactor = getProduct().getPaymentFactor();
		var discount = getPartner().getStatus().getDiscount(date, _paymentDeadline, paymentFactor);
		var penalty = getPartner().getStatus().getPenalty(date, _paymentDeadline, paymentFactor);

		return getTotalPrice() * (1.0 - discount) * (1.0 + penalty);
	}

	/**
	 * Pays this sale
	 * 
	 * @param date
	 *            The date to pay this sale at
	 * @return The amount paid
	 */
	double pay(int date) {
		var paymentAmount = getPaymentAmount(date);

		_paidCost = OptionalDouble.of(paymentAmount);
		_paymentDate = OptionalInt.of(date);

		return paymentAmount;
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		var partner = getPartner();
		var product = getProduct();

		// Either the payed amount, or the amount to pay
		var paymentAmount = _paidCost.orElseGet(() -> getPaymentAmount(warehouseManager.getDate()));

		var baseString = new StringBuilder(String.format("VENDA|%d|%s|%s|%d|%.0f|%.0f|%d", getId(), partner.getId(),
				product.getId(), getQuantity(), getTotalPrice(), paymentAmount, getPaymentDeadline()));

		if (isPaid()) {
			baseString.append(String.format("|%d", _paymentDate.getAsInt()));
		}

		return baseString.toString();
	}

	@Override
	boolean isPaid() {
		return _paidCost.isPresent() && _paymentDate.isPresent();
	}

}
