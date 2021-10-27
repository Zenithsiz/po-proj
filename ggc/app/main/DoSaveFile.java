package ggc.app.main;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import java.io.IOException;
import java.util.Optional;
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
		// Get the existing filename, or request it
		var fileName = Optional.ofNullable(_receiver.fileName()).orElseGet(() -> super.stringField("fileName"));

		// TODO: Only save when changed ocurred
		try {
			_receiver.save(fileName);
		} catch (IOException e) {
			// Note: No `CommandException` exception to throw here
			e.printStackTrace();
		}
	}

}
