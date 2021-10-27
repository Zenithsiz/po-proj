package ggc.core;

import java.util.Map;

/// Recipe for a derived product
public class Recipe {
	/// All ingredient quantities
	private Map<String, Integer> _productQuantities;

	/// Cost factor
	private float _costFactor;

	// Note: Package private to ensure we don't construct it outside of `core`.
	Recipe(Map<String, Integer> _productQuantities, float _costFactor) {
		this._productQuantities = _productQuantities;
		this._costFactor = _costFactor;
	}
}
