package ggc.core;

/// Derived product, able to be created via a recipe of other products.
public class DerivedProduct extends Product {
	/// Recipe
	private Recipe _recipe;

	// Note: Package private to ensure we don't construct it outside of `core`.
	DerivedProduct(String id, Recipe recipe) {
		super(id);
		_recipe = recipe;
	}

	/// Returns the recipe used by this derived product
	public Recipe getRecipe() {
		return _recipe;
	}
}
