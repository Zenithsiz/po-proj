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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ggc.core.exception.BadEntryException;
import ggc.core.exception.InsufficientProductsException;
import ggc.core.exception.ParsingException;
import ggc.core.exception.PartnerAlreadyExistsException;
import ggc.core.exception.ProductAlreadyExistsException;
import ggc.core.exception.UnknownPartnerIdException;
import ggc.core.exception.UnknownProductIdException;
import ggc.core.util.SortedMultiMap;
import ggc.core.util.StreamIterator;
import ggc.core.util.Pair;

/**
 * The warehouse
 */
// Note: Package private because we don't need it outside of core
class Warehouse implements Serializable {

	/** Serial number for serialization. */
	private static final long serialVersionUID = 2021_10_28_18_50L;

	/** Collator for all strings */
	private static Collator collator;

	static {
		collator = Collator.getInstance();
		// Note: Secondary so we consider accents
		collator.setStrength(Collator.SECONDARY);
		collator.setDecomposition(Collator.FULL_DECOMPOSITION);
	}

	/** Current date */
	// TODO: Think about whether or not to make this a static class, wouldn't work super well with loading.
	private int _date;

	/** Available balance */
	private int _availableBalance;

	/** Next transaction id */
	private int _nextTransactionId;

	/** All transactions */
	private List<Transaction> _transactions = new ArrayList<>();

	/** All partners */
	// Note: `transient` as `CollationKey`s aren't [de]serializable and the keys
	//       are redundant either way.
	private transient Map<CollationKey, Partner> _partners = new HashMap<>();

	/** All products */
	// Note: `transient` as `CollationKey`s aren't [de]serializable and the keys
	//       are redundant either way.
	private transient Map<CollationKey, Product> _products = new HashMap<>();

	/** All batches */
	// Note: `transient` as `SortedMultiMap` isn't [de]serializable and the keys
	//       are redundant either way.
	private transient SortedMultiMap<Product, Batch> _batches = new SortedMultiMap<>(new BatchComparator());

	/** Comparator for ordering batches by cheapest */
	private class BatchComparator implements Comparator<Batch> {
		@Override
		public int compare(Batch lhs, Batch rhs) {
			// TODO: Check if this should be backwards?
			return Double.compare(lhs.getUnitPrice(), rhs.getUnitPrice());
		}
	}

	/**
	 * Imports a file onto this warehouse
	 * 
	 * @param fileName
	 *            The filename to import
	 * @throws IOException
	 *             If unable to read the file
	 * @throws BadEntryException
	 *             If the entry was malformed
	 * @throws ParsingException
	 *             If unable to insert any entry into the warehouse
	 */
	void importFile(String fileName) throws IOException, BadEntryException, ParsingException {
		// Create a parser and visit all lines
		var parser = new Parser(fileName);
		parser.visit(new ImportParserVisitor(this));
	}

	/** Visitor for importing a file */
	private class ImportParserVisitor implements ParserVisitor {
		/** The warehouse we're importing onto */
		private Warehouse _warehouse;

		/**
		 * Creates a new visitor
		 * 
		 * @param warehouse
		 *            The warehouse to import onto
		 */
		ImportParserVisitor(Warehouse warehouse) {
			_warehouse = warehouse;
		}

		@Override
		public void visitPartner(String id, String name, String address) {
			var partner = new Partner(id, name, address);
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
			Batch batch = new Batch(product, partner, quantity, unitPrice);
			_warehouse.insertBatch(batch);
		}

		@Override
		public void visitDerivedBatch(String productId, String partnerId, int quantity, double unitPrice,
				double costFactor, Stream<Pair<String, Integer>> recipeProductIdQuantities)
				throws UnknownPartnerIdException, UnknownProductIdException, ProductAlreadyExistsException {
			// Get the product or register it
			Product product = _warehouse.getProduct(productId).orElse(null);
			if (product == null) {
				product = _warehouse.registerDerivedProduct(productId, costFactor, recipeProductIdQuantities);
			}

			// Then get the partner and create a new batch for it
			Partner partner = _warehouse.getPartner(partnerId)
					.orElseThrow(() -> new UnknownPartnerIdException(partnerId));
			Batch batch = new Batch(product, partner, quantity, unitPrice);
			_warehouse.insertBatch(batch);
		}

	}

	/**
	 * Retrieves a collation key given a string key
	 * 
	 * @param key
	 *            The key to retrieve the collation key for
	 * @return The collation key
	 */
	private static CollationKey getCollationKey(String key) {
		return collator.getCollationKey(key);
	}

	/**
	 * Override for serialization to write our transient fields
	 * 
	 * @param out
	 *            The stream to write to
	 * @throws IOException
	 *             If unable to write
	 */
	// Note: We need to override the saving and loading because we use `RuleBasedCollationKey`s,
	// and either way, the hashmaps could be saved as lists, the keys are redundant.
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(new ArrayList<>(_partners.values()));
		out.writeObject(new ArrayList<>(_products.values()));
		out.writeObject(_batches.valuesStream().collect(Collectors.toList()));
	}

	/**
	 * Override for deserialization to read our transient fields
	 * 
	 * @param in
	 *            The stream to read from
	 * @throws IOException
	 *             If unable to read
	 * @throws ClassNotFoundException
	 *             If a class wasn't found while loading
	 */
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

	/**
	 * Retrieves the current date
	 * 
	 * @return The current date
	 */
	int getDate() {
		return _date;
	}

	/**
	 * Advances the current date
	 * 
	 * @param offset
	 *            The offset to add to the date
	 */
	void advanceDate(int offset) {
		assert offset >= 0;
		_date += offset;
		assert _date >= 0;
	}

	/**
	 * Retrieves the available balance
	 * 
	 * @return The available balance
	 */
	double getAvailableBalance() {
		return _availableBalance;
	}

	/**
	 * Retrieves the accounting balance
	 * 
	 * @return The accounting balance
	 */
	double getAccountingBalance() {
		return _availableBalance
				+ _transactions.stream().flatMapToDouble(transactionFilterUnpaidCreditSalePaymentAmounts()).sum();
	}

	/**
	 * Retrieves a stream over all products
	 * 
	 * @return All products
	 */
	Stream<Product> getProducts() {
		return _products.values().stream();
	}

	/**
	 * Retrieves a product given it's id
	 * 
	 * @param productId
	 *            The id of the product
	 * @return The product, if it exists
	 */
	Optional<Product> getProduct(String productId) {
		return Optional.ofNullable(_products.get(getCollationKey(productId)));
	}

	/**
	 * Registers a simple product given it's id
	 * 
	 * @param productId
	 *            The product id
	 * @return The created product
	 * @throws ProductAlreadyExistsException
	 *             If the product already exists
	 */
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

	/**
	 * Registers a derived product
	 * 
	 * @param productId
	 *            The product id
	 * @param costFactor
	 *            The product cost factor
	 * @param recipeProductIdQuantities
	 *            The recipe product id quantities
	 * 
	 * @return The created product
	 * @throws ProductAlreadyExistsException
	 *             If the product already exists
	 * @throws UnknownProductIdException
	 *             If any product in the recipe doesn't exist
	 */
	Product registerDerivedProduct(String productId, double costFactor,
			Stream<Pair<String, Integer>> recipeProductIdQuantities)
			throws ProductAlreadyExistsException, UnknownProductIdException {
		// If we already had the product, throw
		if (getProduct(productId).isPresent()) {
			throw new ProductAlreadyExistsException(productId);
		}

		// Else create the product, insert it and return
		var recipe = Recipe.fromProductIds(recipeProductIdQuantities, this::getProduct);
		var product = new DerivedProduct(productId, recipe, costFactor);
		_products.put(getCollationKey(productId), product);
		return product;
	}

	/**
	 * Retrieves a stream over all batches
	 * 
	 * @return All batches
	 */
	Stream<Batch> getBatches() {
		return _batches.valuesStream();
	}

	/**
	 * Inserts a new batch.
	 * 
	 * @param batch
	 *            The batch to insert
	 */
	private void insertBatch(Batch batch) {
		// Insert the batch
		Product product = batch.getProduct();
		_batches.put(product, batch);

		// If the price of the batch is the highest/lowest yet, set it
		double unitPrice = batch.getUnitPrice();
		var minPrice = product.getMinPrice();
		var maxPrice = product.getMaxPrice();
		if (minPrice.isEmpty() || unitPrice < minPrice.getAsDouble()) {
			product.setMinPrice(unitPrice);
		}
		if (maxPrice.isEmpty() || unitPrice > maxPrice.getAsDouble()) {
			product.setMaxPrice(unitPrice);
		}
	}

	/**
	 * Retrieves a stream over all partners
	 * 
	 * @return All partners
	 */
	Stream<Partner> getPartners() {
		return _partners.values().stream();
	}

	/**
	 * Retrieves a partner given it's id
	 * 
	 * @param partnerId
	 *            Id of the partner
	 * @return The partner, if they exist
	 */
	Optional<Partner> getPartner(String partnerId) {
		return Optional.ofNullable(_partners.get(getCollationKey(partnerId)));
	}

	/**
	 * Retrieves a partner's purchases
	 * 
	 * @param partner
	 *            The partner to get the purchases
	 * @return All purchases of the partner
	 */
	Stream<Purchase> getPartnerPurchases(Partner partner) {
		return partner.getPurchases();
	}

	/**
	 * Retrieves a partner's sales
	 * 
	 * @param partner
	 *            The partner to get the sales
	 * @return All sales of the partner
	 */
	Stream<Sale> getPartnerSales(Partner partner) {
		return partner.getSales();
	}

	/**
	 * Registers a new partner
	 * 
	 * @param id
	 *            The id of the partner
	 * @param name
	 *            The name of the partner
	 * @param address
	 *            The address of the partner
	 * @return The partner created
	 * @throws PartnerAlreadyExistsException
	 *             If the partner already exists
	 */
	Partner registerPartner(String id, String name, String address) throws PartnerAlreadyExistsException {
		// If we already had the partner, throw
		if (getPartner(id).isPresent()) {
			throw new PartnerAlreadyExistsException(id);
		}

		// Else create it, insert it and return
		var partner = new Partner(id, name, address);
		_partners.put(getCollationKey(id), partner);
		return partner;
	}

	/**
	 * Toggles a partner's product notifications
	 * 
	 * @param partner
	 *            The partner to toggle notifications for
	 * @param product
	 *            The product to toggle notifications for
	 */
	void togglePartnerNotifications(Partner partner, Product product) {
		partner.toggleIsProductNotificationBlacklisted(product);
	}

	/**
	 * Retrieves a stream over all transactions
	 * 
	 * @return All transactions
	 */
	Stream<Transaction> getTransactions() {
		return _transactions.stream();
	}

	/**
	 * Retrieves a transaction given it's id
	 * 
	 * @param id
	 *            The id of the transaction
	 * @return The transaction, if valid
	 */
	Optional<Transaction> getTransaction(int id) {
		return id >= 0 && id < _transactions.size() ? Optional.of(_transactions.get(id)) : Optional.empty();
	}

	/**
	 * Registers a new purchase
	 * 
	 * @param partner
	 *            The purchase's partner
	 * @param product
	 *            The purchase's product
	 * @param quantity
	 *            The purchase's quantity
	 * @param unitPrice
	 *            The purchase's unit price
	 * @return The purchase
	 */
	Purchase registerPurchase(Partner partner, Product product, int quantity, double unitPrice) {
		// Get the previous quantities and lowest price
		var prevProductQuantity = productTotalQuantity(product);
		var prevLowestPrice = product.getMinPrice();

		// Create the batch for this purchase and add it
		var batch = new Batch(product, partner, quantity, unitPrice);
		insertBatch(batch);

		// Then create the transaction for it
		var purchase = new Purchase(_nextTransactionId, _date, product, partner, quantity, quantity * unitPrice);
		_nextTransactionId++;
		partner.addPurchase(purchase);
		_transactions.add(purchase);

		// And update our balance
		_availableBalance -= unitPrice * quantity;

		// If this is a new batch of an empty product, emit a `NEW` notification
		// TODO: Move this to all the subclasses
		if (prevProductQuantity == quantity) {
			for (var notificationPartner : _partners.values()) {
				if (!notificationPartner.isProductNotificationBlacklisted(product)) {
					var notification = new Notification(batch, "NEW");
					notificationPartner.addNotifications(notification);
				}
			}
		}

		// If this product is the cheapest of all other batches, and isn't the only batch,
		// emit a `BARGAIN` notification
		if (_batches.get(product).get().size() > 1
				&& (prevLowestPrice.isEmpty() || unitPrice < prevLowestPrice.getAsDouble())) {
			for (var notificationPartner : _partners.values()) {
				if (!notificationPartner.isProductNotificationBlacklisted(product)) {
					var notification = new Notification(batch, "BARGAIN");
					notificationPartner.addNotifications(notification);
				}
			}
		}

		return purchase;
	}

	/**
	 * Registers a new sale
	 * 
	 * @param partner
	 *            The sale's partner
	 * @param product
	 *            The sale's product
	 * @param quantity
	 *            The sale's quantity
	 * @param deadline
	 *            The sale's deadline
	 * @return The sale
	 * @throws InsufficientProductsException
	 *             If there isn't enough quantity of the product for the sale.
	 */
	CreditSale registerSale(Partner partner, Product product, int quantity, int deadline)
			throws InsufficientProductsException {
		// Remove `quantity` of `product`
		var totalPrice = removeProduct(product, quantity);

		// And create the sale
		var sale = new CreditSale(_nextTransactionId, product, partner, quantity, totalPrice, deadline);
		_nextTransactionId++;
		partner.addCreditSale(sale);
		_transactions.add(sale);

		return sale;
	}

	/**
	 * Pays a transaction if it's a sale
	 * 
	 * @param sale
	 *            The transaction to pay
	 */
	public void paySale(Transaction sale) {
		// If it's a credit sale, pay it
		if (sale instanceof CreditSale) {
			var partner = sale.getPartner();
			var amountPaid = partner.paySale((CreditSale) sale, getDate());

			_availableBalance += amountPaid;
		}
	}

	/**
	 * Registers a new breakdown
	 * 
	 * @param partner
	 *            The partner that requested the breakdown
	 * @param product
	 *            The product to break down
	 * @param quantity
	 *            The quantity of product to break down
	 * @return The breakdown sale
	 * @throws InsufficientProductsException
	 *             If there aren't enough products to break down
	 */
	BreakdownSale registerBreakdown(Partner partner, DerivedProduct product, int quantity)
			throws InsufficientProductsException {
		// If we don't have `quantity` products, throw
		var quantityAvailable = _batches.get(product).get().stream().mapToInt(Batch::getQuantity).sum();
		if (quantityAvailable < quantity) {
			throw new InsufficientProductsException(product.getId(), quantity, quantityAvailable);
		}

		// Else remove the quantity of product, and add all components
		// Note: `removeProduct` here won't manufacture any, since we know we have enough in stock
		removeProduct(product, quantity);
		double totalPrice = -product.getCostFactor() * quantity;
		var components = new ArrayList<Pair<Product, Pair<Integer, Double>>>();
		for (var pair : StreamIterator.streamIt(product.getRecipe().getProductQuantities())) {
			var recipeProduct = pair.getLhs();
			var recipeUnitQuantity = pair.getRhs();
			var recipeQuantity = quantity * recipeUnitQuantity;

			// Get the price to create the new batch with
			// TODO: Check if we create a new batch here, or just use the cheapest if it exists
			// TODO: If we have no bundles and no max price, what do we use?
			var recipeUnitPrice = _batches.get(recipeProduct).stream() //
					.flatMap(List::stream) //
					.map(Batch::getUnitPrice) //
					.findFirst() //
					.orElseGet(() -> product.getMaxPrice().orElse(0.0));
			var recipePrice = recipeQuantity * recipeUnitPrice;

			// Then create it and insert it
			var batch = new Batch(recipeProduct, partner, recipeQuantity, recipePrice);
			insertBatch(batch);
			components.add(new Pair<>(recipeProduct, new Pair<>(recipeQuantity, recipePrice)));
		}

		// Update our balance
		_availableBalance += Math.abs(totalPrice);

		// And create the sale
		var sale = new BreakdownSale(_nextTransactionId, _date, product, partner, quantity, totalPrice, components);
		_nextTransactionId++;
		partner.addBreakdownSale(sale);
		_transactions.add(sale);

		return sale;
	}

	/**
	 * Removes a quantity of a product from stock, manufacturing if not enough exist
	 * 
	 * @param product
	 *            The product to remove
	 * @param quantity
	 *            The quantity to remove
	 * @return The total price of all products removed
	 * @throws InsufficientProductsException
	 *             If there weren't enough products to remove
	 */
	private double removeProduct(Product product, int quantity) throws InsufficientProductsException {
		// If we're removing 0, return
		assert quantity >= 0;
		if (quantity == 0) {
			return 0.0;
		}

		// Check that we have enough products
		assertProductQuantity(product, quantity);

		// Go through all batches involving this product
		var batches = _batches.get(product);
		var totalPrice = 0.0;
		var curQuantity = 0;
		if (batches.isPresent()) {
			for (var batch : batches.get()) {
				// If we have enough, stop removing
				if (curQuantity == quantity) {
					break;
				}

				// Take at most what we need or however much the batch has.
				int batchQuantity = Math.min(quantity - curQuantity, batch.getQuantity());

				// Then remove them and update our quantity and price
				totalPrice += batchQuantity * batch.getUnitPrice();
				curQuantity += batchQuantity;
				batch.takeQuantity(batchQuantity);
			}

			// Then remove all empty batches
			batches.get().removeIf(batch -> batch.getQuantity() == 0);
		}

		// If we didn't have enough, manufacture them
		// Note: Given that we asserted we had enough quantity above, if we don't
		//       have enough quantity currently, we know that there's enough quantity
		//       to manufacture it here, and that the product is derived.
		if (curQuantity < quantity) {
			totalPrice += removeProductRecipeComponents(product.getAsDerived().get(), quantity - curQuantity);
		}

		return totalPrice;
	}

	/**
	 * Asserts that there are enough quantity of `product` to supply, including possibly manufacturing.
	 * 
	 * @param product
	 *            The product to check
	 * @param quantity
	 *            The minimum quantity
	 * @throws InsufficientProductsException
	 *             If there aren't enough products
	 */
	private void assertProductQuantity(Product product, int quantity) throws InsufficientProductsException {
		// If we have enough quantity, return
		var quantityAvailable = _batches.get(product).get().stream().mapToInt(Batch::getQuantity).sum();
		if (quantityAvailable >= quantity) {
			return;
		}

		// Else get the product as a derived one, or throw given that we can't manufacture it, otherwise
		var derivedProduct = product.getAsDerived()
				.orElseThrow(() -> new InsufficientProductsException(product.getId(), quantity, quantityAvailable));

		// Finally check if there's enough of each component to manufacture enough product
		int quantityRemaining = quantity - quantityAvailable;
		for (var pair : StreamIterator.streamIt(derivedProduct.getRecipe().getProductQuantities())) {
			var recipeProduct = pair.getLhs();
			var recipeUnitQuantity = pair.getRhs();
			var recipeQuantity = quantityRemaining * recipeUnitQuantity;

			assertProductQuantity(recipeProduct, recipeQuantity);
		}
	}

	/**
	 * Removes all components of the recipe of a product
	 * 
	 * @param product
	 *            The product to remove
	 * @param quantity
	 *            The quantity to remove
	 * @return The total price of all products removed
	 * @throws InsufficientProductsException
	 *             If there weren't enough products to remove
	 */
	private double removeProductRecipeComponents(DerivedProduct product, int quantity)
			throws InsufficientProductsException {
		// Go through all products of the recipe
		double totalPrice = 0;
		for (var pair : StreamIterator.streamIt(product.getRecipe().getProductQuantities())) {
			var recipeProduct = pair.getLhs();
			var recipeUnitQuantity = pair.getRhs();
			var recipeQuantity = quantity * recipeUnitQuantity;

			totalPrice += removeProduct(recipeProduct, recipeQuantity);
		}

		// TODO: Check if this should be `(1 + costFactor) * totalPrice`
		return product.getCostFactor() * totalPrice;
	}

	/**
	 * Retrieves the total quantity of a product
	 * 
	 * @param product
	 *            The product to get the quantity of
	 * @return The quantity of the product
	 */
	int productTotalQuantity(Product product) {
		return _batches.get(product) //
				.map(batches -> batches.stream() //
						.mapToInt(Batch::getQuantity) //
						.sum() //
				).orElse(0);
	}

	/**
	 * Retrieves a product comparator by it's id
	 * 
	 * @return A product comparator by id
	 */
	Comparator<Product> productComparator() {
		return Comparator.comparing(product -> getCollationKey(product.getId()));
	}

	/**
	 * Retrieves a batch comparator by it's product id, partner id, unit price and then quantity
	 * 
	 * @return A batch comparator
	 */
	Comparator<Batch> batchComparator() {
		return Comparator.<Batch, CollationKey>comparing(batch -> getCollationKey(batch.getProduct().getId()))
				.thenComparing(batch -> getCollationKey(batch.getPartner().getId())).thenComparing(Batch::getUnitPrice)
				.thenComparing(Batch::getQuantity);
	}

	/**
	 * Retrieves a batch filter by it's partner
	 * 
	 * @param partner
	 *            The partner to filter by
	 * @return A batch filter by partner
	 */
	Predicate<Batch> batchFilterPartner(Partner partner) {
		return batch -> batch.getPartner() == partner;
	}

	/**
	 * Retrieves a batch filter by it's product
	 * 
	 * @param product
	 *            The product to filter by
	 * @return A batch filter by product
	 */
	Predicate<Batch> batchFilterProduct(Product product) {
		return batch -> batch.getProduct() == product;
	}

	/**
	 * Retrieves a batch filter by it's price
	 * 
	 * @param predicate
	 *            The predicate for the filter
	 * @return A batch filter by price predicate
	 */
	Predicate<Batch> batchFilterPrice(Predicate<Double> predicate) {
		return batch -> predicate.test(batch.getUnitPrice());
	}

	/**
	 * Retrieves a partner comparator by it's id
	 * 
	 * @return A partner comparator by id
	 */
	Comparator<Partner> partnerComparator() {
		// Note: Id is unique, so we don't need to compare by anything else
		return Comparator.comparing(partner -> getCollationKey(partner.getId()));
	}

	/**
	 * Retrieves a sale filter for paid sales
	 * 
	 * @return A sale filter by if they're paid
	 */
	Predicate<Sale> saleFilterPaid() {
		return sale -> sale.isPaid();
	}

	/**
	 * Retrieves a transaction flat map for payment amounts unpaid sales
	 * 
	 * @return A function from a transaction to it's payment amount, if it's unpaid
	 */
	// TODO: Stream isn't the best for this, but `flatMap` might require it, check.
	private Function<Transaction, DoubleStream> transactionFilterUnpaidCreditSalePaymentAmounts() {
		return transaction -> {
			if (transaction instanceof CreditSale) {
				var sale = (CreditSale) transaction;
				if (!sale.isPaid()) {
					return DoubleStream.of(sale.getPaymentAmount(getDate()));
				}
			}

			return DoubleStream.empty();
		};
	}
}
