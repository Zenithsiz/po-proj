package ggc.core.util;

/// A supplier that may throw
@FunctionalInterface
public interface ThrowableSupplier<T, E extends Throwable> {
	/// Gets the result, or throws
	T get() throws E;
}
