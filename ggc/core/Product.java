package ggc.core;

import java.io.Serializable;

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
	// Note: Public so we can get IDs in `app`.
	// TODO: Check if it needs to be public to get IDs or if we just need to use `toString`?
	public String getId() {
		return _id;
	}
}
