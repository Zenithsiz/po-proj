package ggc.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.OptionalDouble;

/// A sale by credit
public class CreditSale extends Sale {
	/// Sale deadline
	private int _deadline;

	/// Amount paid, if any
	// Note: transient because we can't [de]serialize an optional
	private transient OptionalDouble _paidAmount;

	CreditSale(int id, int paymentDate, Product product, Partner partner, int quantity, double totalPrice,
			int deadline) {
		super(id, paymentDate, product, partner, quantity, totalPrice);
		_deadline = deadline;
		_paidAmount = OptionalDouble.empty();
	}

	// Note: We need to override the saving and loading because we use a `OptionalDouble`.
	// Note: We use `NaN` as a sentinel value for the paid amount, as that shouldn't ever be a valid
	//       amount to paid
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(_paidAmount.orElse(Double.NaN));
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		var paidAmount = in.readDouble();
		_paidAmount = Double.isNaN(paidAmount) ? OptionalDouble.empty() : OptionalDouble.of(paidAmount);
	}

	/// Returns the deadline of this sale
	int getDeadline() {
		return _deadline;
	}

	@Override
	public String format(PackagePrivateWarehouseManagerWrapper warehouseManager) {
		var partner = getPartner();
		var product = getProduct();
		var baseString = new StringBuilder(String.format("VENDA|%d|%s|%s|%d|%d|0.0|%d", getId(), partner.getId(),
				product.getId(), getAmount(), Math.round(getTotalPrice()), getDeadline()));

		if (_paidAmount.isPresent()) {
			baseString.append(String.format("|%d", getPaymentDate()));
		}

		return baseString.toString();
	}

	@Override
	boolean isPaid() {
		return _paidAmount.isPresent();
	}

}
