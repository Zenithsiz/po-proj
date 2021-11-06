package ggc.core;

import java.util.Optional;
import java.util.stream.Stream;

/// A warehouse with only it's non-modifying functions available
public class ConstWarehouse {
	/// The warehouse we're wrapping
	private Warehouse _warehouse;

	public ConstWarehouse(Warehouse _warehouse) {
		this._warehouse = _warehouse;
	}

	int getDate() {
		return _warehouse.getDate();
	}

	/// Returns a stream over all products
	Stream<Product> getProducts() {
		return _warehouse.getProducts();
	}

	Optional<Product> getProduct(String productId) {
		return _warehouse.getProduct(productId);
	}

	Stream<Batch> getBatches() {
		return _warehouse.getBatches();
	}

	Stream<Partner> getPartners() {
		return _warehouse.getPartners();
	}

	Optional<Partner> getPartner(String partnerId) {
		return _warehouse.getPartner(partnerId);
	}

	Stream<Transaction> getTransactions() {
		return _warehouse.getTransactions();
	}

	Optional<Transaction> getTransaction(int transactionId) {
		return _warehouse.getTransaction(transactionId);
	}

	Optional<Double> productMaxPrice(Product product) {
		return _warehouse.productMaxPrice(product);
	}

	int productTotalQuantity(Product product) {
		return _warehouse.productTotalQuantity(product);
	}
}
