package ggc.core;

/**
 * A purchase by a partner
 * 
 * This represents a transaction in which the warehouse purchases products from a partner
 */
public class Purchase extends Transaction {
	/** Payment date */
	private int _paymentDate;

	/**
	 * Creates a new purchase
	 * 
	 * @param id
	 *            The id of the transaction
	 * @param paymentDate
	 *            The date this transaction was paid
	 * @param product
	 *            The product purchased from the partner
	 * @param partner
	 *            The partner of this transaction
	 * @param quantity
	 *            The quantity of product purchased from the partner
	 * @param totalPrice
	 *            The total price of this purchase
	 */
	// Note: Package private to ensure we don't construct it outside of `core`.
	Purchase(int id, int paymentDate, Product product, Partner partner, int quantity, double totalPrice) {
		super(id, product, partner, quantity, totalPrice);
		_paymentDate = paymentDate;
	}

	/**
	 * Retrieves this transaction's payment date
	 * 
	 * @return The payment date of this transaction
	 */
	int getPaymentDate() {
		return _paymentDate;
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		var partner = getPartner();
		var product = getProduct();
		return String.format("COMPRA|%d|%s|%s|%d|%d|%d", getId(), partner.getId(), product.getId(), getQuantity(),
				Math.round(getTotalPrice()), getPaymentDate());
	}
}
