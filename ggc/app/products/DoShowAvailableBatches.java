package ggc.app.products;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.core.WarehouseManager;

/**
 * Show available batches.
 */
class DoShowAvailableBatches extends Command<WarehouseManager> {

	DoShowAvailableBatches(WarehouseManager receiver) {
		super(Label.SHOW_AVAILABLE_BATCHES, receiver);
	}

	@Override
	public final void execute() throws CommandException {
		// Note: We display all, so no filter
		ShowBatches.executeFilter(_receiver, _display, _batch -> true);
	}
}
