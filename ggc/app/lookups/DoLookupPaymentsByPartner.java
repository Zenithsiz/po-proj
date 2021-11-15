package ggc.app.lookups;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.UnknownPartnerKeyException;
import ggc.core.WarehouseManager;
import static ggc.core.util.StreamIterator.streamIt;

import java.util.stream.Stream;

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

		// Filter all sales that are paid and the transactions
		var sales = _receiver.getPartnerSales(partner).filter(_receiver.saleFilterPaid());
		var breakdownTransactions = _receiver.getPartnerBreakdownTransactions(partner);
		var transactions = Stream.of(sales, breakdownTransactions).flatMap(transaction -> transaction)
				.sorted(_receiver.transactionComparator());

		// Then display them
		for (var transaction : streamIt(transactions)) {
			_display.addLine(_receiver.format(transaction));
		}

		_display.display();
	}

}
