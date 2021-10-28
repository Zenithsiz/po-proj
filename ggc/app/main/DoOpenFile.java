package ggc.app.main;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import java.io.IOException;
import java.util.Optional;
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
		// Get the existing filename, or
		var fileName = Optional.ofNullable(_receiver.fileName()).orElseGet(() -> super.stringField(FILE_NAME));

		try {
			_receiver.load(fileName);
		} catch (IOException | ClassNotFoundException e) {
			throw new FileOpenFailedException(fileName);
		}
	}

}
