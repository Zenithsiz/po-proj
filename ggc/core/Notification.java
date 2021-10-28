package ggc.core;

public class Notification {
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
}
