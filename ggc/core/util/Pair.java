package ggc.core.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
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

	/// Maps the left value
	public <L2> Pair<L2, R> mapLeft(Function<? super L, ? extends L2> mapper) {
		return new Pair<>(mapper.apply(_lhs), _rhs);
	}

	/// Maps the right value
	public <R2> Pair<L, R2> mapRight(Function<? super R, ? extends R2> mapper) {
		return new Pair<>(_lhs, mapper.apply(_rhs));
	}

	/// Returns a collector to use with streams of type `Pair`
	public static <Lhs, Rhs> Collector<Pair<Lhs, Rhs>, ?, Map<Lhs, Rhs>> toMapCollector() {
		return Collectors.<Pair<Lhs, Rhs>, Lhs, Rhs>toMap(pair -> pair._lhs, pair -> pair._rhs);
	}

	/// Returns a map collector that returns empty if any of the keys were empty

	/// Returns a map collector that returns `Result.Err` if any of the keys were an error
	public static <Lhs, Rhs> Collector<Pair<Result<Lhs>, Rhs>, ?, Result<Map<Lhs, Rhs>>> toResultMapCollector() {
		return new ResultMapCollectorImpl<>();
	}

	/// Collector for `toResultMapCollector`
	private static class ResultMapCollectorImpl<Lhs, Rhs> implements
			Collector<Pair<Result<Lhs>, Rhs>, ResultMapCollectorImpl.ResultHashMap<Lhs, Rhs>, Result<Map<Lhs, Rhs>>> {
		@Override
		public Supplier<ResultHashMap<Lhs, Rhs>> supplier() {
			return () -> new ResultHashMap<Lhs, Rhs>();
		}

		@Override
		public BiConsumer<ResultHashMap<Lhs, Rhs>, Pair<Result<Lhs>, Rhs>> accumulator() {
			return (resMap, pair) -> {
				// If the map is an error, don't do anything
				if (resMap.map.isErr()) {
					return;
				}

				// If the pair key is an error, set the map to the error and return
				var pairLhsErr = pair.getLhs().<HashMap<Lhs, Rhs>>mapWithErr();
				if (pairLhsErr.isPresent()) {
					resMap.map = pairLhsErr.get();
					return;
				}

				// Else add the element
				resMap.map.getOk().put(pair.getLhs().getOk(), pair.getRhs());
			};
		}

		@Override
		public BinaryOperator<ResultHashMap<Lhs, Rhs>> combiner() {
			return (lhsMap, rhsMap) -> {
				// If any of the maps are an error, return the error
				if (lhsMap.map.isErr()) {
					return lhsMap;
				}
				if (rhsMap.map.isErr()) {
					return rhsMap;
				}

				// Else merge all rhs into lhs and return it
				lhsMap.map.getOk().putAll(rhsMap.map.getOk());
				return lhsMap;
			};
		}

		@Override
		public Function<ResultHashMap<Lhs, Rhs>, Result<Map<Lhs, Rhs>>> finisher() {
			return resMap -> resMap.map.map(map -> map);
		}

		@Override
		public Set<Characteristics> characteristics() {
			return Collections.emptySet();
		}

		/// Helper class to contain a `Result<HashMap<Lhs, Rhs>>` so we can set it to `Err` in the functions above
		private static class ResultHashMap<Lhs, Rhs> {
			public Result<HashMap<Lhs, Rhs>> map = Result.ofOk(new HashMap<>());
		}
	}
}
