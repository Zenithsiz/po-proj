package ggc.app.main;

import pt.tecnico.uilib.forms.Form;
import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import java.io.IOException;
import ggc.core.WarehouseManager;
import ggc.core.exception.MissingFileAssociationException;

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
		// Get the existing filename in the warehouse, or ask it, then set it
		// Note: The `orElseGet` should only trigger once
		var fileName = _receiver.getFileName().orElseGet(() -> Form.requestString(Message.newSaveAs()));
		_receiver.setFileName(fileName);

		// If the warehouse isn't dirty, don't do anything
		if (!_receiver.isWarehouseDirty()) {
			return;
		}

		try {
			_receiver.save();
		} catch (MissingFileAssociationException | IOException e) {
			// Note: No `CommandException` exception to throw here
			e.printStackTrace();
		}
	}

}
