package ggc.core;

/**
 * A type which may be formatted according to it's warehouse
 * 
 * This interface serves to allow objects to query the warehouse during their formatting. This is necessary in some
 * places, such as products, which must know their total quantity when displaying.
 */
// Note: Package private so the user has to use `Warehouse.format` instead of this interface
interface WarehouseFormattable {
	/**
	 * Formats this type, with `warehouse` as it's owner
	 * 
	 * @param warehouseManager
	 *            The warehouse manager responsible for managing this object
	 * @return A formatted version of this object, to be displayed to the user
	 */
	// Note: This method will be `public` even if we remove the `public` modifier,
	//       so we cannot restrict it outside of `core`, we're simply careful to
	//       not use it outside of `core`.
	public String format(WarehouseManager warehouseManager);
}
