package ggc.app.main;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.core.WarehouseManager;

/**
 * Show global balance.
 */
class DoShowGlobalBalance extends Command<WarehouseManager> {

	DoShowGlobalBalance(WarehouseManager receiver) {
		super(Label.SHOW_BALANCE, receiver);
	}

	@Override
	public final void execute() throws CommandException {
		var availableBalance = _receiver.getAvailableBalance();
		var accountingBalance = _receiver.getAccountingBalance();
		_display.addLine(Message.currentBalance(availableBalance, accountingBalance)).display();
	}

}
