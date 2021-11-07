package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.UnknownPartnerKeyException;
import ggc.core.WarehouseManager;
import ggc.core.util.StreamIterator;

/**
 * Show all transactions for a specific partner.
 */
class DoShowPartnerAcquisitions extends Command<WarehouseManager> {
	private static final String PARTNER_ID = "partnerId";

	DoShowPartnerAcquisitions(WarehouseManager receiver) {
		super(Label.SHOW_PARTNER_ACQUISITIONS, receiver);

		super.addStringField(PARTNER_ID, Message.requestPartnerKey());
	}

	@Override
	public void execute() throws CommandException {
		// Get the partner
		var partnerId = super.stringField(PARTNER_ID);
		var partner = _receiver.getPartner(partnerId).orElseThrow(() -> new UnknownPartnerKeyException(partnerId));

		// Then get their purchases and display them
		for (var purchase : StreamIterator.streamIt(_receiver.getPartnerPurchases(partner))) {
			_display.addLine(_receiver.format(purchase));
		}

		_display.display();
	}

}
