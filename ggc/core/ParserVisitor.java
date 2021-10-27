package ggc.core;

import java.util.Map;

/// Parser visitor
public interface ParserVisitor {
	/// Visits a partner
	public void visitPartner(Partner partner) throws Exception;

	/// Visits a bundle
	public void visitBundle(String productId, String partnerId, int quantity, float unitPrice) throws Exception;

	/// Visits a bundle of derived products
	public void visitDerivedBundle(String productId, String partnerId, int quantity, float unitPrice, float costFactor,
			Map<String, Integer> recipeProducts) throws Exception;
}
