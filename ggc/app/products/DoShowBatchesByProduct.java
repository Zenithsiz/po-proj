package ggc.app.products;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.UnknownProductKeyException;
import ggc.core.WarehouseManager;

/**
 * Show all products.
 */
class DoShowBatchesByProduct extends Command<WarehouseManager> {
	private static final String PRODUCT_ID = "productId";

	DoShowBatchesByProduct(WarehouseManager receiver) {
		super(Label.SHOW_BATCHES_BY_PRODUCT, receiver);

		super.addStringField(PRODUCT_ID, Message.requestProductKey());
	}

	@Override
	public final void execute() throws CommandException {
		// Get the product
		var productId = super.stringField(PRODUCT_ID);
		var product = _receiver.getProduct(productId).orElseThrow(() -> new UnknownProductKeyException(productId));

		// Then display them
		ShowBatches.executeFilter(_receiver, _display, _receiver.batchFilterProduct(product));
	}

}
