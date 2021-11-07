package ggc.app.lookups;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.core.WarehouseManager;
import ggc.core.util.StreamIterator;

/**
 * Lookup products cheaper than a given price.
 */
public class DoLookupProductBatchesUnderGivenPrice extends Command<WarehouseManager> {
	private static final String PRICE_LIMIT = "priceLimit";

	public DoLookupProductBatchesUnderGivenPrice(WarehouseManager receiver) {
		super(Label.PRODUCTS_UNDER_PRICE, receiver);

		super.addIntegerField(PRICE_LIMIT, Message.requestPriceLimit());
	}

	@Override
	public void execute() throws CommandException {
		// Get all batches under the price limit
		var priceLimit = super.integerField(PRICE_LIMIT);
		var batches = _receiver.getBatches() //
				.filter(_receiver.batchFilterPrice(price -> price < priceLimit)).sorted(_receiver.batchComparator());

		// Then display them all
		for (var batch : StreamIterator.streamIt(batches)) {
			_display.addLine(_receiver.format(batch));
		}

		_display.display();
	}

}
