package ggc.app.transactions;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.UnknownPartnerKeyException;
import ggc.app.exception.UnknownProductKeyException;
import ggc.core.WarehouseManager;

/**
 * 
 */
public class DoRegisterSaleTransaction extends Command<WarehouseManager> {
	private static final String PARTNER_ID = "partnerId";
	private static final String DEADLINE = "deadline";
	private static final String PRODUCT_ID = "productId";
	private static final String QUANTITY = "quantity";

	public DoRegisterSaleTransaction(WarehouseManager receiver) {
		super(Label.REGISTER_SALE_TRANSACTION, receiver);

		super.addStringField(PARTNER_ID, Message.requestPartnerKey());
		super.addIntegerField(DEADLINE, Message.requestPaymentDeadline());
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

		// Then register a new purchase
		var quantity = super.integerField(QUANTITY);
		var deadline = super.integerField(DEADLINE);
		_receiver.registerSale(partner, product, quantity, deadline);
	}

}
