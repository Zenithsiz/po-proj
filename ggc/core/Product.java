package ggc.core;

import java.io.Serializable;
import java.util.stream.Stream;

import ggc.core.util.StreamIterator;

/// Base/Simple product
public class Product implements Serializable, WarehouseFormattable {
	/// Serial number for serialization.
	private static final long serialVersionUID = 2021_10_27_01_17L;

	/// Product id
	private String _id;

	// Note: Package private to ensure we don't construct it outside of `core`.
	Product(String id) {
		_id = id;
	}

	/// Returns this product's id
	String getId() {
		return _id;
	}

	/// Returns extra fields to format the product with
	protected Stream<String> extraFormatFields() {
		return Stream.empty();
	}

	public String format(PackagePrivateWarehouseManagerWrapper warehouseManager) {
		// Get the our max price and total quantity
		// Note: If no batches exist, there is no max price, and so we'll return 0
		double maxPrice = warehouseManager.getWarehouseManager().productMaxPrice(this).orElse(0.0);
		int quantity = warehouseManager.getWarehouseManager().productTotalQuantity(this);

		// Create the base string
		StringBuilder repr = new StringBuilder(String.format("%s|%.0f|%d", _id, maxPrice, quantity));

		// Then add any extra fields we may have
		for (var field : StreamIterator.streamIt(extraFormatFields())) {
			repr.append("|");
			repr.append(field);
		}

		return repr.toString();
	}
}
