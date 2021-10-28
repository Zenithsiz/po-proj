package ggc.app.products;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;

import java.util.stream.Stream;

import ggc.core.Batch;
import ggc.core.WarehouseManager;
import ggc.core.util.StreamIterator;

/**
 * Show available batches.
 */
class DoShowAvailableBatches extends Command<WarehouseManager> {

	DoShowAvailableBatches(WarehouseManager receiver) {
		super(Label.SHOW_AVAILABLE_BATCHES, receiver);
	}

	@Override
	public final void execute() throws CommandException {
		Stream<Batch> batches = _receiver.getBatches().sorted(WarehouseManager.batchComparator());
		for (var batch : StreamIterator.streamIt(batches)) {
			_display.addLine(_receiver.formatBatch(batch));
		}

		_display.display();
	}

}
