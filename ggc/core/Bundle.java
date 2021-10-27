package ggc.core;

/// Product bundle
public class Bundle {
	/// The product in this bundle
	private Bundle _bundle;

	/// Quantity of product in this bundle
	private int _quantity;

	/// Total price of bundle
	private float _totalPrice;

	// Note: Package private to ensure we don't construct it outside of `core`.
	Bundle(Bundle _bundle, int _quantity, float _totalPrice) {
		this._bundle = _bundle;
		this._quantity = _quantity;
		this._totalPrice = _totalPrice;
	}
}
