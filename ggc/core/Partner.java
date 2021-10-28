package ggc.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/// Partner
public class Partner implements Serializable {
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

	// Note: Package private to ensure we don't construct it outside of `core`.
	Partner(String name, String address, String id) {
		_name = name;
		_address = address;
		_id = id;
		_status = PartnerStatus.Normal;
		_points = 0.0;
		_purchases = new ArrayList<>();
		_sales = new ArrayList<>();
		_pendingNotifications = new ArrayList<>();
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

	/// Returns the partner's points
	double getPoints() {
		return _points;
	}

	/// Returns a stream of the partner's purchases
	Stream<Purchase> getPurchases() {
		return _purchases.stream();
	}

	/// Returns a stream of the partner's sales
	Stream<Sale> getSales() {
		return _sales.stream();
	}

	/// Returns all pending notifications and clears them
	List<Notification> clearPendingNotifications() {
		List<Notification> notifications = _pendingNotifications;
		_pendingNotifications = new ArrayList<>();
		return notifications;
	}

	/// Partner status
	enum PartnerStatus {
		Normal, Selection, Elite;

		public String toString() {
			switch (this) {
			case Normal:
				return "NORMAL";
			case Selection:
				return "SELECTION";
			case Elite:
				return "ELITE";
			default:
				throw new RuntimeException("Unknown partner status");
			}
		}
	}
}
