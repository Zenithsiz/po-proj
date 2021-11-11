package ggc.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Stream;
import static ggc.core.util.StreamIterator.streamIt;

/**
 * A product.
 * 
 * A product is the unit that is stored and traded in the warehouse.
 * 
 * It may be a simple product, which cannot be manufactured, or a derived product, which may be derived according to a
 * recipe.
 */
public class Product implements Serializable, WarehouseFormattable {
	/** Serial number for serialization. */
	private static final long serialVersionUID = 2021_10_27_01_17L;

	/** Id of this product */
	private String _id;

	/** Min price this product has been at */
	// Note: transient because we can't [de]serialize an optional
	private transient OptionalDouble _minPrice = OptionalDouble.empty();

	/** Max price this product has been at */
	// Note: transient because we can't [de]serialize an optional
	private transient OptionalDouble _maxPrice = OptionalDouble.empty();

	/**
	 * Override for serialization to write our transient fields
	 * 
	 * @param out
	 *            The stream to write to
	 * @throws IOException
	 *             If unable to write
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeBoolean(_minPrice.isPresent());
		if (_minPrice.isPresent()) {
			out.writeDouble(_minPrice.getAsDouble());
		}

		out.writeBoolean(_maxPrice.isPresent());
		if (_maxPrice.isPresent()) {
			out.writeDouble(_maxPrice.getAsDouble());
		}
	}

	/**
	 * Override for deserialization to read our transient fields
	 * 
	 * @param in
	 *            The stream to read from
	 * @throws IOException
	 *             If unable to read
	 * @throws ClassNotFoundException
	 *             If a class wasn't found while loading
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		_minPrice = in.readBoolean() ? OptionalDouble.of(in.readDouble()) : OptionalDouble.empty();
		_maxPrice = in.readBoolean() ? OptionalDouble.of(in.readDouble()) : OptionalDouble.empty();
	}

	/**
	 * Creates a new simple product
	 * 
	 * @param id
	 *            The id of this product
	 */
	// Note: Package private to ensure we don't construct it outside of `core`.
	Product(String id) {
		_id = id;
	}

	/**
	 * Retrieves this product's id
	 * 
	 * @return The id of this product
	 */
	String getId() {
		return _id;
	}

	/**
	 * Retrieves this product's min price
	 * 
	 * @return The min price of this price
	 */
	OptionalDouble getMinPrice() {
		return _minPrice;
	}

	/**
	 * Sets the min price of this product
	 * 
	 * @param minPrice
	 *            The new minimum price
	 */
	void setMinPrice(double minPrice) {
		assert _minPrice.isEmpty() || minPrice <= _minPrice.getAsDouble();
		_minPrice = OptionalDouble.of(minPrice);
	}

	/**
	 * Retrieves this product's max price
	 * 
	 * @return The max price of this price
	 */
	OptionalDouble getMaxPrice() {
		return _maxPrice;
	}

	/**
	 * Sets the max price of this product
	 * 
	 * @param maxPrice
	 *            The new maximum price
	 */
	void setMaxPrice(double maxPrice) {
		assert _maxPrice.isEmpty() || maxPrice >= _maxPrice.getAsDouble();
		_maxPrice = OptionalDouble.of(maxPrice);
	}

	/**
	 * Attempts to downcast this product to a derived one.
	 * 
	 * @return A derived product, if derived, else empty.
	 */
	Optional<DerivedProduct> getAsDerived() {
		return Optional.empty();
	}

	/**
	 * Retrieves this product's payment factor
	 * <p>
	 * The payment factor dictates how long a partner has between each time period for the payment of sales. It is 5 for
	 * simple products and 3 for derived.
	 * </p>
	 * 
	 * @return The payment factor for this product
	 */
	public int getPaymentFactor() {
		return 5;
	}

	/**
	 * Returns extra fields to format using {@link #format(WarehouseManager)}.
	 * 
	 * @param warehouseManager
	 *            The warehouse manager to format with
	 * 
	 * @return A stream of all extra fields to append. Each one will be prepended with `|`.
	 */
	protected Stream<String> extraFormatFields(WarehouseManager warehouseManager) {
		return Stream.empty();
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		// Get the our max price and total quantity
		int quantity = warehouseManager.productTotalQuantity(this);

		// Create the base string
		StringBuilder repr = new StringBuilder(String.format("%s|%.0f|%d", _id, _maxPrice.orElse(0.0), quantity));

		// Then add any extra fields we may have
		for (var field : streamIt(extraFormatFields(warehouseManager))) {
			repr.append("|");
			repr.append(field);
		}

		return repr.toString();
	}
}
