package ggc.app.main;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.FileOpenFailedException;
import ggc.core.WarehouseManager;
import ggc.core.exception.UnavailableFileException;

/**
 * Open existing saved state.
 */
class DoOpenFile extends Command<WarehouseManager> {
	/** @param receiver */
	DoOpenFile(WarehouseManager receiver) {
		super(Label.OPEN, receiver);

		super.addStringField("fileName", Message.openFile());
	}

	@Override
	public final void execute() throws CommandException {
		String fileName = super.stringField("fileName");

		try {
			_receiver.load(fileName);
		} catch (UnavailableFileException ufe) {
			throw new FileOpenFailedException(ufe.getFilename());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
