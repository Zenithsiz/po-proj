package ggc.core;

import java.io.Serializable;

/**
 * A notification
 */
public abstract class Notification implements Serializable, WarehouseFormattable {
	/** The batch we're notifying for. */
	private Batch _batch;

	/**
	 * Creates a new notification
	 * 
	 * @param batch
	 *            The batch that prompted this notification
	 */
	protected Notification(Batch batch) {
		_batch = batch;
	}

	/**
	 * Retrieves this notification's batch
	 * 
	 * @return The batch that prompted notification
	 */
	protected Batch getBatch() {
		return _batch;
	}

	/**
	 * Retrieves this notification's type
	 * 
	 * @return The type of this notification
	 */
	protected abstract String getType();

	@Override
	public String format(WarehouseManager warehouseManager) {
		return String.format("%s|%s|%d", getType(), _batch.getProduct().getId(), Math.round(_batch.getUnitPrice()));
	}
}
