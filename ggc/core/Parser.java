package ggc.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import ggc.core.exception.BadEntryException;
import ggc.core.exception.ParsingException;
import ggc.core.util.Pair;

/// File parser
// Note: Package private because we don't need it outside of core
class Parser {
	/// Filename of the file we're parsing
	private String _fileName;

	Parser(String fileName) {
		_fileName = fileName;
	}

	/// Visits all lines in this file
	void visit(ParserVisitor visitor) throws IOException, BadEntryException, ParsingException {
		try (var reader = new BufferedReader(new FileReader(_fileName))) {
			String line;
			while ((line = reader.readLine()) != null)
				parseLine(line, visitor);
		} catch (IOException | BadEntryException e) {
			throw e;
		} catch (Exception e) {
			throw new ParsingException(e);
		}
	}

	/// Parses a line
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

	/// Parses a partner
	private void parsePartner(String[] args, ParserVisitor visitor) throws BadEntryException, Exception {
		if (args.length != 4) {
			throw new BadEntryException("Expected 4 arguments, found " + args.length);
		}

		String id = args[1];
		String name = args[2];
		String address = args[3];

		Partner partner = new Partner(id, name, address);
		visitor.visitPartner(partner);
	}

	/// Parses a batch
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

	/// Parses a derived batch
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
		String allRecipeProducts = args[6];

		// Get all recipe products/quantities by separating on `#`s, then separate
		// them to parse the quantities and collect them into a map.
		var recipeProducts = Arrays.stream(allRecipeProducts.split("#"))
				.map(recipeProduct -> Pair.fromArray(recipeProduct.split(":")).mapRight(s -> Integer.parseInt(s)))
				.collect(Pair.toMapCollector());

		visitor.visitDerivedBatch(productId, partnerId, quantity, unitPrice, costFactor, recipeProducts);
	}
}
