package ggc.app.transactions;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.UnknownTransactionKeyException;
import ggc.core.WarehouseManager;

/**
 * Receive payment for sale transaction.
 */
public class DoReceivePayment extends Command<WarehouseManager> {
	private static final String SALE_ID = "saleId";

	public DoReceivePayment(WarehouseManager receiver) {
		super(Label.RECEIVE_PAYMENT, receiver);

		super.addIntegerField(SALE_ID, Message.requestTransactionKey());
	}

	@Override
	public final void execute() throws CommandException {
		// Try to get the transaction
		int saleId = super.integerField(SALE_ID);
		var transaction = _receiver.getTransaction(saleId)
				.orElseThrow(() -> new UnknownTransactionKeyException(saleId));

		// Then try to pay it
		_receiver.paySale(transaction);
	}

}
