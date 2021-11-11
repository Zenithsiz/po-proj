package ggc.core;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ggc.core.exception.UnknownProductIdException;
import ggc.core.util.Pair;
import static ggc.core.util.StreamIterator.streamIt;

/** Recipe for a derived product */
public class Recipe implements Serializable, WarehouseFormattable {
	/** Serial number for serialization. */
	private static final long serialVersionUID = 2021_10_27_05_55L;

	/** All ingredient quantities */
	private Map<Product, Integer> _productQuantities;

	/**
	 * Creates a new recipe from a stream of product Ids
	 * 
	 * @param productIdQuantities
	 *            The quantities for each product id
	 * @param productGetter
	 *            A getter for products given their IDs.
	 * @return The recipe
	 * @throws UnknownProductIdException
	 *             If a product didn't exist
	 */
	// Note: Package private to ensure we don't construct it outside of `core`.
	static Recipe fromProductIds(Stream<Pair<String, Integer>> productIdQuantities,
			Function<String, Optional<Product>> productGetter) throws UnknownProductIdException {
		var productQuantities = new LinkedHashMap<Product, Integer>();
		for (var pair : streamIt(productIdQuantities)) {
			String recipeProductId = pair.getLhs();
			var product = productGetter.apply(recipeProductId);
			if (product.isEmpty()) {
				throw new UnknownProductIdException(recipeProductId);
			}
			productQuantities.put(product.get(), pair.getRhs());
		}

		return new Recipe(productQuantities);
	}

	/**
	 * Creates a recipe from it's product quantities
	 * 
	 * @param productQuantities
	 *            The product quantities
	 */
	private Recipe(Map<Product, Integer> productQuantities) {
		_productQuantities = productQuantities;
	}

	/**
	 * Retrieves all product quantities
	 * 
	 * @return The product quantities of this recipe
	 */
	Stream<Pair<Product, Integer>> getProductQuantities() {
		return _productQuantities.entrySet().stream().map(Pair::fromMapEntry);
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		return _productQuantities.entrySet().stream()
				.map(entry -> String.format("%s:%d", entry.getKey().getId(), entry.getValue()))
				.collect(Collectors.joining("#"));
	}
}
