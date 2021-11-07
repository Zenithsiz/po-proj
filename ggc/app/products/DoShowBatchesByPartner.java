package ggc.app.products;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.core.WarehouseManager;

/**
 * Show batches supplied by partner.
 */
class DoShowBatchesByPartner extends Command<WarehouseManager> {
	private static final String PARTNER_ID = "partnerId";

	DoShowBatchesByPartner(WarehouseManager receiver) {
		super(Label.SHOW_BATCHES_SUPPLIED_BY_PARTNER, receiver);

		super.addStringField(PARTNER_ID, Message.requestPartnerKey());
	}

	@Override
	public final void execute() throws CommandException {
		String partnerId = super.stringField(PARTNER_ID);
		ShowBatches.executeFilter(_receiver, _display, _receiver.batchFilterPartnerId(partnerId));
	}

}
