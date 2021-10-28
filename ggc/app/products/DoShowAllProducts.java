package ggc.app.products;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.core.WarehouseManager;
import ggc.core.util.StreamIterator;

/**
 * Show all products.
 */
class DoShowAllProducts extends Command<WarehouseManager> {

	DoShowAllProducts(WarehouseManager receiver) {
		super(Label.SHOW_ALL_PRODUCTS, receiver);
	}

	@Override
	public final void execute() throws CommandException {
		for (var product : StreamIterator.streamIt(_receiver.getProducts())) {
			_display.addLine(_receiver.formatProduct(product));
		}

		_display.display();
	}

}
