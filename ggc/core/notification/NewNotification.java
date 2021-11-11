package ggc.core.notification;

import ggc.core.Batch;
import ggc.core.Notification;

/**
 * A "NEW" notification
 * 
 * This notifications is sent whenever a product that was out of stock enters back in stock.
 */
public class NewNotification extends Notification {
	/**
	 * Creates a new notification
	 * 
	 * @param batch
	 *            The batch that prompted this notification
	 */
	public NewNotification(Batch batch) {
		super(batch);
	}

	@Override
	protected String getType() {
		return "NEW";
	}
}
