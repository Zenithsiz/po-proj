package ggc.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import ggc.core.partnerStatus.NormalPartnerStatus;

/**
 * A partner.
 * 
 * Partners are the entities that can trade with the warehouse to either sell or buy products.
 */
public class Partner implements Serializable, WarehouseFormattable {
	/** Serial number for serialization. */
	private static final long serialVersionUID = 2021_10_27_03_12L;

	/** Id of the partner */
	private String _id;

	/** Name of the partner */
	private String _name;

	/** Address of the partner */
	private String _address;

	/** Current partner status */
	private PartnerStatus _status;

	/** Partner points */
	private double _points;

	/** All purchases */
	private List<Purchase> _purchases;

	/** All sales */
	private List<Sale> _sales;

	/** All pending notifications */
	private List<Notification> _pendingNotifications;

	/** Blacklisted product notifications */
	private Set<Product> _blacklistedProductNotifications;

	/**
	 * Creates a new partner
	 * 
	 * @param id
	 *            Id of the partner
	 * @param name
	 *            Name of the partner
	 * @param address
	 *            Address of the partner
	 */
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

	/**
	 * Retrieves this partner's id
	 * 
	 * @return The id of this partner
	 */
	String getId() {
		return _id;
	}

	/**
	 * Retrieves this partner's name
	 * 
	 * @return The name of this partner
	 */
	String getName() {
		return _name;
	}

	/**
	 * Retrieves this partner's address
	 * 
	 * @return The address of this partner
	 */
	String getAddress() {
		return _address;
	}

	/**
	 * Retrieves this partner's status
	 * 
	 * @return The status of this partner
	 */
	PartnerStatus getStatus() {
		return _status;
	}

	/**
	 * Retrieves this partner's points
	 * 
	 * @return The points of this partner
	 */
	double getPoints() {
		return _points;
	}

	/** Attempts to promote this partner, if they have enough points */
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

	/**
	 * Retrieves this partner's purchases
	 * 
	 * @return The purchases of this partner
	 */
	Stream<Purchase> getPurchases() {
		return _purchases.stream();
	}

	/**
	 * Adds a purchase to this partner
	 * 
	 * @param purchase
	 *            The purchase to add
	 */
	void addPurchase(Purchase purchase) {
		_purchases.add(purchase);
	}

	/**
	 * Retrieves this partner's sales
	 * 
	 * @return The sales of this partner
	 */
	Stream<Sale> getSales() {
		return _sales.stream();
	}

	/**
	 * Adds a sale to this partner
	 * 
	 * @param sale
	 *            The sale to add
	 */
	void addCreditSale(CreditSale sale) {
		_sales.add(sale);
	}

	/**
	 * Adds a breakdown sale to this partner
	 * 
	 * @param sale
	 *            The sale to add
	 */
	void addBreakdownSale(BreakdownSale sale) {
		_sales.add(sale);

		// Then add our points and attempt to promote
		_points += 10 * sale.getTotalPrice();
		tryPromotePartner();
	}

	/**
	 * Pays a sale
	 * 
	 * @param sale
	 *            The sale to pay
	 * @param date
	 *            The date to pay the sale at
	 * @return The amount paid
	 * 
	 */
	double paySale(CreditSale sale, int date) {
		// Pay and get the paid amount
		var paidAmount = sale.pay(date);

		// Add the point and check for promotion if on time, else demote
		if (date < sale.getPaymentDeadline()) {
			_points += 10 * sale.getTotalPrice();
			tryPromotePartner();
		} else {
			_status = _status.demote();
		}

		return paidAmount;
	}

	/**
	 * Adds a notification
	 * 
	 * @param notification
	 *            The notification to add
	 */
	void addNotifications(Notification notification) {
		_pendingNotifications.add(notification);
	}

	/**
	 * Clears all pending notifications and returns them
	 * 
	 * @return All pending notifications
	 */
	List<Notification> clearPendingNotifications() {
		var notifications = _pendingNotifications;
		_pendingNotifications = new ArrayList<>();
		return notifications;
	}

	/**
	 * Toggle if a product is blacklisted
	 * 
	 * @param product
	 *            The product to toggle
	 */
	void toggleIsProductNotificationBlacklisted(Product product) {
		// Try to remove it and, if it doesn't exist, add it.
		if (!_blacklistedProductNotifications.remove(product)) {
			_blacklistedProductNotifications.add(product);
		}
	}

	/**
	 * Returns if a product notification is blacklisted
	 * 
	 * @param product
	 *            The product to check
	 * @return If blacklisted
	 */
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

		return String.format("%s|%s|%s|%s|%d|%d|%d|%d", _id, _name, _address, _status.format(warehouseManager),
				Math.round(_points), Math.round(totalPurchases), Math.round(totalSales), Math.round(totalSalesPaid));
	}
}
