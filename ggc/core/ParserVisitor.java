package ggc.core;

import java.util.stream.Stream;
import ggc.core.util.Pair;

/** Parser visitor */
// Note: Package private because we don't need it outside of core
interface ParserVisitor {
	/**
	 * Visits a partner
	 * 
	 * @param id
	 *            Id of the partner
	 * @param name
	 *            Name of the partner
	 * @param address
	 *            Address of the partner
	 * @throws Exception
	 *             On any error
	 */
	void visitPartner(String id, String name, String address) throws Exception;

	/**
	 * Visits a batch
	 * 
	 * @param productId
	 *            Id of the product
	 * @param partnerId
	 *            Id of the partner
	 * @param quantity
	 *            Quantity of the product
	 * @param unitPrice
	 *            The per-unit price of the batch
	 * @throws Exception
	 *             On any error
	 */
	// TODO: Maybe swap this to be the same order a `Batch`?
	void visitBatch(String productId, String partnerId, int quantity, double unitPrice) throws Exception;

	/**
	 * Visits a batch
	 * 
	 * @param productId
	 *            Id of the product
	 * @param partnerId
	 *            Id of the partner
	 * @param quantity
	 *            Quantity of the product
	 * @param unitPrice
	 *            The per-unit price of the batch
	 * @param costFactor
	 *            The cost factor of the product
	 * @param recipeProductIdQuantities
	 *            The quantities of each product id for the recipe
	 * @throws Exception
	 *             On any error
	 */
	// TODO: Maybe swap this to be the same order a `Batch`?
	void visitDerivedBatch(String productId, String partnerId, int quantity, double unitPrice, double costFactor,
			Stream<Pair<String, Integer>> recipeProductIdQuantities) throws Exception;
}
