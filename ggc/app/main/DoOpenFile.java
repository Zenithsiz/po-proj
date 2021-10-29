package ggc.app.main;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import java.io.IOException;
import ggc.app.exception.FileOpenFailedException;
import ggc.core.WarehouseManager;

/**
 * Open existing saved state.
 */
class DoOpenFile extends Command<WarehouseManager> {
	private static final String FILE_NAME = "fileName";

	/** @param receiver */
	DoOpenFile(WarehouseManager receiver) {
		super(Label.OPEN, receiver);

		super.addStringField(FILE_NAME, Message.openFile());
	}

	@Override
	public final void execute() throws CommandException {
		// Get the file and try to load from it
		var fileName = super.stringField(FILE_NAME);

		try {
			_receiver.loadFrom(fileName);
		} catch (IOException | ClassNotFoundException _e) {
			throw new FileOpenFailedException(fileName);
		}
	}

}
