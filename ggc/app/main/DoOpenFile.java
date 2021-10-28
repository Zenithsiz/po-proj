package ggc.app.main;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import java.io.IOException;
import ggc.app.exception.FileOpenFailedException;
import ggc.core.WarehouseManager;
import ggc.core.exception.MissingFileAssociationException;

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
		// Get the existing filename in the warehouse, or ask it, then set it
		var fileName = _receiver.getFileName().orElseGet(() -> super.stringField(FILE_NAME));
		_receiver.setFileName(fileName);

		try {
			_receiver.load();
		} catch (MissingFileAssociationException | IOException | ClassNotFoundException _e) {
			throw new FileOpenFailedException(fileName);
		}
	}

}
