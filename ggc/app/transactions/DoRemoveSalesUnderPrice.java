package ggc.app.transactions;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.UnknownPartnerKeyException;
import ggc.core.WarehouseManager;

/**
 * Removes sales under a certain price
 */
public class DoRemoveSalesUnderPrice extends Command<WarehouseManager> {
	private static final String PARTNER_ID = "partnerId";
	private static final String PRICE_LIMIT = "priceLimit";

	public DoRemoveSalesUnderPrice(WarehouseManager receiver) {
		super("Remover Vendas", receiver);

		super.addStringField(PARTNER_ID, Message.requestPartnerKey());
		super.addIntegerField(PRICE_LIMIT, "Preço limite: ");
	}

	@Override
	public final void execute() throws CommandException {
		var partnerId = super.stringField(PARTNER_ID);
		var priceLimit = super.integerField(PRICE_LIMIT);

		// Get the partner and then remove their sales and breakdowns under the limit price
		var partner = _receiver.getPartner(partnerId).orElseThrow(() -> new UnknownPartnerKeyException(partnerId));
		var transactions = _receiver.removePartnerSalesAndBreakdownTransactionsUnder(partner, priceLimit);

		// Then display them
		for (var transaction : transactions) {
			_display.addLine(_receiver.format(transaction));
		}
		_display.addLine(String.format("Vendas removidas: %d", transactions.size()));
		_display.display();
	}

}
