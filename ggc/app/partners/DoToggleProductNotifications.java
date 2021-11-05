package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.UnknownPartnerKeyException;
import ggc.app.exception.UnknownProductKeyException;
import ggc.core.Partner;
import ggc.core.Product;
import ggc.core.WarehouseManager;

/**
 * Toggle product-related notifications.
 */
class DoToggleProductNotifications extends Command<WarehouseManager> {
	private static final String PARTNER_ID = "partnerId";
	private static final String PRODUCT_ID = "productId";

	DoToggleProductNotifications(WarehouseManager receiver) {
		super(Label.TOGGLE_PRODUCT_NOTIFICATIONS, receiver);

		super.addStringField(PARTNER_ID, Message.requestPartnerKey());
		super.addStringField(PRODUCT_ID, Message.requestProductKey());
	}

	@Override
	public void execute() throws CommandException {
		// Get both the product and partner
		String partnerId = super.stringField(PARTNER_ID);
		String productId = super.stringField(PRODUCT_ID);
		Partner partner = _receiver.getPartner(partnerId).orElseThrow(() -> new UnknownPartnerKeyException(partnerId));
		Product product = _receiver.getProduct(productId).orElseThrow(() -> new UnknownProductKeyException(productId));

		// Then toggle them
		_receiver.togglePartnerNotifications(partner, product);
	}

}
