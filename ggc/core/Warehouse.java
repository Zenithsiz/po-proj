package ggc.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.io.IOException;
import ggc.core.exception.BadEntryException;
import ggc.core.exception.ParsingException;
import ggc.core.exception.UnknownPartnerIdException;
import ggc.core.exception.UnknownProductIdException;
import ggc.core.util.Pair;

/**
 * Class Warehouse implements a warehouse.
 */
public class Warehouse implements Serializable {

	/** Serial number for serialization. */
	private static final long serialVersionUID = 202109192006L;

	/// Current date
	private int _date = 0;

	/// Next transaction id
	private int _nextTransactionId = 0;

	/// All transactions
	private List<Transaction> _transactions = new ArrayList<>();

	/// All partners
	private Map<String, Partner> _partners = new HashMap<>();

	/// All products
	private Map<String, Product> _products = new HashMap<>();

	/// All bundles
	private List<Bundle> _bundles = new ArrayList<>();

	/**
	 * @param fileName
	 *            filename to be loaded.
	 * @throws IOException
	 * @throws BadEntryException
	 */
	void importFile(String fileName) throws IOException, BadEntryException, ParsingException {
		// Create a parser and visit all lines
		var parser = new Parser(fileName);
		parser.visit(new ImportParserVisitor(this));
	}

	/// Visitor for importing a file
	private class ImportParserVisitor implements ParserVisitor {
		/// The warehouse we're importing onto
		private Warehouse _warehouse;

		ImportParserVisitor(Warehouse warehouse) {
			_warehouse = warehouse;
		}

		@Override
		public void visitPartner(Partner partner) {
			_warehouse._partners.put(partner.getId(), partner);
		}

		@Override
		public void visitBundle(String productId, String partnerId, int quantity, double unitPrice)
				throws UnknownPartnerIdException {
			// Get an existing product, or register it
			Product product = _warehouse._products.computeIfAbsent(productId, _key -> new Product(productId));

			// Then get the partner
			Partner partner = Optional.ofNullable(_warehouse._partners.get(partnerId))
					.orElseThrow(() -> new UnknownPartnerIdException(partnerId));

			// And create a new bundle
			Bundle bundle = new Bundle(product, quantity, partner, unitPrice);
			_warehouse._bundles.add(bundle);
		}

		@Override
		public void visitDerivedBundle(String productId, String partnerId, int quantity, double unitPrice,
				double costFactor, Map<String, Integer> recipeProducts)
				throws UnknownPartnerIdException, UnknownProductIdException {
			// If any of the recipe products don't exist, throw
			// Note: We do this regardless if the product exist to ensure that the user specified
			// products which already exist
			for (var entry : recipeProducts.entrySet()) {
				String recipeProductId = entry.getKey();
				if (!_warehouse._products.containsKey(recipeProductId)) {
					throw new UnknownProductIdException(recipeProductId);
				}
			}

			// Get an existing product, or register it
			Product product = _warehouse._products.computeIfAbsent(productId, _key -> {
				// Get all product quantities
				// Note: We've already checked all recipe products exist, so this contains no `null`s.
				Map<Product, Integer> productQuantities = recipeProducts.entrySet().stream().map(entry -> {
					Product recipeProduct = _warehouse._products.get(entry.getKey());
					return new Pair<>(recipeProduct, entry.getValue());
				}).collect(Pair.toMapCollector());

				Recipe recipe = new Recipe(productQuantities);
				return new DerivedProduct(productId, recipe, costFactor);
			});

			// Then get the partner
			Partner partner = Optional.ofNullable(_warehouse._partners.get(partnerId))
					.orElseThrow(() -> new UnknownPartnerIdException(partnerId));

			// And create a new bundle
			Bundle bundle = new Bundle(product, quantity, partner, unitPrice);
			_warehouse._bundles.add(bundle);
		}

	}

	/// Returns the current date
	public int getDate() {
		return _date;
	}

	/// Advances the current date
	public void advanceDate(int offset) {
		_date += offset;
	}

	/// Returns a stream over all products
	public Stream<Product> getProducts() {
		return _products.values().stream();
	}

	/// Returns the max price of a product
	public Optional<Double> productMaxPrice(Product product) {
		return _bundles.stream().filter(bundle -> bundle.getProduct() == product).max(Bundle::compareByUnitPrice)
				.map(Bundle::getUnitPrice);
	}

	/// Returns the total quantity of a product
	public int productTotalQuantity(Product product) {
		return _bundles.stream().filter(bundle -> bundle.getProduct() == product).mapToInt(Bundle::getQuantity).sum();
	}
}
