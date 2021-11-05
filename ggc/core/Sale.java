package ggc.core;

/// A sale by a partner
public abstract class Sale extends Transaction {

	// Note: Package private to ensure we don't construct it outside of `core`.
	Sale(int id, int paymentDate, Product product, Partner partner, int quantity, double totalPrice) {
		super(id, paymentDate, product, partner, quantity, totalPrice);
	}

	/// Returns if this sale is paid
	abstract boolean isPaid();
}
