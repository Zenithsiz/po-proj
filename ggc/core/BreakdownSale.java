package ggc.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ggc.core.util.Pair;

/** A sale from a breakdown */
public class BreakdownSale extends Sale {
	/** The date this sale took place */
	private int _date;

	/** TODO: Once figured out, comment this */
	private double _differentialTotalPrice;

	/** All of the products we created in this sale, along with their total price. */
	private List<Pair<Product, Pair<Integer, Double>>> _productsCreated;

	/**
	 * Creates a new breakdown sale.
	 * 
	 * @param id
	 *            The id of this sale
	 * @param date
	 *            the date this sale was created
	 * @param product
	 *            The product that was broken down.
	 * @param partner
	 *            The partner that requested the breakdown
	 * @param quantity
	 *            The quantity of product that was broken down
	 * @param totalPrice
	 *            TODO: Once figured out, comment this
	 * @param productsCreated
	 *            A map of all products created in this break down, with their quantities and total prices.
	 */
	BreakdownSale(int id, int date, Product product, Partner partner, int quantity, double totalPrice,
			List<Pair<Product, Pair<Integer, Double>>> productsCreated) {
		super(id, product, partner, quantity, Math.abs(totalPrice));
		_date = date;
		_differentialTotalPrice = totalPrice;
		_productsCreated = new ArrayList<>(productsCreated);
	}

	/**
	 * Retrieves the payment date of this sale
	 * 
	 * @return The date this sale was created
	 */
	int getDate() {
		return _date;
	}

	/**
	 * TODO:
	 * 
	 * @return TODO:
	 */
	double getDifferentialTotalPrice() {
		return _differentialTotalPrice;
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		var partner = getPartner();
		var product = getProduct();
		var baseString = new StringBuilder(
				String.format("DESAGREGAÇÃO|%s|%s|%s|%d|%d|%d|%d|", getId(), partner.getId(), product.getId(),
						getQuantity(), Math.round(_differentialTotalPrice), Math.round(getTotalPrice()), _date));

		String components = _productsCreated.stream().map(pair -> String.format("%s:%d:%d", pair.getLhs().getId(),
				pair.getRhs().getLhs(), Math.round(pair.getRhs().getRhs()))).collect(Collectors.joining("#"));

		baseString.append(components);

		return baseString.toString();
	}

	@Override
	boolean isPaid() {
		return true;
	}
}
