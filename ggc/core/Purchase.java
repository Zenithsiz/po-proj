package ggc.core;

/// A purchase by a partner
public class Purchase extends Transaction {

	@Override
	String format() {
		var partner = getPartner();
		var product = getProduct();
		return String.format("COMPRA|%d|%s|%s|%d|%f|%d", getId(), partner.getId(), product.getId(), getAmount(),
				getTotalPrice(), getPaymentDate());
	}

}
