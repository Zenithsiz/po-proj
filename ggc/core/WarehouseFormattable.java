package ggc.core;

/// A type which may be formatted according to it's warehouse
public interface WarehouseFormattable {
	/// Formats this type, with `warehouse` as it's owner
	public String format(ConstWarehouse warehouse);
}
