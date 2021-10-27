package ggc.core.exception;

public class UnknownProductIdException extends Exception {

	/// Product id
	private String _productId;

	public UnknownProductIdException(String productId) {
		super("Unknown product id: " + productId);
		_productId = productId;
	}

	public String getProductId() {
		return _productId;
	}
}
