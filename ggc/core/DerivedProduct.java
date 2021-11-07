package ggc.core;

import java.util.stream.Stream;

/// Derived product, able to be created via a recipe of other products.
public class DerivedProduct extends Product {
	/// Recipe
	private Recipe _recipe;

	/// Cost factor
	private double _costFactor;

	// Note: Package private to ensure we don't construct it outside of `core`.
	DerivedProduct(String id, Recipe recipe, double costFactor) {
		super(id);
		_recipe = recipe;
		_costFactor = costFactor;
	}

	/// Returns the recipe used by this derived product
	Recipe getRecipe() {
		return _recipe;
	}

	/// Returns the cost factor used by this derived product
	double getCostFactor() {
		return _costFactor;
	}

	/// Returns extra fields to format the product with
	@Override
	protected Stream<String> extraFormatFields() {
		// TODO: The tests don't have the cost factor, but the standard says it should be there, which one to do?
		//return Stream.of(Double.toString(_costFactor), _recipe.toString());
		return Stream.of(_recipe.toString());
	}
}
