package ggc.core;

import java.util.Map;

/// Parser visitor
// Note: Package private because we don't need it outside of core
interface ParserVisitor {
	/// Visits a partner
	void visitPartner(Partner partner) throws Exception;

	/// Visits a batch
	void visitBatch(String productId, String partnerId, int quantity, double unitPrice) throws Exception;

	/// Visits a batch of derived products
	void visitDerivedBatch(String productId, String partnerId, int quantity, double unitPrice, double costFactor,
			Map<String, Integer> recipeProducts) throws Exception;
}
