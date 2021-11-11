package ggc.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import ggc.core.exception.BadEntryException;
import ggc.core.exception.ParsingException;
import ggc.core.util.Pair;

/**
 * File parser using the visitor partner.
 */
// Note: Package private because we don't need it outside of core
class Parser {
	/** Filename of the file we're parsing */
	private String _fileName;

	/**
	 * Creates a new partner
	 * 
	 * @param fileName
	 *            File name of the file to parse
	 */
	Parser(String fileName) {
		_fileName = fileName;
	}

	/**
	 * Visits all lines in this file
	 * 
	 * @param visitor
	 *            The visitor for each line
	 * @throws IOException
	 *             If unable to read the file
	 * @throws BadEntryException
	 *             If the entry was malformed
	 * @throws ParsingException
	 *             If the visitor throws any exception
	 */
	void visit(ParserVisitor visitor) throws IOException, BadEntryException, ParsingException {
		// Open the reader
		try (var reader = new BufferedReader(new FileReader(_fileName))) {
			// Then read and parse a line until we're at the end
			String line;
			while ((line = reader.readLine()) != null)
				parseLine(line, visitor);
		}
		// Wrap any other exceptions from the visitor in `ParsingException`.
		catch (Exception e) {
			throw new ParsingException(e);
		}
	}

	/**
	 * Parses a line
	 * 
	 * @param line
	 *            The line to parse
	 * @param visitor
	 *            The visitor to parse
	 * @throws BadEntryException
	 *             If the entry was malformed
	 * @throws ParsingException
	 *             If the visitor throws any exception
	 * 
	 */
	private void parseLine(String line, ParserVisitor visitor) throws BadEntryException, Exception {
		String[] args = line.split("\\|");

		if (args.length < 2) {
			throw new BadEntryException("Missing `|` in entry");
		}

		switch (args[0]) {
			case "PARTNER":
				parsePartner(args, visitor);
				break;

			case "BATCH_S":
				parseBatch(args, visitor);
				break;

			case "BATCH_M":
				parseDerivedBatch(args, visitor);
				break;

			default:
				throw new BadEntryException("Unknown entry: " + args[0]);
		}
	}

	/**
	 * Parses a partner
	 * 
	 * @param args
	 *            The arguments to parse
	 * @param visitor
	 *            The visitor to parse
	 * @throws BadEntryException
	 *             If the entry was malformed
	 * @throws ParsingException
	 *             If the visitor throws any exception
	 * 
	 */
	private void parsePartner(String[] args, ParserVisitor visitor) throws BadEntryException, Exception {
		if (args.length != 4) {
			throw new BadEntryException("Expected 4 arguments, found " + args.length);
		}

		String id = args[1];
		String name = args[2];
		String address = args[3];

		visitor.visitPartner(id, name, address);
	}

	/**
	 * Parses a batch
	 * 
	 * @param args
	 *            The arguments to parse
	 * @param visitor
	 *            The visitor to parse
	 * @throws BadEntryException
	 *             If the entry was malformed
	 * @throws ParsingException
	 *             If the visitor throws any exception
	 * 
	 */
	private void parseBatch(String[] args, ParserVisitor visitor)
			throws BadEntryException, Exception, NumberFormatException {
		if (args.length != 5) {
			throw new BadEntryException("Expected 5 arguments, found " + args.length);
		}

		String productId = args[1];
		String partnerId = args[2];
		double unitPrice = Integer.parseInt(args[3]);
		int quantity = Integer.parseInt(args[4]);

		visitor.visitBatch(productId, partnerId, quantity, unitPrice);
	}

	/**
	 * Parses a batch of derived products
	 * 
	 * @param args
	 *            The arguments to parse
	 * @param visitor
	 *            The visitor to parse
	 * @throws BadEntryException
	 *             If the entry was malformed
	 * @throws ParsingException
	 *             If the visitor throws any exception
	 * 
	 */
	private void parseDerivedBatch(String[] args, ParserVisitor visitor)
			throws BadEntryException, Exception, NumberFormatException {
		if (args.length != 7) {
			throw new BadEntryException("Expected 7 arguments, found " + args.length);
		}

		String productId = args[1];
		String partnerId = args[2];
		double unitPrice = Integer.parseInt(args[3]);
		int quantity = Integer.parseInt(args[4]);
		double costFactor = Double.parseDouble(args[5]);
		String allProductQuantities = args[6];

		// Get all recipe products/quantities by separating on `#`s, then separate
		// them to parse the quantities and collect them into a map.
		var productQuantities = Arrays.stream(allProductQuantities.split("#")) //
				.map(recipeProduct -> recipeProduct.split(":")) //
				.map(Pair::fromArray) //
				.map(pair -> pair.mapRight(Integer::parseInt));

		visitor.visitDerivedBatch(productId, partnerId, quantity, unitPrice, costFactor, productQuantities);
	}
}
