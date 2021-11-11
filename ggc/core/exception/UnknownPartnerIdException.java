package ggc.core.exception;

public class UnknownPartnerIdException extends Exception {

	/** Partner id */
	private String _partnerId;

	public UnknownPartnerIdException(String partnerId) {
		super("Unknown Partner id: " + partnerId);
		_partnerId = partnerId;
	}

	public String getPartnerId() {
		return _partnerId;
	}
}
