package ggc.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/// A sale by credit
public class CreditSale extends Sale {
	/// Sale deadline
	private int _deadline;

	/// Payment date
	private transient OptionalInt _paymentDate;

	/// Amount paid, if any
	// Note: transient because we can't [de]serialize an optional
	private transient OptionalDouble _paidAmount;

	CreditSale(int id, Product product, Partner partner, int quantity, double totalPrice, int deadline) {
		super(id, product, partner, quantity, totalPrice);
		_deadline = deadline;
		_paymentDate = OptionalInt.empty();
		_paidAmount = OptionalDouble.empty();
	}

	// Note: We need to override the saving and loading because we use a `OptionalDouble`.
	// Note: We use `NaN` as a sentinel value for the paid amount, as that shouldn't ever be a valid
	//       amount to paid. We also use `-1` for the payment date, as that shouldn't be a valid date
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeInt(_paymentDate.orElse(-1));
		out.writeDouble(_paidAmount.orElse(Double.NaN));
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		var paymentDate = in.readInt();
		var paidAmount = in.readDouble();
		_paymentDate = paymentDate == -1 ? OptionalInt.empty() : OptionalInt.of(paymentDate);
		_paidAmount = Double.isNaN(paidAmount) ? OptionalDouble.empty() : OptionalDouble.of(paidAmount);
	}

	/// Returns the deadline of this sale
	int getDeadline() {
		return _deadline;
	}

	/// Returns the payment amount for this sale.
	/// 
	/// This is either the paid amount, or the amount to pay, if paid today
	double getPaymentAmount(int date) {
		int paymentFactor = getProduct().getPaymentFactor();
		var discount = getPartner().getStatus().getDiscount(date, _deadline, paymentFactor);
		var penalty = getPartner().getStatus().getPenalty(date, _deadline, paymentFactor);

		return getTotalPrice() * (1.0 - discount) * (1.0 + penalty);
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		var partner = getPartner();
		var product = getProduct();

		// Either the payed amount, or the amount to pay
		var paymentAmount = _paidAmount.orElseGet(() -> getPaymentAmount(warehouseManager.getDate()));

		var baseString = new StringBuilder(String.format("VENDA|%d|%s|%s|%d|%.0f|%.0f|%d", getId(), partner.getId(),
				product.getId(), getAmount(), getTotalPrice(), paymentAmount, getDeadline()));

		if (_paidAmount.isPresent() && _paymentDate.isPresent()) {
			baseString.append(String.format("|%d", _paymentDate.getAsInt()));
		}

		return baseString.toString();
	}

	@Override
	boolean isPaid() {
		return _paidAmount.isPresent();
	}

}
