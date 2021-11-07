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

	/// If any changes were performed on the warehouse since the last save
	private boolean _warehouseIsDirty;

	/// Saves the file into the associated file, or, if inexistent, gets it from `supplier`
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

	/// Loads from `fileName` and then associated filename
	public void loadFrom(String fileName) throws IOException, ClassNotFoundException {
		try (var file = new FileInputStream(fileName); var stream = new ObjectInputStream(file)) {
			// Try to read the warehouse
			// Note: We set dirty to false, as the new file is now the current state of the warehouse
			_warehouse = (Warehouse) stream.readObject();
			_warehouseIsDirty = false;
			_fileName = Optional.of(fileName);
		}
	}

	/// Imports the file `textFile` onto the warehouse.
	public void importFile(String textFile) throws ImportFileException {
		// Import and set ourselves as dirty
		try {
			_warehouse.importFile(textFile);
			_warehouseIsDirty = true;
		} catch (IOException | BadEntryException | ParsingException e) {
			throw new ImportFileException(textFile, e);
		}
	}

	/// Returns if any changes were made to the warehouse
	public boolean isWarehouseDirty() {
		return _warehouseIsDirty;
	}

	/// Returns the current date
	public int getDate() {
		return _warehouse.getDate();
	}

	/// Advances the current date
	public void advanceDate(int offset) {
		_warehouse.advanceDate(offset);
		_warehouseIsDirty = true;
	}

	/// Returns the available balance
	public double getAvailableBalance() {
		return _warehouse.getAvailableBalance();
	}

	/// Returns the accounting balance
	public double getAccountingBalance() {
		return _warehouse.getAccountingBalance();
	}

	/// Returns a stream over all products
	public Stream<Product> getProducts() {
		return _warehouse.getProducts();
	}

	/// Returns a product given it's id
	public Optional<Product> getProduct(String productId) {
		return _warehouse.getProduct(productId);
	}

	/// Registers a simple product given it's id
	public Product registerProduct(String productId) throws ProductAlreadyExistsException {
		var product = _warehouse.registerProduct(productId);
		_warehouseIsDirty = true;
		return product;
	}

	/// Registers a derived product given it's id, alpha and all components by id
	public Product registerDerivedProduct(String productId, double costFactor,
			Stream<Pair<String, Integer>> recipeProducts)
			throws ProductAlreadyExistsException, UnknownProductIdException {
		var product = _warehouse.registerDerivedProduct(productId, costFactor, recipeProducts);
		_warehouseIsDirty = true;
		return product;
	}

	/// Returns a stream over all batches
	public Stream<Batch> getBatches() {
		return _warehouse.getBatches();
	}

	/// Returns a stream over all partners
	public Stream<Partner> getPartners() {
		return _warehouse.getPartners();
	}

	/// Returns a partner given it's id
	public Optional<Partner> getPartner(String partnerId) {
		return _warehouse.getPartner(partnerId);
	}

	/// Returns a partner's purchases
	public Stream<Purchase> getPartnerPurchases(Partner partner) {
		return _warehouse.getPartnerPurchases(partner);
	}

	/// Returns a partner's sales
	public Stream<Sale> getPartnerSales(Partner partner) {
		return _warehouse.getPartnerSales(partner);
	}

	/// Registers a new partner
	public Partner registerPartner(String partnerId, String partnerName, String partnerAddress)
			throws PartnerAlreadyExistsException {
		var partner = _warehouse.registerPartner(partnerId, partnerName, partnerAddress);
		_warehouseIsDirty = true;
		return partner;
	}

	/// Toggles a partner's product notifications
	public void togglePartnerNotifications(Partner partner, Product product) {
		_warehouse.togglePartnerNotifications(partner, product);
		_warehouseIsDirty = true;
	}

	/// Returns a stream over all transactions
	public Stream<Transaction> getTransactions() {
		return _warehouse.getTransactions();
	}

	/// Returns a transaction given it's id
	public Optional<Transaction> getTransaction(int transactionId) {
		return _warehouse.getTransaction(transactionId);
	}

	/// Registers a new purchase
	public Purchase registerPurchase(Partner partner, Product product, int quantity, double unitPrice) {
		var purchase = _warehouse.registerPurchase(partner, product, quantity, unitPrice);
		_warehouseIsDirty = true;
		return purchase;
	}

	/// Registers a new sale
	public Sale registerSale(Partner partner, Product product, int quantity, int deadline) {
		var sale = _warehouse.registerSale(partner, product, quantity, deadline);
		_warehouseIsDirty = true;
		return sale;
	}

	/// Registers a new breakdown
	public Sale registerBreakdown(Partner partner, Product product, int quantity) {
		var sale = _warehouse.registerBreakdown(partner, product, quantity);
		_warehouseIsDirty = true;
		return sale;
	}

	/// Returns the max price of a product
	///
	/// Returns `Optional.EMPTY` if `product` does not exist
	/// in any batch in the warehouse.
	public Optional<Double> productMaxPrice(Product product) {
		return _warehouse.productMaxPrice(product);
	}

	/// Returns the total quantity of a product
	public int productTotalQuantity(Product product) {
		return _warehouse.productTotalQuantity(product);
	}

	/// Returns a batch comparator by product id
	public static Comparator<Product> productComparator() {
		return Warehouse.productComparator();
	}

	/// Returns a batch comparator by product id, partner id, unit price and then quantity
	public static Comparator<Batch> batchComparator() {
		return Warehouse.batchComparator();
	}

	/// Returns a batch filter by it's partner id
	public static Predicate<Batch> batchFilterPartnerId(String partnerId) {
		return Warehouse.batchFilterPartnerId(partnerId);
	}

	/// Returns a batch filter by it's product id
	public static Predicate<Batch> batchFilterProductId(String productId) {
		return Warehouse.batchFilterProductId(productId);
	}

	/// Returns a batch filter by it's price
	public static Predicate<Batch> batchFilterPrice(Predicate<Double> predicate) {
		return Warehouse.batchFilterPrice(predicate);
	}

	/// Returns a partner comparator
	public static Comparator<Partner> partnerComparator() {
		return Warehouse.partnerComparator();
	}

	/// Returns a sale filter for paid sales
	// TODO: Make all of these non-static.
	public static Predicate<Sale> saleFilterPaid() {
		return Warehouse.saleFilterPaid();
	}

	/// Clears pending partner notifications and returns them
	public List<Notification> clearPendingPartnerNotifications(Partner partner) {
		var notifications = partner.clearPendingNotifications();
		_warehouseIsDirty |= !notifications.isEmpty();
		return notifications;
	}

	/// Formats a value
	public <T extends WarehouseFormattable> String format(T value) {
		return value.format(new PackagePrivateWarehouseManagerWrapper(this));
	}
}
