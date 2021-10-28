package ggc.core;

import java.io.Serializable;
import java.util.stream.Stream;

/// Base/Simple product
public class Product implements Serializable {
	/// Serial number for serialization.
	private static final long serialVersionUID = 2021_10_27_01_17L;

	/// Product id
	private String _id;

	// Note: Package private to ensure we don't construct it outside of `core`.
	Product(String id) {
		_id = id;
	}

	/// Returns this product's id
	public String getId() {
		return _id;
	}

	/// Returns extra fields to format the product with
	Stream<String> extraFormatFields() {
		return Stream.empty();
	}
}
