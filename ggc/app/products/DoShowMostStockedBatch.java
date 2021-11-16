package ggc.app.products;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.core.WarehouseManager;

/**
 * Shows the most stocked batch
 */
class DoShowMostStockedBatch extends Command<WarehouseManager> {

	DoShowMostStockedBatch(WarehouseManager receiver) {
		super("Produto com maior lote", receiver);
	}

	@Override
	public final void execute() throws CommandException {
		var batch = _receiver.getBatches().max(_receiver.batchComparatorByQuantity());

		if (batch.isPresent()) {
			_display.addLine(_receiver.formatBatchByProductIdAndQuantity(batch.get()));
			_display.display();
		}
	}

}
