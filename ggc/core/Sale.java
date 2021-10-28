package ggc.core;

/// A sale by a partner
public abstract class Sale extends Transaction {

	/// Returns if this sale is paid
	abstract boolean isPaid();
}
