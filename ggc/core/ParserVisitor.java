package ggc.core;

import java.util.Map;

/// Parser visitor
public interface ParserVisitor {
	/// Visits a partner
	public void visitPartner(Partner partner) throws Exception;

	/// Visits a batch
	public void visitBatch(String productId, String partnerId, int quantity, double unitPrice) throws Exception;

	/// Visits a batch of derived products
	public void visitDerivedBatch(String productId, String partnerId, int quantity, double unitPrice, double costFactor,
			Map<String, Integer> recipeProducts) throws Exception;
}
