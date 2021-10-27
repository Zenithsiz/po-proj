package ggc.app.main;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.InvalidDateException;
import ggc.core.WarehouseManager;

/**
 * Advance current date.
 */
class DoAdvanceDate extends Command<WarehouseManager> {

	DoAdvanceDate(WarehouseManager receiver) {
		super(Label.ADVANCE_DATE, receiver);

		super.addIntegerField("offset", Message.requestDaysToAdvance());
	}

	@Override
	public final void execute() throws CommandException {
		int offset = super.integerField("offset");

		if (offset <= 0) {
			throw new InvalidDateException(offset);
		}

		_receiver.advanceDate(offset);
	}

}
