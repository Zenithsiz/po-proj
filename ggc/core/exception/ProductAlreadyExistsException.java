package ggc.core.exception;

public class ProductAlreadyExistsException extends Exception {

	/// Product id
	private String _productId;

	public ProductAlreadyExistsException(String productId) {
		super("Product id already exists: " + productId);
		_productId = productId;
	}

	public String getProductId() {
		return _productId;
	}
}
