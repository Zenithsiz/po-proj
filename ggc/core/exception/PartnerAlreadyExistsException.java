package ggc.core.exception;

public class PartnerAlreadyExistsException extends Exception {

	/// Partner id
	private String _partnerId;

	public PartnerAlreadyExistsException(String partnerId) {
		super("Partner already exists: " + partnerId);
		_partnerId = partnerId;
	}

	public String getPartnerId() {
		return _partnerId;
	}
}
