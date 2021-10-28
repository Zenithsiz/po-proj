package ggc.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Optional;
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
	private String _fileName = null;

	/** The warehouse itself. */
	private Warehouse _warehouse = new Warehouse();

	/// Returns the current file name
	public String fileName() {
		return _fileName;
	}

	/**
	 * @@throws IOException
	 */
	public void save(String fileName) throws IOException {
		// Open the file to save, and create an output stream from it
		try (var file = new FileOutputStream(fileName); var stream = new ObjectOutputStream(file)) {
			// Write the warehouse to file
			stream.writeObject(_warehouse);
		}

		_fileName = fileName;
	}

	/**
	 * @@param filename
	 * @@throws IOException
	 */
	public void load(String fileName) throws IOException, ClassNotFoundException {
		try (var file = new FileInputStream(fileName); var stream = new ObjectInputStream(file)) {
			// Try to read the warehouse
			_warehouse = (Warehouse) stream.readObject();
		}

		_fileName = fileName;
	}

	/**
	 * @param textFile
	 * @throws ImportFileException
	 */
	public void importFile(String textFile) throws ImportFileException {
		try {
			_warehouse.importFile(textFile);
		} catch (IOException | BadEntryException | ParsingException e) {
			throw new ImportFileException(textFile, e);
		}
	}

	/// Returns the current date
	public int getDate() {
		return _warehouse.getDate();
	}

	/// Advances the current date
	public void advanceDate(int offset) {
		_warehouse.advanceDate(offset);
	}

	/// Returns a stream over all products
	public Stream<Product> getProducts() {
		return _warehouse.getProducts();
	}

	/// Returns a stream over all batches
	public Stream<Batch> getBatches() {
		return _warehouse.getBatches();
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

}
