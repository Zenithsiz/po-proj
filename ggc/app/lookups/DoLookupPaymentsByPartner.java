package ggc.app.lookups;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.UnknownPartnerKeyException;
import ggc.core.WarehouseManager;
import ggc.core.util.StreamIterator;

/**
 * Lookup payments by given partner.
 */
public class DoLookupPaymentsByPartner extends Command<WarehouseManager> {
	private static final String PARTNER_ID = "partnerId";

	public DoLookupPaymentsByPartner(WarehouseManager receiver) {
		super(Label.PAID_BY_PARTNER, receiver);

		super.addStringField(PARTNER_ID, Message.requestPartnerKey());
	}

	@Override
	public void execute() throws CommandException {
		// Get the partner
		var partnerId = super.stringField(PARTNER_ID);
		var partner = _receiver.getPartner(partnerId).orElseThrow(() -> new UnknownPartnerKeyException(partnerId));

		// Filter all sales that are paid
		var sales = _receiver.getPartnerSales(partner).filter(_receiver.saleFilterPaid());

		// Then get their sales and display them
		for (var sale : StreamIterator.streamIt(sales)) {
			_display.addLine(_receiver.format(sale));
		}

		_display.display();
	}

}
