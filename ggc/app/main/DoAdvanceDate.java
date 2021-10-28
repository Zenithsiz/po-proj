package ggc.app.main;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.InvalidDateException;
import ggc.core.WarehouseManager;

/**
 * Advance current date.
 */
class DoAdvanceDate extends Command<WarehouseManager> {

	private static final String DAYS_OFFSET = "offset";

	DoAdvanceDate(WarehouseManager receiver) {
		super(Label.ADVANCE_DATE, receiver);

		super.addIntegerField(DAYS_OFFSET, Message.requestDaysToAdvance());
	}

	@Override
	public final void execute() throws CommandException {
		int offset = super.integerField(DAYS_OFFSET);

		if (offset <= 0) {
			throw new InvalidDateException(offset);
		}

		_receiver.advanceDate(offset);
	}

}
