package ggc.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import ggc.core.exception.BadEntryException;
import ggc.core.exception.ImportFileException;
import ggc.core.exception.ParsingException;
import ggc.core.util.StreamIterator;

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

	/**
	 * @param textFile
	 * @throws ImportFileException
	 */
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

	/// Returns a stream over all products
	public Stream<Product> getProducts() {
		return _warehouse.getProducts();
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

	/// Registers a new partner
	///
	/// Returns the new partner if successful, or empty is a partner with the same name exists
	public Optional<Partner> registerPartner(String partnerId, String partnerName, String partnerAddress) {
		var partner = _warehouse.registerPartner(partnerId, partnerName, partnerAddress);
		_warehouseIsDirty |= partner.isPresent();
		return partner;
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

	/// Formats a product according to it's availability
	public String formatProduct(Product product) {
		// Get the product's max price and total quantity
		// Note: If no batches exist, there is no max price, and so we'll return 0
		double maxPrice = productMaxPrice(product).orElse(0.0);
		int quantity = productTotalQuantity(product);

		// Create the base string
		StringBuilder repr = new StringBuilder(String.format("%s|%.0f|%d", product.getId(), maxPrice, quantity));

		// Then add any extra fields the product may have
		for (var field : StreamIterator.streamIt(product.extraFormatFields())) {
			repr.append("|");
			repr.append(field);
		}

		return repr.toString();
	}

	/// Returns a batch comparator by product id
	public static Comparator<Product> productComparator() {
		return Comparator.comparing(Product::getId);
	}

	/// Formats a batch according to it's availability
	public String formatBatch(Batch batch) {
		return String.format("%s|%s|%.0f|%d", batch.getProduct().getId(), batch.getPartner().getId(),
				batch.getUnitPrice(), batch.getQuantity());
	}

	/// Returns a batch comparator by product id, partner id, unit price and then quantity
	public static Comparator<Batch> batchComparator() {
		return Comparator.<Batch, String>comparing(batch -> batch.getProduct().getId())
				.thenComparing(batch -> batch.getPartner().getId()).thenComparing(Batch::getUnitPrice)
				.thenComparing(Batch::getQuantity);
	}

	/// Formats a partner
	public String formatPartner(Partner partner) {
		double totalPurchases = partner.getPurchases().mapToDouble(Transaction::getTotalPrice).sum();
		double totalSales = 0.0;
		double totalSalesPaid = 0.0;
		for (var sale : StreamIterator.streamIt(partner.getSales())) {
			totalSales += sale.getTotalPrice();
			totalSalesPaid += sale.isPaid() ? sale.getTotalPrice() : 0.0;
		}

		return String.format("%s|%s|%s|%s|%.0f|%.0f|%.0f|%.0f", partner.getId(), partner.getName(),
				partner.getAddress(), partner.getStatus(), partner.getPoints(), totalPurchases, totalSales,
				totalSalesPaid);
	}

	/// Returns a partner comparator
	public static Comparator<Partner> partnerComparator() {
		// Note: Id is unique, so we don't need to compare by anything else
		return Comparator.comparing(Partner::getId);
	}

	/// Clears pending partner notifications and returns them
	public List<Notification> clearPendingPartnerNotifications(Partner partner) {
		var notifications = partner.clearPendingNotifications();
		_warehouseIsDirty |= !notifications.isEmpty();
		return notifications;
	}

	/// Formats a notification
	public String formatNotification(Notification notification) {
		Batch batch = notification.getBatch();
		return String.format("%s|%s|%.0f", notification.getType(), batch.getProduct(),
				notification.getBatch().getUnitPrice());
	}
}
