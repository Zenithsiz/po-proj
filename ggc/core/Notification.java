package ggc.core;

import java.io.Serializable;

/** A notification */
// TODO: Create subclasses for this instead of having `_type`
public class Notification implements Serializable, WarehouseFormattable {
	/** The batch we're notifying for. */
	private Batch _batch;

	/** TODO: Remove */
	private String _type;

	/**
	 * Creates a new notification
	 * 
	 * @param batch
	 *            The batch that prompted this notification
	 * @param type
	 *            TODO: Remove
	 */
	Notification(Batch batch, String type) {
		_batch = batch;
		_type = type;
	}

	/**
	 * Retrieves this notification's batch
	 * 
	 * @return The batch that prompted notification
	 */
	Batch getBatch() {
		return _batch;
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		return String.format("%s|%s|%.0f", _type, _batch.getProduct().getId(), _batch.getUnitPrice());
	}
}
