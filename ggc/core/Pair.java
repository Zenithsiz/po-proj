package ggc.core;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/// Pair class to hold 2 values
public class Pair<L, R> {
	/// Left value
	private L _lhs;

	/// Right value
	private R _rhs;

	public Pair(L lhs, R rhs) {
		_lhs = lhs;
		_rhs = rhs;
	}

	/// Constructs a pair from an array with 2 members.
	public static <T> Pair<T, T> fromArray(T[] array) {
		if (array.length != 2) {
			throw new RuntimeException("Expected an array of length 2");
		}

		return new Pair<>(array[0], array[1]);
	}

	/// Returns the left value
	public L getLhs() {
		return _lhs;
	}

	/// Returns the right value
	public R getRhs() {
		return _rhs;
	}

	/// Maps the right value
	public <R2> Pair<L, R2> mapRight(Function<? super R, ? extends R2> mapper) {
		return new Pair<>(_lhs, mapper.apply(_rhs));
	}

	/// Returns a collector to use with streams of type `Pair`
	public static <Lhs, Rhs> Collector<Pair<Lhs, Rhs>, ?, Map<Lhs, Rhs>> toMapCollector() {
		return Collectors.<Pair<Lhs, Rhs>, Lhs, Rhs>toMap(pair -> pair._lhs, pair -> pair._rhs);
	}
}
