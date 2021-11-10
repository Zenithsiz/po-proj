package ggc.core;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

import ggc.core.util.StreamIterator;

/// Base/Simple product
public class Product implements Serializable, WarehouseFormattable {
	/// Serial number for serialization.
	private static final long serialVersionUID = 2021_10_27_01_17L;

	/// Product id
	private String _id;

	/// Max price seen of the product
	private double _maxPrice = 0;

	// Note: Package private to ensure we don't construct it outside of `core`.
	Product(String id) {
		_id = id;
	}

	/// Returns this product's id
	String getId() {
		return _id;
	}

	/// Returns this product's max price
	double getMaxPrice() {
		return _maxPrice;
	}

	/// Sets the max price of the product
	void setMaxPrice(double maxPrice) {
		assert maxPrice >= _maxPrice;
		_maxPrice = maxPrice;
	}

	/// Returns this product as a derived product, if it is one
	Optional<DerivedProduct> getAsDerived() {
		return Optional.empty();
	}

	/// Returns the payment factor for this product
	public int getPaymentFactor() {
		return 5;
	}

	/// Returns extra fields to format the product with
	protected Stream<String> extraFormatFields() {
		return Stream.empty();
	}

	public String format(WarehouseManager warehouseManager) {
		// Get the our max price and total quantity
		int quantity = warehouseManager.productTotalQuantity(this);

		// Create the base string
		StringBuilder repr = new StringBuilder(String.format("%s|%.0f|%d", _id, _maxPrice, quantity));

		// Then add any extra fields we may have
		for (var field : StreamIterator.streamIt(extraFormatFields())) {
			repr.append("|");
			repr.append(field);
		}

		return repr.toString();
	}
}
