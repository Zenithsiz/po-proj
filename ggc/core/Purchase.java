package ggc.core;

/// A purchase by a partner
public class Purchase extends Transaction {
	/// Payment date
	private int _paymentDate;

	// Note: Package private to ensure we don't construct it outside of `core`.
	Purchase(int id, int paymentDate, Product product, Partner partner, int quantity, double totalPrice) {
		super(id, product, partner, quantity, totalPrice);
		_paymentDate = paymentDate;
	}

	/// Returns this transaction's payment date
	int getPaymentDate() {
		return _paymentDate;
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		var partner = getPartner();
		var product = getProduct();
		return String.format("COMPRA|%d|%s|%s|%d|%.0f|%d", getId(), partner.getId(), product.getId(), getQuantity(),
				getTotalPrice(), getPaymentDate());
	}
}
