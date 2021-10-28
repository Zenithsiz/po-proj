package ggc.core;

import java.util.stream.Stream;

/// Derived product, able to be created via a recipe of other products.
public class DerivedProduct extends Product {
	/// Recipe
	private Recipe _recipe;

	/// Cost factor
	private float _costFactor;

	// Note: Package private to ensure we don't construct it outside of `core`.
	DerivedProduct(String id, Recipe recipe, float costFactor) {
		super(id);
		_recipe = recipe;
		_costFactor = costFactor;
	}

	/// Returns the recipe used by this derived product
	public Recipe getRecipe() {
		return _recipe;
	}

	/// Returns the cost factor used by this derived product
	public float getCostFactor() {
		return _costFactor;
	}

	/// Returns extra fields to format the product with
	@Override
	Stream<String> extraFormatFields() {
		return Stream.of(Float.toString(_costFactor), _recipe.toString());
	}
}
