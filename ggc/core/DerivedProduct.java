package ggc.core;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Derived product
 * 
 * A product which is derived from other products
 */
public class DerivedProduct extends Product {
	/** The recipe to create this product */
	private Recipe _recipe;

	/** Cost factor when creating this product from it's recipe */
	private double _costFactor;

	/**
	 * Creates a new derived product
	 * 
	 * @param id
	 *            The product id
	 * @param recipe
	 *            The recipe for this product
	 * @param costFactor
	 *            The cost factor when manufacturing this product
	 */
	// Note: Package private to ensure we don't construct it outside of `core`.
	DerivedProduct(String id, Recipe recipe, double costFactor) {
		super(id);
		_recipe = recipe;
		_costFactor = costFactor;
	}

	/**
	 * Retrieves this product's recipe
	 * 
	 * @return The recipe to manufacture this product
	 */
	Recipe getRecipe() {
		return _recipe;
	}

	/**
	 * Retrieves this product's cost factor
	 * 
	 * @return The cost factor of this product
	 */
	double getCostFactor() {
		return _costFactor;
	}

	@Override
	Optional<DerivedProduct> getAsDerived() {
		return Optional.of(this);
	}

	@Override
	public int getPaymentFactor() {
		return 3;
	}

	@Override
	protected Stream<String> extraFormatFields(WarehouseManager warehouseManager) {
		return Stream.of(_recipe.format(warehouseManager));
	}
}
