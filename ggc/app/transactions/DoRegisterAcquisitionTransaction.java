package ggc.app.transactions;

import pt.tecnico.uilib.forms.Form;
import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import java.util.stream.IntStream;
import ggc.app.exception.UnknownPartnerKeyException;
import ggc.app.exception.UnknownProductKeyException;
import ggc.core.Product;
import ggc.core.WarehouseManager;
import ggc.core.util.Pair;
import ggc.core.util.StreamIterator;

/**
 * Register order.
 */
public class DoRegisterAcquisitionTransaction extends Command<WarehouseManager> {
	private static final String PARTNER_ID = "partnerId";
	private static final String PRODUCT_ID = "productId";
	private static final String QUANTITY = "quantity";
	private static final String UNIT_PRICE = "unitPrice";

	public DoRegisterAcquisitionTransaction(WarehouseManager receiver) {
		super(Label.REGISTER_ACQUISITION_TRANSACTION, receiver);

		super.addStringField(PARTNER_ID, Message.requestPartnerKey());
		super.addStringField(PRODUCT_ID, Message.requestProductKey());
		super.addIntegerField(QUANTITY, Message.requestAmount());
		super.addRealField(UNIT_PRICE, Message.requestPrice());
	}

	@Override
	public final void execute() throws CommandException {
		// Get the partner
		var partnerId = super.stringField(PARTNER_ID);
		var partner = _receiver.getPartner(partnerId).orElseThrow(() -> new UnknownPartnerKeyException(partnerId));

		// Then get the product, or register a new one, if it doesn't exist
		var productId = super.stringField(PRODUCT_ID);
		var product = _receiver.getProduct(productId).orElse(null);
		if (product == null) {
			product = createProductIfInexistent(productId);
		}

		// Then register a new purchase
		var quantity = super.integerField(QUANTITY);
		var unitPrice = super.realField(UNIT_PRICE);
		_receiver.registerPurchase(partner, product, quantity, unitPrice);

	}

	private Product createProductIfInexistent(String productId) throws CommandException {
		// If the user wants to add a recipe, ask for all components
		if (Form.confirm(Message.requestAddRecipe())) {
			var componentsLen = Form.requestInteger(Message.requestNumberOfComponents());
			var costFactor = Form.requestReal(Message.requestAlpha());
			var recipeProducts = IntStream.range(0, componentsLen)
					.mapToObj(_idx -> new Pair<String, Integer>(Form.requestString(Message.requestProductKey()),
							Form.requestInteger(Message.requestAmount())));

			// Note: If we get none, a component didn't exist
			return _receiver.registerDerivedProduct(productId, costFactor, recipeProducts).orElseThrow(() -> {
				// Find which component didn't exist
				for (var pair : StreamIterator.streamIt(recipeProducts)) {
					if (!_receiver.getProduct(pair.getLhs()).isPresent()) {
						return new UnknownProductKeyException(pair.getLhs());
					}
				}

				return new UnknownProductKeyException(productId);
			});
		}

		// Else add a simple product
		else {
			return _receiver.registerProduct(productId).orElseThrow(() -> new UnknownProductKeyException(productId));
		}
	}

}
