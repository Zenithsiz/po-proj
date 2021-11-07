package ggc.core;

import java.io.Serializable;

public class Notification implements Serializable, WarehouseFormattable {
	/// Batch of the product we're notifying about
	private Batch _batch;

	/// Notification type
	private String _type;

	Notification(Batch batch, String type) {
		_batch = batch;
		_type = type;
	}

	/// Returns the batch of this notification
	Batch getBatch() {
		return _batch;
	}

	/// Returns the type of this notification
	String getType() {
		return _type;
	}

	@Override
	public String format(PackagePrivateWarehouseManagerWrapper warehouseManager) {
		return String.format("%s|%s|%.0f", _type, _batch.getProduct(), _batch.getUnitPrice());
	}
}
