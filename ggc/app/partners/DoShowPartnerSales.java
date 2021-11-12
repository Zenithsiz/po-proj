package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.UnknownPartnerKeyException;
import ggc.core.WarehouseManager;
import static ggc.core.util.StreamIterator.streamIt;

/**
 * Show all transactions for a specific partner.
 */
class DoShowPartnerSales extends Command<WarehouseManager> {
	private static final String PARTNER_ID = "partnerId";

	DoShowPartnerSales(WarehouseManager receiver) {
		super(Label.SHOW_PARTNER_SALES, receiver);

		super.addStringField(PARTNER_ID, Message.requestPartnerKey());
	}

	@Override
	public void execute() throws CommandException {
		// Get the partner
		var partnerId = super.stringField(PARTNER_ID);
		var partner = _receiver.getPartner(partnerId).orElseThrow(() -> new UnknownPartnerKeyException(partnerId));

		// Then get their sales and breakdowns and display them
		for (var sale : streamIt(_receiver.getPartnerSales(partner))) {
			_display.addLine(_receiver.format(sale));
		}
		for (var breakdownTransaction : streamIt(_receiver.getPartnerBreakdownTransactions(partner))) {
			_display.addLine(_receiver.format(breakdownTransaction));
		}

		_display.display();
	}

}
