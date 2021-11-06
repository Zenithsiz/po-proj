package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.DuplicatePartnerKeyException;
import ggc.core.WarehouseManager;
import ggc.core.exception.PartnerAlreadyExistsException;

/**
 * Register new partner.
 */
class DoRegisterPartner extends Command<WarehouseManager> {
	private static final String PARTNER_ID = "partnerId";
	private static final String PARTNER_NAME = "partnerName";
	private static final String PARTNER_ADDRESS = "partnerAddress";

	DoRegisterPartner(WarehouseManager receiver) {
		super(Label.REGISTER_PARTNER, receiver);

		super.addStringField(PARTNER_ID, Message.requestPartnerKey());
		super.addStringField(PARTNER_NAME, Message.requestPartnerName());
		super.addStringField(PARTNER_ADDRESS, Message.requestPartnerAddress());
	}

	@Override
	public void execute() throws CommandException {
		// Try to create the partner
		String partnerId = super.stringField(PARTNER_ID);
		String partnerName = super.stringField(PARTNER_NAME);
		String partnerAddress = super.stringField(PARTNER_ADDRESS);

		try {
			_receiver.registerPartner(partnerId, partnerName, partnerAddress);
		} catch (PartnerAlreadyExistsException e) {
			throw new DuplicatePartnerKeyException(partnerId);
		}
	}

}
