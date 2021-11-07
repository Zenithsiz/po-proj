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
import ggc.core.exception.PartnerAlreadyExistsException;
import ggc.core.exception.ProductAlreadyExistsException;
import ggc.core.exception.UnknownPartnerIdException;
import ggc.core.exception.UnknownProductIdException;
import ggc.core.util.SortedMultiMap;
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

	/// Available balance
	private int _availableBalance;

	/// Accounting balance
	private int _accountingBalance;

	/// Next transaction id
	private int _nextTransactionId;

	/// All transactions
	private List<Transaction> _transactions = new ArrayList<>();

	/// All partners
	// Note: `transient` as `CollationKey`s aren't [de]serializable and the keys
	//       are redundant either way.
	private transient Map<CollationKey, Partner> _partners = new HashMap<>();

	/// All products
	// Note: `transient` as `CollationKey`s aren't [de]serializable and the keys
	//       are redundant either way.
	private transient Map<CollationKey, Product> _products = new HashMap<>();

	/// All batches
	// Note: `transient` as `SortedMultiMap` isn't [de]serializable and the keys
	//       are redundant either way.
	private transient SortedMultiMap<Product, Batch> _batches = new SortedMultiMap<>(new BatchComparator());

	/// Comparator for ordering batches by cheapest
	private class BatchComparator implements Comparator<Batch> {
		@Override
		public int compare(Batch lhs, Batch rhs) {
			// TODO: Check if this should be backwards?
			return Double.compare(lhs.getUnitPrice(), rhs.getUnitPrice());
		}
	}

	/// Imports a file onto this warehouse
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
				throws UnknownPartnerIdException, ProductAlreadyExistsException {
			// Get the product or register it
			Product product = _warehouse.getProduct(productId).orElse(null);
			if (product == null) {
				product = _warehouse.registerProduct(productId);
			}

			// Then get the partner and create a new batch for it
			Partner partner = _warehouse.getPartner(partnerId)
					.orElseThrow(() -> new UnknownPartnerIdException(partnerId));
			Batch batch = new Batch(product, quantity, partner, unitPrice);
			_warehouse._batches.put(product, batch);
		}

		@Override
		public void visitDerivedBatch(String productId, String partnerId, int quantity, double unitPrice,
				double costFactor, Stream<Pair<String, Integer>> recipeProducts)
				throws UnknownPartnerIdException, UnknownProductIdException, ProductAlreadyExistsException {
			// Get the product or register it
			Product product = _warehouse.getProduct(productId).orElse(null);
			if (product == null) {
				product = _warehouse.registerDerivedProduct(productId, costFactor, recipeProducts);
			}

			// Then get the partner and create a new batch for it
			Partner partner = _warehouse.getPartner(partnerId)
					.orElseThrow(() -> new UnknownPartnerIdException(partnerId));
			Batch batch = new Batch(product, quantity, partner, unitPrice);
			_warehouse._batches.put(product, batch);
		}

	}

	/// Returns a collation key given a string with the class collation
	private static CollationKey getCollationKey(String key) {
		return collator.getCollationKey(key);
	}

	// Note: We need to override the saving and loading because we use `RuleBasedCollationKey`s,
	// and either way, the hashmaps could be saved as lists, the keys are redundant.
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(new ArrayList<>(_partners.values()));
		out.writeObject(new ArrayList<>(_products.values()));
		out.writeObject(_batches.valuesStream().toList());
	}

	@SuppressWarnings("unchecked") // We're doing a raw cast without being able to properly check the underlying class
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		var partners = (List<Partner>) in.readObject();
		var products = (List<Product>) in.readObject();
		var batches = (List<Batch>) in.readObject();

		_partners = partners.stream()
				.collect(Collectors.toMap(partner -> getCollationKey(partner.getId()), partner -> partner));
		_products = products.stream()
				.collect(Collectors.toMap(product -> getCollationKey(product.getId()), product -> product));
		_batches = batches.stream().map(batch -> new Pair<>(batch.getProduct(), batch))
				.collect(SortedMultiMap.collector(new BatchComparator()));
	}

	/// Returns the current date
	int getDate() {
		return _date;
	}

	/// Advances the current date
	void advanceDate(int offset) {
		_date += offset;
	}

	/// Returns the available balance
	double getAvailableBalance() {
		return _availableBalance;
	}

	/// Returns the accounting balance
	double getAccountingBalance() {
		return _accountingBalance;
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
	Product registerProduct(String productId) throws ProductAlreadyExistsException {
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
	Product registerDerivedProduct(String productId, double costFactor, Stream<Pair<String, Integer>> recipeProducts)
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
				.getOrThrow(UnknownProductIdException.class);

		// Then create the product, insert it and return
		Recipe recipe = new Recipe(productQuantities);
		var product = new DerivedProduct(productId, recipe, costFactor);
		_products.put(getCollationKey(productId), product);
		return product;
	}

	/// Returns a stream over all batches
	Stream<Batch> getBatches() {
		return _batches.valuesStream();
	}

	/// Returns a stream over all partners
	Stream<Partner> getPartners() {
		return _partners.values().stream();
	}

	/// Returns a partner given it's id
	Optional<Partner> getPartner(String partnerId) {
		return Optional.ofNullable(_partners.get(getCollationKey(partnerId)));
	}

	/// Returns a partner's purchases
	Stream<Purchase> getPartnerPurchases(Partner partner) {
		return partner.getPurchases();
	}

	/// Returns a partner's sales
	Stream<Sale> getPartnerSales(Partner partner) {
		return partner.getSales();
	}

	/// Registers a new partner
	Partner registerPartner(String partnerId, String partnerName, String partnerAddress)
			throws PartnerAlreadyExistsException {
		// If we already had the partner, throw
		if (getPartner(partnerId).isPresent()) {
			throw new PartnerAlreadyExistsException(partnerId);
		}

		// Else create it, insert it and return
		var partner = new Partner(partnerId, partnerName, partnerAddress);
		_partners.put(getCollationKey(partnerId), partner);
		return partner;
	}

	/// Toggles a partner's product notifications
	void togglePartnerNotifications(Partner partner, Product product) {
		partner.toggleIsProductNotificationBlacklisted(product);
	}

	/// Returns a stream over all transactions
	Stream<Transaction> getTransactions() {
		return _transactions.stream();
	}

	/// Returns a transaction given it's id
	Optional<Transaction> getTransaction(int transactionId) {
		return transactionId >= 0 && transactionId < _transactions.size()
				? Optional.of(_transactions.get(transactionId))
				: Optional.empty();
	}

	/// Registers a new purchase
	Purchase registerPurchase(Partner partner, Product product, int quantity, double unitPrice) {
		// Create the batch for this purchase and add it
		var batch = new Batch(product, quantity, partner, unitPrice);
		_batches.put(product, batch);

		// Then create the transaction for it
		// TODO: Verify that the date of the purchase is the current day
		var purchase = new Purchase(_nextTransactionId, _date, product, partner, quantity, quantity * unitPrice);
		_nextTransactionId++;
		partner.addPurchase(purchase);
		_transactions.add(purchase);

		return purchase;
	}

	/// Registers a new sale
	Sale registerSale(Partner partner, Product product, int quantity, int deadline) {
		// TODO: Figure out whether or not to "reset" to the previous state if an error occurs
		// midway through, for now we assume we don't reset for simplicity

		// The total price of the sale so far and the current quantity of products gotten
		var totalPrice = 0.0;
		var curQuantity = 0;

		// Go through all batches involving this product
		for (var batch : _batches.get(product).get()) {
			// If we have enough, quit
			if (curQuantity == quantity) {
				break;
			}

			// Check the max quantity we can take from this batch
			int batchQuantity = Math.min(quantity - curQuantity, batch.getQuantity());

			totalPrice += batchQuantity * batch.getUnitPrice();
			batch.takeQuantity(batchQuantity);
		}

		var paymentDate = 0; // TODO:

		// Create the sale
		var sale = new CreditSale(_nextTransactionId, paymentDate, product, partner, quantity, totalPrice, deadline);
		_nextTransactionId++;
		partner.addSale(sale);
		_transactions.add(sale);

		return sale;
	}

	/// Registers a new breakdown
	Sale registerBreakdown(Partner partner, Product product, int quantity) {
		// TODO:
		return null;
	}

	/// Returns the max price of a product
	Optional<Double> productMaxPrice(Product product) {
		return _batches.valuesStream() //
				.filter(batch -> batch.getProduct() == product) //
				.max(Batch::compareByUnitPrice) //
				.map(Batch::getUnitPrice);
	}

	/// Returns the total quantity of a product
	int productTotalQuantity(Product product) {
		return _batches.valuesStream() //
				.filter(batch -> batch.getProduct() == product) //
				.mapToInt(Batch::getQuantity) //
				.sum();
	}

	/// Returns a product comparator by it's id
	Comparator<Product> productComparator() {
		return Comparator.comparing(product -> getCollationKey(product.getId()));
	}

	/// Returns a batch comparator by it's product id, partner id, unit price and then quantity
	Comparator<Batch> batchComparator() {
		return Comparator.<Batch, CollationKey>comparing(batch -> getCollationKey(batch.getProduct().getId()))
				.thenComparing(batch -> getCollationKey(batch.getPartner().getId())).thenComparing(Batch::getUnitPrice)
				.thenComparing(Batch::getQuantity);
	}

	/// Returns a batch filter by it's partner id
	Predicate<Batch> batchFilterPartnerId(String partnerId) {
		return batch -> collator.equals(batch.getPartner().getId(), partnerId);
	}

	/// Returns a batch filter by it's product id
	Predicate<Batch> batchFilterProductId(String productId) {
		return batch -> collator.equals(batch.getProduct().getId(), productId);
	}

	/// Returns a batch filter by it's price
	Predicate<Batch> batchFilterPrice(Predicate<Double> predicate) {
		return batch -> predicate.test(batch.getUnitPrice());
	}

	/// Returns a partner comparator by it's id
	Comparator<Partner> partnerComparator() {
		// Note: Id is unique, so we don't need to compare by anything else
		return Comparator.comparing(partner -> getCollationKey(partner.getId()));
	}

	/// Returns a sale filter for paid sales
	Predicate<Sale> saleFilterPaid() {
		return sale -> sale.isPaid();
	}
}
