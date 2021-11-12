package ggc.core;

/**
 * A purchase by a partner
 * 
 * This represents a transaction in which the warehouse purchases products from a partner
 */
public class Purchase extends Transaction {
	/** Payment date */
	private int _paymentDate;

	/** Total cost */
	private double _totalCost;

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
	 * @param totalCost
	 *            The total cost of this purchase
	 */
	// Note: Package private to ensure we don't construct it outside of `core`.
	Purchase(int id, int paymentDate, Product product, Partner partner, int quantity, double totalCost) {
		super(id, product, partner, quantity);
		_paymentDate = paymentDate;
		_totalCost = totalCost;
	}

	/**
	 * Retrieves this transaction's payment date
	 * 
	 * @return The payment date of this transaction
	 */
	int getPaymentDate() {
		return _paymentDate;
	}

	/**
	 * Retrieves this transaction's total cost
	 * 
	 * @return The total cost of this transaction
	 */
	double getTotalCost() {
		return _totalCost;
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		var partner = getPartner();
		var product = getProduct();
		return String.format("COMPRA|%d|%s|%s|%d|%d|%d", getId(), partner.getId(), product.getId(), getQuantity(),
				Math.round(_totalCost), getPaymentDate());
	}
}
