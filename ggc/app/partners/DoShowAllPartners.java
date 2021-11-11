package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import java.util.stream.Stream;
import ggc.core.Partner;
import ggc.core.WarehouseManager;
import static ggc.core.util.StreamIterator.streamIt;

/**
 * Show all partners.
 */
class DoShowAllPartners extends Command<WarehouseManager> {

	DoShowAllPartners(WarehouseManager receiver) {
		super(Label.SHOW_ALL_PARTNERS, receiver);
	}

	@Override
	public void execute() throws CommandException {
		Stream<Partner> partners = _receiver.getPartners().sorted(_receiver.partnerComparator());
		for (var partner : streamIt(partners)) {
			_display.addLine(_receiver.format(partner));
		}

		_display.display();
	}

}
