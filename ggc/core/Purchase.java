package ggc.core;

/// A purchase by a partner
public class Purchase extends Transaction {

	// Note: Package private to ensure we don't construct it outside of `core`.
	Purchase(int id, int paymentDate, Product product, Partner partner, int quantity, double totalPrice) {
		super(id, paymentDate, product, partner, quantity, totalPrice);
	}

	@Override
	public String format(ConstWarehouse warehouse) {
		var partner = getPartner();
		var product = getProduct();
		return String.format("COMPRA|%d|%s|%s|%d|%f|%d", getId(), partner.getId(), product.getId(), getAmount(),
				getTotalPrice(), getPaymentDate());
	}

}
