package ggc.core;

/// A type which may be formatted according to it's warehouse
// Note: Package private so the user has to use `Warehouse.format` instead of this interface
interface WarehouseFormattable {
	/// Formats this type, with `warehouse` as it's owner
	// Note: This method will be `public` even if we remove the `public` modifier,
	//       so we cannot restrict it outside of `core`, we're simply careful to
	//       not use it outside of `core`.
	public String format(WarehouseManager warehouseManager);
}
