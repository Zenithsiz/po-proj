package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.UnknownPartnerKeyException;
import ggc.core.Partner;
import ggc.core.WarehouseManager;

/**
 * Show partner.
 */
class DoShowPartner extends Command<WarehouseManager> {
	private static final String PARTNER_ID = "partnerId";

	DoShowPartner(WarehouseManager receiver) {
		super(Label.SHOW_PARTNER, receiver);

		super.addStringField(PARTNER_ID, Message.requestPartnerKey());
	}

	@Override
	public void execute() throws CommandException {
		// Get the partner
		String partnerId = super.stringField(PARTNER_ID);
		Partner partner = _receiver.getPartner(partnerId).orElseThrow(() -> new UnknownPartnerKeyException(partnerId));

		// Display them
		_display.addLine(_receiver.formatPartner(partner));

		// Then display their pending notifications
		for (var notification : _receiver.clearPendingPartnerNotifications(partner)) {
			_display.addLine(_receiver.formatNotification(notification));
		}

		_display.display();
	}

}
