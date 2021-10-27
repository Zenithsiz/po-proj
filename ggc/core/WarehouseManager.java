package ggc.core;

//FIXME import classes (cannot import from pt.tecnico or ggc.app)

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import ggc.core.exception.BadEntryException;
import ggc.core.exception.ImportFileException;
import ggc.core.exception.UnavailableFileException;
import ggc.core.exception.MissingFileAssociationException;

/** Façade for access. */
public class WarehouseManager {

	/** Name of file storing current warehouse. */
	private String _fileName = "";

	/** The warehouse itself. */
	private Warehouse _warehouse = new Warehouse();

	/// Returns if we have a file currently
	public boolean hasFile() {
		return this._fileName != null;
	}

	/**
	 * @@throws IOException
	 * @@throws FileNotFoundException
	 * @@throws MissingFileAssociationException
	 */
	public void save() throws IOException, FileNotFoundException, MissingFileAssociationException {
		// If we don't have a file, throw
		if (!this.hasFile()) {
			throw new MissingFileAssociationException();
		}

		// Open the file to save, and create an output stream from it
		try (var file = new FileOutputStream(this._fileName); var stream = new ObjectOutputStream(file)) {
			// Write the warehouse to file
			stream.writeObject(this._warehouse);
		}
	}

	/**
	 * @@param filename
	 * @@throws MissingFileAssociationException
	 * @@throws IOException
	 * @@throws FileNotFoundException
	 */
	public void saveAs(String fileName) throws MissingFileAssociationException, FileNotFoundException, IOException {
		_fileName = fileName;
		save();
	}

	/**
	 * @@param filename
	 * @@throws UnavailableFileException
	 */
	public void load(String fileName) throws UnavailableFileException, ClassNotFoundException {
		try (var file = new FileInputStream(fileName); var stream = new ObjectInputStream(file)) {
			// Try to read the warehouse
			this._warehouse = (Warehouse) stream.readObject();
		} catch (IOException e) {
			throw new UnavailableFileException(fileName);
		}

		// If we loaded it, change out filename
		this._fileName = fileName;
	}

	/**
	 * @param textFile
	 * @throws ImportFileException
	 */
	public void importFile(String textFile) throws ImportFileException {
		try {
			_warehouse.importFile(textFile);
		} catch (IOException | BadEntryException /* FIXME maybe other exceptions */ e) {
			throw new ImportFileException(textFile, e);
		}
	}

}
