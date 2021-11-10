package ggc.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import ggc.core.partnerStatus.NormalPartnerStatus;

/// Partner
public class Partner implements Serializable, WarehouseFormattable {
	/// Serial number for serialization.
	private static final long serialVersionUID = 2021_10_27_03_12L;

	/// Name
	private String _name;

	/// Address
	private String _address;

	/// Id
	private String _id;

	/// Status
	private PartnerStatus _status;

	/// Points
	private double _points;

	/// All purchases
	private List<Purchase> _purchases;

	/// All sales
	private List<Sale> _sales;

	/// All pending notifications
	private List<Notification> _pendingNotifications;

	/// Blacklisted product notifications
	private Set<Product> _blacklistedProductNotifications;

	// Note: Package private to ensure we don't construct it outside of `core`.
	Partner(String id, String name, String address) {
		_name = name;
		_address = address;
		_id = id;
		_status = new NormalPartnerStatus();
		_points = 0.0;
		_purchases = new ArrayList<>();
		_sales = new ArrayList<>();
		_pendingNotifications = new ArrayList<>();
		_blacklistedProductNotifications = new HashSet<>();
	}

	/// Returns the partner's id
	String getId() {
		return _id;
	}

	/// Returns the partner's name
	String getName() {
		return _name;
	}

	/// Returns the partner's address
	String getAddress() {
		return _address;
	}

	/// Returns the partner's status
	PartnerStatus getStatus() {
		return _status;
	}

	/// Attempts to promote this partner
	private void tryPromotePartner() {
		// Keep promoting until we can't promote anymore
		// TODO: Check if this should be recursive
		while (true) {
			var promotion = _status.checkPromotion(_points);
			if (promotion.isEmpty()) {
				break;
			}
			_status = promotion.get();
		}
	}

	/// Returns the partner's points
	double getPoints() {
		return _points;
	}

	/// Returns a stream of the partner's purchases
	Stream<Purchase> getPurchases() {
		return _purchases.stream();
	}

	/// Adds a purchase to this partner
	public void addPurchase(Purchase purchase) {
		_purchases.add(purchase);
	}

	/// Returns a stream of the partner's sales
	Stream<Sale> getSales() {
		return _sales.stream();
	}

	/// Adds a sale to this partner
	public void addSale(Sale sale) {
		_sales.add(sale);
	}

	/// Pays a sale and returns the value paid
	public double paySale(CreditSale sale, int date) {
		// Pay and get the paid amount
		var paidAmount = sale.pay(date);

		// Add the point and check for promotion
		// TODO: Only add it if it isn't late
		_points += 10 * sale.getTotalPrice();
		tryPromotePartner();

		return paidAmount;
	}

	/// Adds a notification
	public void addNotifications(Notification notification) {
		_pendingNotifications.add(notification);
	}

	/// Returns all pending notifications and clears them
	List<Notification> clearPendingNotifications() {
		var notifications = _pendingNotifications;
		_pendingNotifications = new ArrayList<>();
		return notifications;
	}

	/// Toggle if a product is blacklisted
	void toggleIsProductNotificationBlacklisted(Product product) {
		// Try to remove it and, if it doesn't exist, add it.
		if (!_blacklistedProductNotifications.remove(product)) {
			_blacklistedProductNotifications.add(product);
		}
	}

	/// Returns if a product notification is blacklisted
	boolean isProductNotificationBlacklisted(Product product) {
		return _blacklistedProductNotifications.contains(product);
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		double totalPurchases = _purchases.stream().mapToDouble(Transaction::getTotalPrice).sum();
		double totalSales = 0.0;
		double totalSalesPaid = 0.0;
		for (var sale : _sales) {
			totalSales += sale.getTotalPrice();
			totalSalesPaid += sale.isPaid() ? sale.getTotalPrice() : 0.0;
		}

		return String.format("%s|%s|%s|%s|%.0f|%.0f|%.0f|%.0f", _id, _name, _address, _status.format(warehouseManager),
				_points, totalPurchases, totalSales, totalSalesPaid);
	}
}
