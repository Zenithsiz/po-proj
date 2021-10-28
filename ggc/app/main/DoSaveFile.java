package ggc.app.main;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import java.io.IOException;
import java.util.Optional;
import java.util.OptionalInt;

import ggc.core.WarehouseManager;

/**
 * Save current state to file under current name (if unnamed, query for name).
 */
class DoSaveFile extends Command<WarehouseManager> {
	/** @param receiver */
	DoSaveFile(WarehouseManager receiver) {
		super(Label.SAVE, receiver);

		// TODO: Somehow don't ask for the filename if we already have it
		// Note: The asking is always done, even if we don't call `stringField` in `execute`, for some reason.
		super.addStringField("fileName", Message.newSaveAs());
	}

	@Override
	public final void execute() throws CommandException {
		// If the warehouse isn't dirty, don't do anything
		if (!_receiver.isWarehouseDirty()) {
			return;
		}

		// Else get the existing filename, or request it
		var fileName = Optional.ofNullable(_receiver.fileName()).orElseGet(() -> super.stringField("fileName"));

		try {
			_receiver.save(fileName);
		} catch (IOException e) {
			// Note: No `CommandException` exception to throw here
			e.printStackTrace();
		}
	}

}
