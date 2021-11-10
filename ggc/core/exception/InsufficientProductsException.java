package ggc.core.exception;

/** Exception thrown when a product is unavailable. */
public class InsufficientProductsException extends Exception {
	/** Serial number for serialization. */
	private static final long serialVersionUID = 2021_11_09_14_47L;

	// Product id
	private String _productId;

	// Quantity requested
	private int _quantityRequested;

	// Quantity available
	private int _quantityAvailable;

	public InsufficientProductsException(String productId, int quantityRequested, int quantityAvailable) {
		_productId = productId;
		_quantityRequested = quantityRequested;
		_quantityAvailable = quantityAvailable;
	}

	/// Returns the product id
	public String getProductId() {
		return _productId;
	}

	/// Returns the quantity requested
	public int getQuantityRequested() {
		return _quantityRequested;
	}

	/// Returns the quantity available
	public int getQuantityAvailable() {
		return _quantityAvailable;
	}
}
