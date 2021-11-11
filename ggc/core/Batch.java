package ggc.core;

import java.io.Serializable;
import java.util.Objects;

/**
 * <h2>Product batch.</h2>
 * 
 * Stores a quantity of products, supplied by a partner at a given unit price. <br>
 * It is the only source of "storage" for products within the warehouse. <br>
 * When it's quantity reaches 0, it is removed from the warehouse.
 */
public class Batch implements Serializable, WarehouseFormattable {
	/** Serial number for serialization. */
	private static final long serialVersionUID = 2021_10_27_05_54L;

	/** The product this batch stores */
	private Product _product;

	/**
	 * The quantity of product in this batch.
	 * 
	 * Always <code> > 0 </code>
	 */
	private int _quantity;

	/** The partner that supplies this batch */
	private Partner _partner;

	/** Per-unit price of the products in this batch */
	private double _unitPrice;

	/**
	 * Constructs a new batch
	 * 
	 * @param product
	 *            The product of this batch
	 * @param quantity
	 *            The quantity of product in this batch
	 * @param partner
	 *            The partner of this batch
	 * @param unitPrice
	 *            the unit price of each product in this batch
	 */
	// Note: Package private to ensure we don't construct it outside of `core`.
	Batch(Product product, Partner partner, int quantity, double unitPrice) {
		assert quantity >= 0;
		assert unitPrice >= 0.0;

		_product = Objects.requireNonNull(product);
		_partner = Objects.requireNonNull(partner);
		_quantity = quantity;
		_unitPrice = unitPrice;
	}

	/**
	 * Retrieves this batch's product
	 * 
	 * @return The product this batch supplies
	 */
	Product getProduct() {
		return _product;
	}

	/**
	 * Retrieves this batch's partner
	 * 
	 * @return The partner supplying this batch
	 */
	Partner getPartner() {
		return _partner;
	}

	/**
	 * Retrieves this batch's unit price
	 * 
	 * @return The per-unit price of the products in this batch
	 */
	double getUnitPrice() {
		return _unitPrice;
	}

	/**
	 * Retrieves this batch's quantity
	 * 
	 * @return The quantity of product in this batch
	 */
	int getQuantity() {
		return _quantity;
	}

	/**
	 * Takes `quantity` products away from this batch
	 * 
	 * @param quantity
	 *            The quantity of product to take
	 */
	public void takeQuantity(int quantity) {
		_quantity -= quantity;
		assert _quantity >= 0;
	}

	/**
	 * Compares two batches by unit price
	 * 
	 * @param lhs
	 *            The first batch to compare
	 * @param rhs
	 *            The second batch to compare
	 * @return The result of <code>Double.compare</code> of both arguments
	 */
	static int compareByUnitPrice(Batch lhs, Batch rhs) {
		return Double.compare(lhs._unitPrice, rhs._unitPrice);
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		return String.format("%s|%s|%d|%d", _product.getId(), _partner.getId(), Math.round(_unitPrice), _quantity);
	}
}
