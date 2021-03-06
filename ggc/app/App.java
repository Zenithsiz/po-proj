package ggc.app;

import pt.tecnico.uilib.Dialog;
import pt.tecnico.uilib.menus.Menu;
import ggc.core.WarehouseManager;
import ggc.core.exception.ImportFileException;

/** Main driver for the management application. */
public class App {

	/**
	 * @param _args
	 *            command line arguments.
	 */
	public static void main(String[] _args) {
		try (var ui = Dialog.UI) {
			WarehouseManager manager = new WarehouseManager();

			String dataFile = System.getProperty("import");
			if (dataFile != null) {
				try {
					manager.importFile(dataFile);
				} catch (ImportFileException e) {
					// no behavior described: just present the problem
					e.printStackTrace();
				}
			}

			Menu menu = new ggc.app.main.Menu(manager);
			menu.open();
		}
	}

}
