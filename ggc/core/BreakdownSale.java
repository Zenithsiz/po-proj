package ggc.core;

/// A sale from a breakdown
public class BreakdownSale extends Sale {
	/// Payment date
	private int _paymentDate;

	BreakdownSale(int id, int paymentDate, Product product, Partner partner, int quantity, double totalPrice,
			int deadline) {
		super(id, product, partner, quantity, totalPrice);
		_paymentDate = paymentDate;
	}

	/// Returns the payment date of this sale
	int getPaymentDate() {
		return _paymentDate;
	}

	@Override
	public String format(WarehouseManager warehouseManager) {
		var partner = getPartner();
		var product = getProduct();
		//var baseString = new StringBuilder(String.format("DESAGREGAÇÃO|%s|%s|%s|%d|%.0f|%.0f"));
		var baseString = new StringBuilder(String.format("DESAGREGAÇÃO"));

		// DESAGREGAÇÃO|id|idPa|idPr|quantidade|vbase|vpag|data|idC1:q1:v1#...#idCN:qN:vN

		/*
		if (_paidAmount.isPresent() && _paymentDate.isPresent()) {
			baseString.append(String.format("|%d", _paymentDate.getAsInt()));
		}
		*/

		return baseString.toString();
	}

	@Override
	boolean isPaid() {
		return true;
	}
}
