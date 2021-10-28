package ggc.core.util;

import java.util.stream.Stream;

/// Adaptor from a stream to an iterator
public class StreamIterator {
	/// Returns an `Iterable` instance from a stream
	public static <E> Iterable<E> streamIt(Stream<E> stream) {
		return () -> stream.iterator();
	}
}
