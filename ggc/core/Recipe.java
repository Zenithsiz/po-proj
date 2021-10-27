package ggc.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/// Recipe for a derived product
public class Recipe implements Serializable {
	/// Serial number for serialization.
	private static final long serialVersionUID = 2021_10_27_05_55L;

	/// All ingredient quantities
	private Map<Product, Integer> _productQuantities;

	/// Cost factor
	private float _costFactor;

	// Note: Package private to ensure we don't construct it outside of `core`.
	Recipe(Map<Product, Integer> productQuantities, float costFactor) {
		_productQuantities = new HashMap<>(productQuantities);
		_costFactor = costFactor;
	}

	/// Returns a stream of all products in this recipe
	public Stream<Product> products() {
		return _productQuantities.keySet().stream();
	}
}
