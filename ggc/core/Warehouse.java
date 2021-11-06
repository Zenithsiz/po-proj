package ggc.core;

import java.io.Serializable;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import ggc.core.exception.BadEntryException;
import ggc.core.exception.ParsingException;
import ggc.core.exception.ProductAlreadyExistsException;
import ggc.core.exception.UnknownPartnerIdException;
import ggc.core.exception.UnknownProductIdException;
import ggc.core.util.Pair;
import ggc.core.util.Result;

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

	/// Current balance
	private int _balance;

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
		return collator.getCollationKey(key);
	}

	// Note: We need to override the saving and loading because we use `RuleBasedCollationKey`s,
	// and either way, the hashmaps could be saved as lists, the keys are redundant.
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(_date);
		out.writeObject(_balance);
		out.writeObject(_nextTransactionId);
		out.writeObject(_transactions);
		out.writeObject(new ArrayList<>(_partners.values()));
		out.writeObject(new ArrayList<>(_products.values()));
		out.writeObject(_batches);
	}

	@SuppressWarnings("unchecked") // We're doing a raw cast without being able to properly check the underlying class
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		var date = (Integer) in.readObject();
		var balance = (Integer) in.readObject();
		var nextTransactionId = (Integer) in.readObject();
		var transactions = (List<Transaction>) in.readObject();
		var partners = (List<Partner>) in.readObject();
		var products = (List<Product>) in.readObject();
		var batches = (List<Batch>) in.readObject();

		_date = date;
		_balance = balance;
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

	/// Returns a product given it's id
	Optional<Product> getProduct(String productId) {
		return Optional.ofNullable(_products.get(getCollationKey(productId)));
	}

	/// Registers a simple product given it's id
	public Product registerProduct(String productId) throws ProductAlreadyExistsException {
		// If we already had the product, throw
		if (getProduct(productId).isPresent()) {
			throw new ProductAlreadyExistsException(productId);
		}

		// Else create it, insert it and return
		var product = new Product(productId);
		_products.put(getCollationKey(productId), product);
		return product;
	}

	/// Registers a derived product given it's id, alpha and all components by id
	public Product registerDerivedProduct(String productId, double costFactor,
			Stream<Pair<String, Integer>> recipeProducts)
			throws ProductAlreadyExistsException, UnknownProductIdException {
		// If we already had the product, throw
		if (getProduct(productId).isPresent()) {
			throw new ProductAlreadyExistsException(productId);
		}

		// Else get all product quantities, or throw if one of them didn't exist
		Map<Product, Integer> productQuantities = recipeProducts //
				.map(pair -> pair.mapLeft(recipeProductId -> Result.fromOptional( //
						getProduct(recipeProductId), //
						() -> new UnknownProductIdException(recipeProductId) //
				))) //
				.collect(Pair.toResultMapCollector()) //
				.getOrThrow();

		// Then create the product, insert it and return
		Recipe recipe = new Recipe(productQuantities);
		var product = new DerivedProduct(productId, recipe, costFactor);
		_products.put(getCollationKey(productId), product);
		return product;
	}

	/// Returns a stream over all batches
	Stream<Batch> getBatches() {
		return _batches.stream();
	}

	/// Returns a stream over all partners
	Stream<Partner> getPartners() {
		return _partners.values().stream();
	}

	/// Returns a partner given it's id
	Optional<Partner> getPartner(String partnerId) {
		return Optional.ofNullable(_partners.get(getCollationKey(partnerId)));
	}

	/// Registers a new partner
	///
	/// Returns the new partner if successful, or empty is a partner with the same name exists
	Optional<Partner> registerPartner(String partnerId, String partnerName, String partnerAddress) {
		// If we didn't have the product, insert it and return it
		if (getPartner(partnerId).isEmpty()) {
			var partner = new Partner(partnerId, partnerName, partnerAddress);
			_partners.put(getCollationKey(partnerId), partner);
			return Optional.of(partner);
		}

		// Else return empty
		return Optional.empty();
	}

	/// Toggles a partner's product notifications
	void togglePartnerNotifications(Partner partner, Product product) {
		partner.toggleIsProductNotificationBlacklisted(product);
	}

	/// Returns a stream over all transactions
	Stream<Transaction> getTransactions() {
		return _transactions.stream();
	}

	/// Registers a new purchase
	public Purchase registerPurchase(Partner partner, Product product, int quantity, double unitPrice) {
		// Create the batch for this purchase and add it
		var batch = new Batch(product, quantity, partner, unitPrice);
		_batches.add(batch);

		var paymentDate = 0; // TODO:

		// Then create the transaction for it
		var purchase = new Purchase(_nextTransactionId, paymentDate, product, partner, quantity, quantity * unitPrice);
		_nextTransactionId++;
		partner.addPurchase(purchase);
		_transactions.add(purchase);

		return purchase;
	}

	/// Returns a transaction given it's id
	Optional<Transaction> getTransaction(int transactionId) {
		return transactionId < _transactions.size() ? Optional.of(_transactions.get(transactionId)) : Optional.empty();
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

	/// Returns a product comparator by it's id
	static Comparator<Product> productComparator() {
		return Comparator.comparing(product -> getCollationKey(product.getId()));
	}

	/// Returns a batch comparator by it's product id, partner id, unit price and then quantity
	static Comparator<Batch> batchComparator() {
		return Comparator.<Batch, CollationKey>comparing(batch -> getCollationKey(batch.getProduct().getId()))
				.thenComparing(batch -> getCollationKey(batch.getPartner().getId())).thenComparing(Batch::getUnitPrice)
				.thenComparing(Batch::getQuantity);
	}

	/// Returns a batch filter by it's partner id
	static Predicate<Batch> batchFilterPartnerId(String partnerId) {
		return batch -> collator.equals(batch.getPartner().getId(), partnerId);
	}

	/// Returns a batch filter by it's product id
	static Predicate<Batch> batchFilterProductId(String productId) {
		return batch -> collator.equals(batch.getProduct().getId(), productId);
	}

	/// Returns a partner comparator by it's id
	static Comparator<Partner> partnerComparator() {
		// Note: Id is unique, so we don't need to compare by anything else
		return Comparator.comparing(partner -> getCollationKey(partner.getId()));
	}
}
