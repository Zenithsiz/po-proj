package ggc.core.notification;

import ggc.core.Batch;
import ggc.core.Notification;

/**
 * A "BARGAIN" notification
 * 
 * This notifications is sent whenever a product hits a new minimum all-low price.
 */
public class BargainNotification extends Notification {
	/**
	 * Creates a new notification
	 * 
	 * @param batch
	 *            The batch that prompted this notification
	 */
	public BargainNotification(Batch batch) {
		super(batch);
	}

	@Override
	protected String getType() {
		return "BARGAIN";
	}
}
