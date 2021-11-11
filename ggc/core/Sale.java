package ggc.core;

/**
 * A sale by a partner.
 * 
 * This abstract transactions in which the warehouse receives products from a partner.
 */
public abstract class Sale extends Transaction {
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
	 */
	// Note: Package private to ensure we don't construct it outside of `core`.
	Sale(int id, Product product, Partner partner, int quantity, double totalPrice) {
		super(id, product, partner, quantity, totalPrice);
	}

	/**
	 * Retrieves whether or not this sale is already paid
	 * 
	 * @return If this sale is paid
	 */
	abstract boolean isPaid();
}
