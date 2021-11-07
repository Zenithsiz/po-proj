package ggc.core;

/// A package-private warehouse manager wrapper for visibility issues.
// TODO: Maybe rename?
class PackagePrivateWarehouseManagerWrapper {
	/// The warehouse manager
	private WarehouseManager _warehouseManager;

	PackagePrivateWarehouseManagerWrapper(WarehouseManager warehouseManager) {
		_warehouseManager = warehouseManager;
	}

	/// Returns the warehouse manager
	WarehouseManager getWarehouseManager() {
		return _warehouseManager;
	}
}
