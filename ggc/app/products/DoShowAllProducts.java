package ggc.app.products;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;

import java.util.stream.Stream;

import ggc.core.Product;
import ggc.core.WarehouseManager;
import static ggc.core.util.StreamIterator.streamIt;

/**
 * Show all products.
 */
class DoShowAllProducts extends Command<WarehouseManager> {

	DoShowAllProducts(WarehouseManager receiver) {
		super(Label.SHOW_ALL_PRODUCTS, receiver);
	}

	@Override
	public final void execute() throws CommandException {
		Stream<Product> products = _receiver.getProducts().sorted(_receiver.productComparator());
		for (var product : streamIt(products)) {
			_display.addLine(_receiver.format(product));
		}

		_display.display();
	}

}
