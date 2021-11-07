package ggc.core;

/// A type which may be formatted according to it's warehouse
// Note: Package private so the user has to use `Warehouse.format` instead of this interface
interface WarehouseFormattable {
	/// Formats this type, with `warehouse` as it's owner
	// Note: This method will be `public` even if we remove the `public` modifier,
	//       so instead we use a package-private wrapper class over `WarehouseManager` that cannot
	//       be constructed outside of this class.
	public String format(PackagePrivateWarehouseManagerWrapper warehouseManager);
}
