package ggc.app.main;

import pt.tecnico.uilib.forms.Form;
import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import java.io.IOException;
import ggc.core.WarehouseManager;

/**
 * Save current state to file under current name (if unnamed, query for name).
 */
class DoSaveFile extends Command<WarehouseManager> {
	/** @param receiver */
	DoSaveFile(WarehouseManager receiver) {
		super(Label.SAVE, receiver);
	}

	@Override
	public final void execute() throws CommandException {
		// If the warehouse isn't dirty, don't do anything
		if (!_receiver.isWarehouseDirty()) {
			return;
		}

		// Else try to save it with the associated filename, or ask the user
		try {
			_receiver.save(() -> Form.requestString(Message.newSaveAs()));
		} catch (IOException e) {
			// Note: No `CommandException` exception to throw here
			e.printStackTrace();
		}
	}

}
