package ggc.app.main;

import pt.tecnico.uilib.forms.Form;
import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import java.io.IOException;
import java.lang.reflect.Field;
import ggc.core.WarehouseManager;
import ggc.core.exception.MissingFileAssociationException;

/**
 * Save current state to file under current name (if unnamed, query for name).
 */
class DoSaveFile extends Command<WarehouseManager> {
	private static final String FILE_NAME = "fileName";

	/** @param receiver */
	DoSaveFile(WarehouseManager receiver) {
		super(Label.SAVE, receiver);

		super.addStringField(FILE_NAME, Message.newSaveAs());
	}

	@Override
	public final void execute() throws CommandException {
		// Get the existing filename in the warehouse, or ask it, then set it
		// Note: The `orElseGet` should only trigger once
		var fileName = _receiver.getFileName().orElseGet(() -> super.stringField(FILE_NAME));
		_receiver.setFileName(fileName);

		// Remove the filename field now that we have it for sure
		// Note: This "hack" is here because I'm not sure how to remove the fields without it, the only
		// public/protected fields that access `_form` either add new ones, or get the values of existing
		// fields only.
		try {
			Field formField = getClass().getSuperclass().getDeclaredField("_form");
			formField.setAccessible(true);
			Form form = (Form) formField.get(this);
			form.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}

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
