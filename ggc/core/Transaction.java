package ggc.core;

import java.io.Serializable;

/**
 * A transaction between the warehouse and a partner over a product
 * 
 * This class abstracts the concept of a transaction involving a quantity of product being exchanged between the
 * warehouse and a partner.
 */
public abstract class Transaction implements Serializable, WarehouseFormattable {
	/** Serial number for serialization. */
	private static final long serialVersionUID = 2021_10_27_03_16L;

	/**
	 * Transaction id. This id will be managed by the warehouse, being incremented on each new transaction created.
	 * <p>
	 * Note that we don't store the current transaction id in this class, as there may exist multiple warehouses
	 * simultaneously, each with their own transaction counter.
	 * </p>
	 */
	private int _id;

	/** The product being exchanged in this transaction */
	private Product _product;

	/** Quantity of product being exchanged in this transaction */
	private int _quantity;

	/** The partner associated with this transaction */
	private Partner _partner;

	/**
	 * Creates a new transaction.
	 * 
	 * @param id
	 *            The id of this transaction
	 * @param product
	 *            The product being exchanged
	 * @param partner
	 *            The partner for this exchange
	 * @param quantity
	 *            The quantity of product being exchanged
	 */
	// Note: Package private to ensure we don't construct it outside of `core`.
	Transaction(int id, Product product, Partner partner, int quantity) {
		_id = id;
		_product = product;
		_partner = partner;
		_quantity = quantity;
	}

	/**
	 * Retrieves this transaction's id
	 * 
	 * @return The id of this transaction
	 */
	int getId() {
		return _id;
	}

	/**
	 * Retrieves this transaction's product
	 * 
	 * @return The product of this transaction
	 */
	Product getProduct() {
		return _product;
	}

	/**
	 * Retrieves this transaction's partner
	 * 
	 * @return The partner of this transaction
	 */
	Partner getPartner() {
		return _partner;
	}

	/**
	 * Retrieves this transaction's product quantity
	 * 
	 * @return The quantity of product exchanged in this transaction
	 */
	int getQuantity() {
		return _quantity;
	}

	/**
	 * Retrieves if this transaction is paid
	 * 
	 * @return If paid
	 */
	abstract boolean isPaid();

	/**
	 * Pays this transaction if it isn't paid already
	 * 
	 * @param date
	 *            The date to pay the transaction at.
	 * @return The amount paid.
	 */
	// TODO:
}
