package ggc.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import ggc.core.util.Pair;

/** A breakdown transaction */
public class BreakdownTransaction extends Transaction {
	/** The date this transaction took place */
	private int _date;

	/** The base cost of the breakdown, possibly negative */
	private double _baseCost;

	/** All of the products we created in this transaction, along with their total price. */
	private List<Pair<Product, Pair<Integer, Double>>> _productsCreated;

	/**
	 * Creates a new breakdown transaction.
	 * 
	 * @param id
	 *            The id of this transaction
	 * @param date
	 *            the date this transaction was created
	 * @param product
	 *            The product that was broken down.
	 * @param partner
	 *            The partner that requested the breakdown
	 * @param quantity
	 *            The quantity of product that was broken down
	 * @param baseCost
	 *            The base cost of the transaction
	 * @param productsCreated
	 *            A map of all products created in this break down, with their quantities and total prices.
	 */
	BreakdownTransaction(int id, int date, Product product, Partner partner, int quantity, double baseCost,
			List<Pair<Product, Pair<Integer, Double>>> productsCreated) {
		super(id, product, partner, quantity);
		_date = date;
		_baseCost = baseCost;
		_productsCreated = new ArrayList<>(productsCreated);
	}

	/**
	 * Retrieves the payment date of this transaction
	 * 
	 * @return The date this transaction was created
	 */
	int getDate() {
		return _date;
	}

	/**
	 * Retrieves the base cost of this transaction
	 * 
	 * @return The base cost of this transaction, possibly negative
	 */
	double getBaseCost() {
		return _baseCost;
	}

	/**
	 * Returns the paid cost of this transaction
	 * 
	 * @return The paid cost of this transaction, always positive
	 */
	double getPaidCost() {
		// Note: If the base cost was negative, the partner doesn't
		//       pay anything
		return Math.max(0, _baseCost);
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		var partner = getPartner();
		var product = getProduct();
		var baseString = new StringBuilder(String.format("DESAGREGA????O|%d|%s|%s|%d|%d|%d|%d|", getId(), partner.getId(),
				product.getId(), getQuantity(), Math.round(getBaseCost()), Math.round(getPaidCost()), _date));

		String components = _productsCreated.stream().map(pair -> String.format("%s:%d:%d", pair.getLhs().getId(),
				pair.getRhs().getLhs(), Math.round(pair.getRhs().getRhs()))).collect(Collectors.joining("#"));

		baseString.append(components);

		return baseString.toString();
	}
}
