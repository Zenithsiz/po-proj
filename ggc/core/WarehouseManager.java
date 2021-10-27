package ggc.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import ggc.core.exception.BadEntryException;
import ggc.core.exception.ImportFileException;
import ggc.core.exception.ParsingException;
import ggc.core.exception.UnavailableFileException;

/** Façade for access. */
public class WarehouseManager {

	/** Name of file storing current warehouse. */
	private String _fileName = null;

	/** The warehouse itself. */
	private Warehouse _warehouse = new Warehouse();

	/// Returns the current file name
	public String fileName() {
		return _fileName;
	}

	/**
	 * @@throws IOException
	 */
	public void save(String fileName) throws IOException {
		// Open the file to save, and create an output stream from it
		try (var file = new FileOutputStream(fileName); var stream = new ObjectOutputStream(file)) {
			// Write the warehouse to file
			stream.writeObject(_warehouse);
		}

		_fileName = fileName;
	}

	/**
	 * @@param filename
	 * @@throws UnavailableFileException
	 * @@throws ClassNotFoundException
	 */
	public void load(String fileName) throws UnavailableFileException, ClassNotFoundException {
		try (var file = new FileInputStream(fileName); var stream = new ObjectInputStream(file)) {
			// Try to read the warehouse
			_warehouse = (Warehouse) stream.readObject();
		} catch (IOException e) {
			throw new UnavailableFileException(fileName);
		}

		_fileName = fileName;
	}

	/**
	 * @param textFile
	 * @throws ImportFileException
	 */
	public void importFile(String textFile) throws ImportFileException {
		try {
			_warehouse.importFile(textFile);
		} catch (IOException | BadEntryException | ParsingException e) {
			throw new ImportFileException(textFile, e);
		}
	}

}
