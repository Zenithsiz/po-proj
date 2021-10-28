package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.core.WarehouseManager;
import ggc.core.util.StreamIterator;

/**
 * Show all partners.
 */
class DoShowAllPartners extends Command<WarehouseManager> {

	DoShowAllPartners(WarehouseManager receiver) {
		super(Label.SHOW_ALL_PARTNERS, receiver);
	}

	@Override
	public void execute() throws CommandException {
		for (var partner : StreamIterator.streamIt(_receiver.getPartners())) {
			_display.addLine(_receiver.formatPartner(partner));
		}

		_display.display();
	}

}
