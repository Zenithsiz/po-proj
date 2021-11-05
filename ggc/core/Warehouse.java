package ggc.core;

import java.io.Serializable;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import ggc.core.exception.BadEntryException;
import ggc.core.exception.ParsingException;
import ggc.core.exception.UnknownPartnerIdException;
import ggc.core.exception.UnknownProductIdException;
import ggc.core.util.Pair;

/**
 * Class Warehouse implements a warehouse.
 */
// Note: Package private because we don't need it outside of core
class Warehouse implements Serializable {

	/** Serial number for serialization. */
	private static final long serialVersionUID = 2021_10_28_18_50L;

	/// Collator for all strings
	private static Collator collator;

	static {
		collator = Collator.getInstance();
		// Note: Secondary so we consider accents
		collator.setStrength(Collator.SECONDARY);
		collator.setDecomposition(Collator.FULL_DECOMPOSITION);
	}

	/// Current date
	private int _date;

	/// Next transaction id
	private int _nextTransactionId;

	/// All transactions
	private List<Transaction> _transactions = new ArrayList<>();

	/// All partners
	private Map<CollationKey, Partner> _partners = new HashMap<>();

	/// All products
	private Map<CollationKey, Product> _products = new HashMap<>();

	/// All batches
	private List<Batch> _batches = new ArrayList<>();

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
			_warehouse._partners.put(getCollationKey(partner.getId()), partner);
		}

		@Override
		public void visitBatch(String productId, String partnerId, int quantity, double unitPrice)
				throws UnknownPartnerIdException {
			// Get an existing product, or register it
			Product product = _warehouse._products.computeIfAbsent(getCollationKey(productId),
					_key -> new Product(productId));

			// Then get the partner
			Partner partner = Optional.ofNullable(_warehouse._partners.get(getCollationKey(partnerId)))
					.orElseThrow(() -> new UnknownPartnerIdException(partnerId));

			// And create a new batch
			Batch batch = new Batch(product, quantity, partner, unitPrice);
			_warehouse._batches.add(batch);
		}

		@Override
		public void visitDerivedBatch(String productId, String partnerId, int quantity, double unitPrice,
				double costFactor, Map<String, Integer> recipeProducts)
				throws UnknownPartnerIdException, UnknownProductIdException {
			// If any of the recipe products don't exist, throw
			// Note: We do this regardless if the product exist to ensure that the user specified
			// products which already exist
			for (var entry : recipeProducts.entrySet()) {
				String recipeProductId = entry.getKey();
				if (!_warehouse._products.containsKey(getCollationKey(recipeProductId))) {
					throw new UnknownProductIdException(recipeProductId);
				}
			}

			// Get an existing product, or register it
			Product product = _warehouse._products.computeIfAbsent(getCollationKey(productId), _key -> {
				// Get all product quantities
				// Note: We've already checked all recipe products exist, so this contains no `null`s.
				Map<Product, Integer> productQuantities = recipeProducts.entrySet().stream() //
						.map(entry -> {
							String recipeProductId = entry.getKey();
							Product recipeProduct = _warehouse._products.get(getCollationKey(recipeProductId));
							return new Pair<>(recipeProduct, entry.getValue());
						}).collect(Pair.toMapCollector());

				Recipe recipe = new Recipe(productQuantities);
				return new DerivedProduct(productId, recipe, costFactor);
			});

			// Then get the partner
			Partner partner = Optional.ofNullable(_warehouse._partners.get(getCollationKey(partnerId)))
					.orElseThrow(() -> new UnknownPartnerIdException(partnerId));

			// And create a new batch
			Batch batch = new Batch(product, quantity, partner, unitPrice);
			_warehouse._batches.add(batch);
		}

	}

	/// Returns a collation key given a string with the class collation
	private static CollationKey getCollationKey(String key) {
		return Warehouse.collator.getCollationKey(key);
	}

	// Note: We need to override the saving and loading because we use `RuleBasedCollationKey`s,
	// and either way, the hashmaps could be saved as lists, the keys are redundant.
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(_date);
		out.writeObject(_nextTransactionId);
		out.writeObject(_transactions);
		out.writeObject(new ArrayList<>(_partners.values()));
		out.writeObject(new ArrayList<>(_products.values()));
		out.writeObject(_batches);
	}

	@SuppressWarnings("unchecked") // We're doing a raw cast without being able to properly check the underlying class
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		var date = (Integer) in.readObject();
		var nextTransactionId = (Integer) in.readObject();
		var transactions = (List<Transaction>) in.readObject();
		var partners = (List<Partner>) in.readObject();
		var products = (List<Product>) in.readObject();
		var batches = (List<Batch>) in.readObject();

		_date = date;
		_nextTransactionId = nextTransactionId;
		_transactions = transactions;
		_partners = partners.stream()
				.collect(Collectors.toMap(partner -> getCollationKey(partner.getId()), partner -> partner));
		_products = products.stream()
				.collect(Collectors.toMap(product -> getCollationKey(product.getId()), product -> product));
		_batches = batches;
	}

	/// Returns the current date
	int getDate() {
		return _date;
	}

	/// Advances the current date
	void advanceDate(int offset) {
		_date += offset;
	}

	/// Returns a stream over all products
	Stream<Product> getProducts() {
		return _products.values().stream();
	}

	/// Returns a stream over all batches
	Stream<Batch> getBatches() {
		return _batches.stream();
	}

	/// Returns a stream over all partners
	Stream<Partner> getPartners() {
		return _partners.values().stream();
	}

	/// Registers a new partner
	///
	/// Returns the new partner if successful, or empty is a partner with the same name exists
	public Optional<Partner> registerPartner(String partnerId, String partnerName, String partnerAddress) {
		Partner partner = _partners.get(getCollationKey(partnerId));

		// If we didn't have the partner, insert it and return it
		if (partner == null) {
			partner = new Partner(partnerId, partnerName, partnerAddress);
			_partners.put(getCollationKey(partnerId), partner);
			return Optional.of(partner);
		}

		return Optional.empty();
	}

	/// Returns a stream over all transactions
	Stream<Transaction> getTransactions() {
		return _transactions.stream();
	}

	/// Returns a partner given it's id
	Optional<Partner> getPartner(String partnerId) {
		return Optional.ofNullable(_partners.get(getCollationKey(partnerId)));
	}

	/// Returns the max price of a product
	Optional<Double> productMaxPrice(Product product) {
		return _batches.stream() //
				.filter(batch -> batch.getProduct() == product) //
				.max(Batch::compareByUnitPrice) //
				.map(Batch::getUnitPrice);
	}

	/// Returns the total quantity of a product
	int productTotalQuantity(Product product) {
		return _batches.stream() //
				.filter(batch -> batch.getProduct() == product) //
				.mapToInt(Batch::getQuantity) //
				.sum();
	}
}
