package ggc.app.transactions;

import pt.tecnico.uilib.forms.Form;
import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import java.util.stream.IntStream;
import ggc.app.exception.UnknownPartnerKeyException;
import ggc.app.exception.UnknownProductKeyException;
import ggc.core.Product;
import ggc.core.WarehouseManager;
import ggc.core.exception.ProductAlreadyExistsException;
import ggc.core.exception.UnknownProductIdException;
import ggc.core.util.Pair;

/**
 * Register order.
 */
public class DoRegisterAcquisitionTransaction extends Command<WarehouseManager> {
	private static final String PARTNER_ID = "partnerId";
	private static final String PRODUCT_ID = "productId";
	private static final String UNIT_PRICE = "unitPrice";
	private static final String QUANTITY = "quantity";

	public DoRegisterAcquisitionTransaction(WarehouseManager receiver) {
		super(Label.REGISTER_ACQUISITION_TRANSACTION, receiver);

		super.addStringField(PARTNER_ID, Message.requestPartnerKey());
		super.addStringField(PRODUCT_ID, Message.requestProductKey());
		super.addRealField(UNIT_PRICE, Message.requestPrice());
		super.addIntegerField(QUANTITY, Message.requestAmount());
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
			try {
				product = createProductIfInexistent(productId);
			} catch (ProductAlreadyExistsException e) {
				// Note: Can't happen, we just checked it didn't exist
				e.printStackTrace();
			}
		}

		// Then register a new purchase
		var quantity = super.integerField(QUANTITY);
		var unitPrice = super.realField(UNIT_PRICE);
		_receiver.registerPurchase(partner, product, quantity, unitPrice);
	}

	/// Creates a product if the supplied id wasn't valid.
	private Product createProductIfInexistent(String productId) throws CommandException, ProductAlreadyExistsException {
		// If the user wants to add a recipe, ask for all components
		if (Form.confirm(Message.requestAddRecipe())) {
			var componentsLen = Form.requestInteger(Message.requestNumberOfComponents());
			var costFactor = Form.requestReal(Message.requestAlpha());
			var productQuantities = IntStream.range(0, componentsLen)
					.mapToObj(_idx -> new Pair<String, Integer>(Form.requestString(Message.requestProductKey()),
							Form.requestInteger(Message.requestAmount())));

			// Note: If we get none, a component didn't exist
			try {
				return _receiver.registerDerivedProduct(productId, costFactor, productQuantities);
			} catch (UnknownProductIdException e) {
				throw new UnknownProductKeyException(e.getProductId());
			}
		}

		// Else add a simple product
		else {
			return _receiver.registerProduct(productId);
		}
	}

}
