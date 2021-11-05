package ggc.app.transactions;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exception.UnknownTransactionKeyException;
import ggc.core.WarehouseManager;

/**
 * Show specific transaction.
 */
public class DoShowTransaction extends Command<WarehouseManager> {
	private static final String TRANSACTION_ID = "transactionId";

	public DoShowTransaction(WarehouseManager receiver) {
		super(Label.SHOW_TRANSACTION, receiver);
		super.addIntegerField(TRANSACTION_ID, Message.requestTransactionKey());
	}

	@Override
	public final void execute() throws CommandException {
		// Try to get the transaction
		int transactionId = super.integerField(TRANSACTION_ID);
		var transaction = _receiver.getTransaction(transactionId)
				.orElseThrow(() -> new UnknownTransactionKeyException(transactionId));

		// Then display it
		_display.addLine(_receiver.formatTransaction(transaction));
		_display.display();
	}

}
