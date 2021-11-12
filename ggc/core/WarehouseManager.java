package ggc.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import ggc.core.exception.InsufficientProductsException;
import ggc.core.exception.BadEntryException;
import ggc.core.exception.ImportFileException;
import ggc.core.exception.ParsingException;
import ggc.core.exception.PartnerAlreadyExistsException;
import ggc.core.exception.ProductAlreadyExistsException;
import ggc.core.exception.UnknownProductIdException;
import ggc.core.util.Pair;

/** Façade for access. */
public class WarehouseManager {

	/** Name of file storing current warehouse. */
	private Optional<String> _fileName = Optional.empty();

	/** The warehouse itself. */
	private Warehouse _warehouse = new Warehouse();

	/** If any changes were performed on the warehouse since the last save */
	private boolean _warehouseIsDirty;

	/**
	 * Saves the file into the associated file, or, if inexistent, gets it from the supplier
	 * 
	 * @param supplier
	 *            The filename supplier, if none is associated
	 * @throws IOException
	 *             If unable to save
	 */
	public void save(Supplier<? extends String> supplier) throws IOException {
		// Get our associated file, or use the supplier if we don't have it.
		var fileName = _fileName.orElseGet(supplier);

		// Open the file to save, and create an output stream from it
		try (var file = new FileOutputStream(fileName); var stream = new ObjectOutputStream(file)) {
			// Write the warehouse to file
			stream.writeObject(_warehouse);
			_warehouseIsDirty = false;
			_fileName = Optional.of(fileName);
		}
	}

	/**
	 * Loads from the file name and then associates it
	 * 
	 * @param fileName
	 *            The filename to load from
	 * @throws IOException
	 *             If unable to load
	 * @throws ClassNotFoundException
	 *             If a class wasn't found during loading
	 */
	public void loadFrom(String fileName) throws IOException, ClassNotFoundException {
		try (var file = new FileInputStream(fileName); var stream = new ObjectInputStream(file)) {
			// Try to read the warehouse
			// Note: We set dirty to false, as the new file is now the current state of the warehouse
			_warehouse = (Warehouse) stream.readObject();
			_warehouseIsDirty = false;
			_fileName = Optional.of(fileName);
		}
	}

	/**
	 * Imports a file onto this warehouse
	 * 
	 * @param fileName
	 *            The filename to import
	 * @throws ImportFileException
	 *             If unable to import the file
	 */
	public void importFile(String fileName) throws ImportFileException {
		// Import and set ourselves as dirty
		try {
			_warehouse.importFile(fileName);
			_warehouseIsDirty = true;
		} catch (IOException | BadEntryException | ParsingException e) {
			throw new ImportFileException(fileName, e);
		}
	}

	/**
	 * Returns if any changes were made to the warehouse
	 * 
	 * @return If the warehouse is dirty since the last save
	 */
	public boolean isWarehouseDirty() {
		return _warehouseIsDirty;
	}

	/**
	 * Retrieves the current date
	 * 
	 * @return The current date
	 */
	public int getDate() {
		return _warehouse.getDate();
	}

	/**
	 * Advances the current date
	 * 
	 * @param offset
	 *            The offset to add to the date
	 */
	public void advanceDate(int offset) {
		_warehouse.advanceDate(offset);
		_warehouseIsDirty = true;
	}

	/**
	 * Retrieves the available balance
	 * 
	 * @return The available balance
	 */
	public double getAvailableBalance() {
		return _warehouse.getAvailableBalance();
	}

	/**
	 * Retrieves the accounting balance
	 * 
	 * @return The accounting balance
	 */
	public double getAccountingBalance() {
		return _warehouse.getAccountingBalance();
	}

	/**
	 * Retrieves a stream over all products
	 * 
	 * @return All products
	 */
	public Stream<Product> getProducts() {
		return _warehouse.getProducts();
	}

	/**
	 * Retrieves a product given it's id
	 * 
	 * @param productId
	 *            The id of the product
	 * @return The product, if it exists
	 */
	public Optional<Product> getProduct(String productId) {
		return _warehouse.getProduct(productId);
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
	public Product registerProduct(String productId) throws ProductAlreadyExistsException {
		var product = _warehouse.registerProduct(productId);
		_warehouseIsDirty = true;
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
	public Product registerDerivedProduct(String productId, double costFactor,
			Stream<Pair<String, Integer>> recipeProductIdQuantities)
			throws ProductAlreadyExistsException, UnknownProductIdException {
		var product = _warehouse.registerDerivedProduct(productId, costFactor, recipeProductIdQuantities);
		_warehouseIsDirty = true;
		return product;
	}

	/**
	 * Retrieves a stream over all batches
	 * 
	 * @return All batches
	 */
	public Stream<Batch> getBatches() {
		return _warehouse.getBatches();
	}

	/**
	 * Retrieves a stream over all batches of a product
	 * 
	 * @param product
	 *            The product
	 * @return All batches
	 */
	public Stream<Batch> getBatchesForProduct(Product product) {
		return _warehouse.getBatchesForProduct(product);
	}

	/**
	 * Retrieves a stream over all partners
	 * 
	 * @return All partners
	 */
	public Stream<Partner> getPartners() {
		return _warehouse.getPartners();
	}

	/**
	 * Retrieves a partner given it's id
	 * 
	 * @param partnerId
	 *            Id of the partner
	 * @return The partner, if they exist
	 */
	public Optional<Partner> getPartner(String partnerId) {
		return _warehouse.getPartner(partnerId);
	}

	/**
	 * Retrieves a partner's purchases
	 * 
	 * @param partner
	 *            The partner to get the purchases
	 * @return All purchases of the partner
	 */
	public Stream<Purchase> getPartnerPurchases(Partner partner) {
		return _warehouse.getPartnerPurchases(partner);
	}

	/**
	 * Retrieves a partner's sales
	 * 
	 * @param partner
	 *            The partner to get the sales
	 * @return All sales of the partner
	 */
	public Stream<Sale> getPartnerSales(Partner partner) {
		return _warehouse.getPartnerSales(partner);
	}

	/**
	 * Retrieves a partner's breakdown transactions
	 * 
	 * @param partner
	 *            The partner to get the breakdown transactions
	 * @return All breakdown transactions of the partner
	 */
	public Stream<BreakdownTransaction> getPartnerBreakdownTransactions(Partner partner) {
		return _warehouse.getPartnerBreakdownTransactions(partner);
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
	public Partner registerPartner(String id, String name, String address) throws PartnerAlreadyExistsException {
		var partner = _warehouse.registerPartner(id, name, address);
		_warehouseIsDirty = true;
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
	public void togglePartnerNotifications(Partner partner, Product product) {
		_warehouse.togglePartnerNotifications(partner, product);
		_warehouseIsDirty = true;
	}

	/**
	 * Retrieves a stream over all transactions
	 * 
	 * @return All transactions
	 */
	public Stream<Transaction> getTransactions() {
		return _warehouse.getTransactions();
	}

	/**
	 * Retrieves a transaction given it's id
	 * 
	 * @param id
	 *            The id of the transaction
	 * @return The transaction, if valid
	 */
	public Optional<Transaction> getTransaction(int id) {
		return _warehouse.getTransaction(id);
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
	public Purchase registerPurchase(Partner partner, Product product, int quantity, double unitPrice) {
		var purchase = _warehouse.registerPurchase(partner, product, quantity, unitPrice);
		_warehouseIsDirty = true;
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
	 * @throws InsufficientProductsException
	 *             If there isn't enough quantity of the product for the sale.
	 */
	public void registerSale(Partner partner, Product product, int quantity, int deadline)
			throws InsufficientProductsException {
		_warehouse.registerSale(partner, product, quantity, deadline);
		_warehouseIsDirty = true;
	}

	/**
	 * Pays a transaction if it's a sale
	 * 
	 * @param sale
	 *            The transaction to pay
	 */
	public void paySale(Transaction sale) {
		_warehouse.paySale(sale);
		_warehouseIsDirty = true;
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
	 * @throws InsufficientProductsException
	 *             If there aren't enough products to break down
	 */
	public void registerBreakdown(Partner partner, Product product, int quantity) throws InsufficientProductsException {
		// If ` product` isn't derived, return
		var productAsDerived = product.getAsDerived();
		if (productAsDerived.isEmpty()) {
			return;
		}

		// Else register it
		_warehouse.registerBreakdown(partner, productAsDerived.get(), quantity);
		_warehouseIsDirty = true;
	}

	/**
	 * Retrieves the total quantity of a product
	 * 
	 * @param product
	 *            The product to get the quantity of
	 * @return The quantity of the product
	 */
	public int productTotalQuantity(Product product) {
		return _warehouse.productTotalQuantity(product);
	}

	/**
	 * Retrieves a product comparator by it's id
	 * 
	 * @return A product comparator by id
	 */
	public Comparator<Product> productComparator() {
		return _warehouse.productComparator();
	}

	/**
	 * Retrieves a batch comparator by it's product id, partner id, unit price and then quantity
	 * 
	 * @return A batch comparator
	 */
	public Comparator<Batch> batchComparator() {
		return _warehouse.batchComparator();
	}

	/**
	 * Retrieves a batch filter by it's partner
	 * 
	 * @param partner
	 *            The partner to filter by
	 * @return A batch filter by partner
	 */
	public Predicate<Batch> batchFilterPartner(Partner partner) {
		return _warehouse.batchFilterPartner(partner);
	}

	/**
	 * Retrieves a batch filter by it's product
	 * 
	 * @param product
	 *            The product to filter by
	 * @return A batch filter by product
	 */
	public Predicate<Batch> batchFilterProduct(Product product) {
		return _warehouse.batchFilterProduct(product);
	}

	/**
	 * Retrieves a batch filter by it's price
	 * 
	 * @param predicate
	 *            The predicate for the filter
	 * @return A batch filter by price predicate
	 */
	public Predicate<Batch> batchFilterPrice(Predicate<Double> predicate) {
		return _warehouse.batchFilterPrice(predicate);
	}

	/**
	 * Retrieves a partner comparator by it's id
	 * 
	 * @return A partner comparator by id
	 */
	public Comparator<Partner> partnerComparator() {
		return _warehouse.partnerComparator();
	}

	/**
	 * Retrieves a sale filter for paid sales
	 * 
	 * @return A sale filter by if they're paid
	 */
	public Predicate<Sale> saleFilterPaid() {
		return _warehouse.saleFilterPaid();
	}

	/**
	 * Clears all pending notifications from a partner and returns them
	 * 
	 * @param partner
	 *            The partner to clear notifications
	 * @return All pending notifications
	 */
	public List<Notification> clearPendingPartnerNotifications(Partner partner) {
		var notifications = partner.clearPendingNotifications();
		_warehouseIsDirty |= !notifications.isEmpty();
		return notifications;
	}

	/**
	 * Formats a value
	 * 
	 * @param <T>
	 *            The type of the value to format
	 * @param value
	 *            The value to format
	 * @return The value formatted
	 */
	public <T extends WarehouseFormattable> String format(T value) {
		return value.format(this);
	}
}
