package ggc.app.transactions;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.UnavailableProductException;
import ggc.app.exception.UnknownPartnerKeyException;
import ggc.app.exception.UnknownProductKeyException;
import ggc.core.WarehouseManager;
import ggc.core.exception.InsufficientProductsException;

/**
 * Register order.
 */
public class DoRegisterBreakdownTransaction extends Command<WarehouseManager> {
	private static final String PARTNER_ID = "partnerId";
	private static final String PRODUCT_ID = "productId";
	private static final String QUANTITY = "quantity";

	public DoRegisterBreakdownTransaction(WarehouseManager receiver) {
		super(Label.REGISTER_BREAKDOWN_TRANSACTION, receiver);

		super.addStringField(PARTNER_ID, Message.requestPartnerKey());
		super.addStringField(PRODUCT_ID, Message.requestProductKey());
		super.addIntegerField(QUANTITY, Message.requestAmount());
	}

	@Override
	public final void execute() throws CommandException {
		// Get the partner
		var partnerId = super.stringField(PARTNER_ID);
		var partner = _receiver.getPartner(partnerId).orElseThrow(() -> new UnknownPartnerKeyException(partnerId));

		// Then get the product, or register a new one, if it doesn't exist
		var productId = super.stringField(PRODUCT_ID);
		var product = _receiver.getProduct(productId).orElseThrow(() -> new UnknownProductKeyException(productId));

		// Then register a new breakdown
		var quantity = super.integerField(QUANTITY);
		try {
			_receiver.registerBreakdown(partner, product, quantity);
		} catch (InsufficientProductsException e) {
			throw new UnavailableProductException(e.getProductId(), e.getQuantityRequested(), e.getQuantityAvailable());
		}
	}

}
